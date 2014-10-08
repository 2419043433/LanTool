package com.orange.file_transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.orange.base.ErrorCode;
import com.orange.base.thread.Threads;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.asio.interfaces.AsyncChannelFactoryBase;
import com.orange.net.util.MessageCodecUtil;

//TODO: add weak semantics
//currently we just implement small file transfer, huge file transfer should make a new implementation
public class FileTransferJob {
	private static final String TAG = "FileTransferJob";

	public static interface Client {
		// notify file transfer progress changed
		void onProgressChanged(FileTransferJob job, int progress);

		// notify file transfer finished
		void onFinished(FileTransferJob job);

		// notify error
		void onError(FileTransferJob job, ErrorCode errorCode, String msg,
				Throwable throwable);
	}

	// ---------------------------------------------IO Thread only begin
	private FileInputStream mFileInputStream;
	private FileTransferState mState;
	private int mBlockSize = 2 * (4096);
	private BlockBuffer mBlockBuffer = new BlockBuffer();
	private BlockMarks mSendBlockMarks;
	private BlockMarks mReadBlockMarks;
	// ---------------------------------------------IO Thread only end
	private File mFile;
	private long mFilePointer = 0;
	private String mDestIp;
	private int mDestPort;
	private Client mClient;
	private AsyncChannelFactoryBase mChannelFactory;
	private AsyncChannelBase mChannel;
	private boolean mIsReadFinished = false;

	private enum FileTransferState {
		Init, Connect, ReadHeader, SendHeader, ReadBody, SendBody, Finished
	}

