package com.orange.file_transfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.orange.base.thread.Threads;
import com.orange.net.FrameDecoder;
import com.orange.net.MessageDecoder;
import com.orange.net.asio.interfaces.IAsyncChannel;
import com.orange.net.interfaces.IMessage;
import com.orange.net.interfaces.IStreamDecoder;
import com.orange.net.util.MessageCodecUtil;

/*
 * for one file receive
 */
public class FileReceiveJob {

	private static final String TAG = "FileReceiveJob";
	private IStreamDecoder mStreamDecoder;
	private IAsyncChannel mChannel;
	private ByteBuffer mReceiveBuffer = ByteBuffer.wrap(new byte[40960]);
	private FileTransferHeaderMessage mHeader;

	private int mReceivedLength = 0;// used for test, including header and body
	private String mGUID;
	// block marks
	BlockMarks mReceivedBlockMarks;
	BlockMarks mWriteBlockMarks;
	// blocks received but have not been written to file
	private Map<Integer, FileTransferBlockMessage> mReceivedBlocks = new HashMap<Integer, FileTransferBlockMessage>();

	// file and related output stream
	FileOutputStream mFileOutputStream;
	File mFile;
	// client
	private Client mClient;

	public static interface Client {
		void onProgressChanged(FileReceiveJob job, int progress);

		void onError(FileReceiveJob job);
	}

	public void setHeader(FileTransferHeaderMessage header) {
		mHeader = header;
	}

	public void setGUID(String guid) {
		mGUID = guid;
	}

	private void log(String info) {
		// Logger.getLogger(TAG).log(Level.INFO, info);
	}

	public FileReceiveJob(IAsyncChannel channel) {

		mChannel = channel;

		// setup decoders
		MessageDecoder messageDecoder = new MessageDecoder();
		messageDecoder.add(FileTransferHeaderMessage.class.getName(),
				mFileHeaderMessageHandler);
		messageDecoder.add(FileTransferBlockMessage.class.getName(),
				mFileBlockMessageHandler);
		mStreamDecoder = new FrameDecoder(messageDecoder);
		// setup block marker
		mReceivedBlockMarks = new BlockMarks();
		mReceivedBlockMarks.setClient(new BlockMarks.Client() {
			@Override
			public void onAllFinished() {
			}

			@Override
			public void onProgressChaned(int progress) {
				log("receive progress " + progress + "%");
			}

			@Override
			public void onRangeFinished(int start, int end) {
				System.out.println("receive block[" + (start + 1) + ":" + end
						+ "]");
				ArrayList<FileTransferBlockMessage> toWrite = new ArrayList<FileTransferBlockMessage>();
				for (int i = start + 1; i <= end; ++i) {
					toWrite.add(mReceivedBlocks.get(i));
				}
				log("write block " + start + ":" + end);
				Threads.forThread(Threads.Type.IO_File).post(
						new WriteBlockRunnable(toWrite));
			}
		});

		mWriteBlockMarks = new BlockMarks();
		mWriteBlockMarks.setClient(new BlockMarks.Client() {
			@Override
			public void onAllFinished() {
				try {
					mFileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onProgressChaned(int progress) {
				mClient.onProgressChanged(FileReceiveJob.this, progress);
				log("write progress " + progress + "%");
			}

			@Override
			public void onRangeFinished(int start, int end) {
				// TODO Auto-generated method stub

			}
		});
	}

	public void start() {
		startOnIOThread();
	}

	private void startOnIOThread() {
		Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {
			@Override
			public void run() {
				mChannel.read(mReceiveBuffer, 0L, TimeUnit.MILLISECONDS, null,
						mReadCompletionHandler);
			}
		});
	}

	private CompletionHandler<Integer, Void> mReadCompletionHandler = new CompletionHandler<Integer, Void>() {

		@Override
		public void completed(Integer result, Void attachment) {
			/*
			 * Callbacks of IAsyncChannel are from different thread, but we need
			 * handle all the decode OP on IO_Network Thread, so just post this
			 * runnable to IO_Network thread to handle read complete OP
			 */
			Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {
				@Override
				public void run() {
					// do decode and continue read from channel
					mReceivedLength += result;
					if (mHeader != null) {
						System.out.println("receive:" + mReceivedLength
								+ " file length:" + mHeader.getFileLength());
					}
					mStreamDecoder.decode(mReceiveBuffer.array(), 0, result);
					mChannel.read(mReceiveBuffer, 0L, TimeUnit.MILLISECONDS,
							null, mReadCompletionHandler);
				}
			});
		}

		@Override
		public void failed(Throwable exc, Void attachment) {

		}
	};

	/*
	 * Write block to file, run in IO_File Thread
	 */
	class WriteBlockRunnable implements Runnable {
		private ArrayList<FileTransferBlockMessage> mBlocks;

		public WriteBlockRunnable(ArrayList<FileTransferBlockMessage> blocks) {
			mBlocks = blocks;
		}

		@Override
		public void run() {
			for (FileTransferBlockMessage block : mBlocks) {
				// System.out.println("write block start : " +
				// block.getIndex());
				try {
					mFileOutputStream.write(block.getData(), 0,
							block.getContentLength());
				} catch (IOException e) {
					e.printStackTrace();
				}

				// notify IO_Network Thread write block finished and can read
				// more blocks
				Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {
					@Override
					public void run() {
						// System.out.println("write block finished : " +
						// block.getIndex());
						mWriteBlockMarks.onBlockFinished(block.getIndex());
						mReceivedBlocks.remove(block.getIndex());
					}
				});
			}
		}

	}

	public void setClient(Client client) {
		mClient = client;
	}

	// MessageHandler
	private com.orange.net.interfaces.IMessageHandler mFileHeaderMessageHandler = new com.orange.net.interfaces.IMessageHandler() {

		@Override
		public boolean handleMessage(IMessage msg) {
			mHeader = MessageCodecUtil.convert(msg);
			if (null == mHeader) {
				return false;
			}
			// temporary for this path, TODO: let user choose use default or
			// select a new path
			String path = "/home/wangli/Documents/" + mHeader.getFileName();
			mFile = new File(path);
			try {
				mFileOutputStream = new FileOutputStream(mFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			// initialize block marks on header message
			mWriteBlockMarks.init((int) mHeader.getFileLength(),
					mHeader.getFileBlockSize());
			mReceivedBlockMarks.init((int) mHeader.getFileLength(),
					mHeader.getFileBlockSize());
			return true;
		}
	};
	private com.orange.net.interfaces.IMessageHandler mFileBlockMessageHandler = new com.orange.net.interfaces.IMessageHandler() {

		@Override
		public boolean handleMessage(IMessage msg) {
			FileTransferBlockMessage message = MessageCodecUtil.convert(msg);
			if (null == message) {
				return false;
			}
			mReceivedBlocks.put(message.getIndex(), message);
			System.out.println("receive block:" + message.getIndex()
					+ " block count:" + mReceivedBlockMarks.getBlockNum());
			mReceivedBlockMarks.onBlockFinished(message.getIndex());
			return true;
		}
	};
}