	public FileTransferJob() {
		mState = FileTransferState.Init;

		mSendBlockMarks = new BlockMarks();
		mSendBlockMarks.setClient(new BlockMarks.Client() {
			@Override
			public void onAllFinished() {
				mState = FileTransferState.Finished;
			}

			@Override
			public void onProgressChaned(int progress) {
				notifyProgressChangedOnUIThread(progress);
				Logger.getLogger(TAG).log(Level.INFO,
						"send progress " + progress + "%");
			}

			@Override
			public void onRangeFinished(int start, int end) {
				// TODO Auto-generated method stub

			}
		});

		mReadBlockMarks = new BlockMarks();
		mReadBlockMarks.setClient(new BlockMarks.Client() {
			@Override
			public void onAllFinished() {
				try {
					mIsReadFinished = true;
					mFileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onProgressChaned(int progress) {
				Logger.getLogger(TAG).log(Level.INFO,
						"read progress " + progress + "%");
			}

			@Override
			public void onRangeFinished(int start, int end) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void notifyProgressChangedOnUIThread(int progress) {
		Threads.forThread(Threads.Type.UI).post(new Runnable() {

			@Override
			public void run() {
				mClient.onProgressChanged(FileTransferJob.this, progress);
			}
		});
	}

	public void setDest(String ip, int port) {
		mDestIp = ip;
		mDestPort = port;
	}

	public void setFilePath(String path) {
		mFile = new File(path);

		// currently only handle file size < 4G, huge file should use another
		// transfer strategy
		mSendBlockMarks.init((int) mFile.length(), mBlockSize);
		mReadBlockMarks.init((int) mFile.length(), mBlockSize);
	}

	public String getFilePath() {
		return mFile.getAbsolutePath();
	}

	public String getDestIp() {
		return mDestIp;
	}

	public int getDestPort() {
		return mDestPort;
	}

	public void setClient(Client client) {
		mClient = client;
	}

	public void setAsyncChannelFactory(AsyncChannelFactoryBase channelFactory) {
		mChannelFactory = channelFactory;
	}

	public void start() {
		if (!mFile.exists()) {
			mClient.onError(FileTransferJob.this, ErrorCode.ErrorFileNotFound,
					"File " + mFile + " not exists!", null);
			return;
		}

		assert (mState == FileTransferState.Init);
		if (mState != FileTransferState.Init) {
			mClient.onError(FileTransferJob.this, ErrorCode.ErrorIllegalState,
					"Start file transfer job on state [" + mState + "]", null);
			return;
		}

		startOnIOThread();
	}

	private void startOnIOThread() {
		Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {
			@Override
			public void run() {
				mChannel = mChannelFactory.createAsyncChannel();
				mChannel.setClient(mAsyncChannelClient);
				mChannel.connect(new InetSocketAddress(mDestIp, mDestPort));
				mState = FileTransferState.Connect;
			}
		});
	}

	private void sendHeader() {
		Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {

			@Override
			public void run() {
				FileTransferHeaderMessage message = new FileTransferHeaderMessage();
				message.setmFileBlockSize(mBlockSize);
				message.setmFileLength(mFile.length());
				message.setmFileName(mFile.getName());
				byte[] data = MessageCodecUtil.writeMessage(message);
				mChannel.write(data, null);
			}
		});
	}

	private void checkAndReadBody() {
		if (!needReadBody()) {
			return;
		}

		readBody();
	}

	private boolean needReadBody() {
		if (mState != FileTransferState.SendBody) {
			return false;
		}
		if (mBlockBuffer.contains(BlockBufferState.Reading)) {
			return false;
		}
		return true;
	}

	private void readBody() {
		if (mIsReadFinished) {
			return;
		}
		if (null == mFileInputStream) {
			try {
				mFileInputStream = new FileInputStream(mFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		BlockBuffer.Item item = mBlockBuffer.getIdleItem();
		if (null == item) {
			return;
		}

		item.mState = BlockBufferState.Reading;
		Threads.forThread(Threads.Type.IO_File).post(
				new ReadFileBlockRunnable(item));
	}

	private void sendBody() {
		if (mState == FileTransferState.Finished) {
			return;
		}
		if (mBlockBuffer.contains(BlockBufferState.Sending)) {
			return;
		}
		BlockBuffer.Item item = mBlockBuffer.getFirstReadOKItem();
		if (null == item) {
			return;
		}

		item.mState = BlockBufferState.Sending;
		FileTransferBlockMessage message = new FileTransferBlockMessage();
		message.setIndex(item.mIndex);
		message.setContentLength(item.mContentLength);
		message.setData(item.mBuffer);
		byte[] data = MessageCodecUtil.writeMessage(message);
		mChannel.write(data, (Object) item);
	}

	private class ReadFileBlockRunnable implements Runnable {
		BlockBuffer.Item mItem;

		public ReadFileBlockRunnable(BlockBuffer.Item item) {
			mItem = item;
		}

		@Override
		public void run() {
			try {
				if(mFileInputStream.available() == 0)
				{
					return;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int bytesRead = -1;
			try {
				mItem.mIndex = (int) mFilePointer / mBlockSize;
				/*
				 * TODO: 1. asynchronously read file to avoiding block the
				 * entire file thread. 2. check if read method can ensure read
				 * buffer.length byte data
				 */
				bytesRead = mFileInputStream.read(mItem.mBuffer);
				Logger.getLogger(TAG).log(Level.INFO,
						"readFile****: " + bytesRead);
			} catch (IOException e) {
				e.printStackTrace();
			}
			mFilePointer += mBlockSize;
			mItem.mContentLength = bytesRead;
			Threads.forThread(Threads.Type.IO_Network).post(
					new ReadFileBlockOKRunnable(mItem));
		}
	}

	private class ReadFileBlockOKRunnable implements Runnable {

		private BlockBuffer.Item mItem;

		public ReadFileBlockOKRunnable(BlockBuffer.Item item) {
			mItem = item;
		}

		@Override
		public void run() {
			mItem.mState = BlockBufferState.ReadOK;
			mReadBlockMarks.onBlockFinished(mItem.mIndex);
			// read next block.
			readBody();
			// check and send block(if all blocks are reading, sending operation
			// may be hold up
			sendBody();
		}
	}

	private AsyncChannelBase.Client mAsyncChannelClient = new AsyncChannelBase.Client() {

		@Override
		public boolean onReadCompleted(AsyncChannelBase channel, byte[] buffer,
				int length, Object attach) {
			return false;
		}

		@Override
		public void onConnected(AsyncChannelBase channel) {
			assert (mState == FileTransferState.Connect);
			if (mState != FileTransferState.Connect) {
				mClient.onError(FileTransferJob.this,
						ErrorCode.ErrorIllegalState,
						"Connect completed but job on state [" + mState + "]",
						null);
				return;
			}
			sendHeader();
			mState = FileTransferState.SendHeader;
		}

		@Override
		public void onError(AsyncChannelBase channel, ErrorCode errorCode,
				String msg, Throwable throwable) {
			mClient.onError(FileTransferJob.this, errorCode, msg, throwable);
			// may handle error here
		}

		@Override
		public void onWriteCompleted(AsyncChannelBase channel, int length,
				Object attach) {
//			Logger.getLogger(TAG)
//					.log(Level.INFO, "onWriteCompleted: " + length + "[threadid:" + Thread.currentThread().getId() + "]");
			assert (mState == FileTransferState.SendHeader || mState == FileTransferState.SendBody);
			switch (mState) {
			case SendHeader:
				mState = FileTransferState.SendBody;
				break;
			case SendBody:
				BlockBuffer.Item item = (BlockBuffer.Item) attach;
				item.mState = BlockBufferState.Idle;
				mSendBlockMarks.onBlockFinished(item.mIndex);
				break;
			default:
				break;
			}
			sendBody();
			checkAndReadBody();
		}
	};

	// at most one item can be in Reading or Sending state,
	// other items are in either Idle or ReadOK state
	enum BlockBufferState {
		Idle, Reading, ReadOK, Sending,
	}

	private class BlockBuffer {
		ArrayList<Item> mItems = new ArrayList<Item>();

		public BlockBuffer() {
			for (int i = 0; i < 5; ++i) {
				mItems.add(new Item());
			}
		}

		public Item getIdleItem() {
			for (Item item : mItems) {
				if (item.mState == BlockBufferState.Idle) {
					return item;
				}
			}

			return null;
		}

		public boolean contains(BlockBufferState state) {
			for (Item item : mItems) {
				if (item.mState == state) {
					return true;
				}
			}

			return false;
		}

		public Item getFirstReadOKItem() {
			for (Item item : mItems) {
				if (item.mState == BlockBufferState.ReadOK) {
					return item;
				}
			}

			return null;
		}

		class Item {
			public int mIndex = -1;
			public byte[] mBuffer = new byte[mBlockSize];
			public int mContentLength;
			public BlockBufferState mState = BlockBufferState.Idle;
		}
	}

}