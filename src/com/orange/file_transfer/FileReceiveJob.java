package com.orange.file_transfer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.orange.base.ErrorCode;
import com.orange.base.thread.Threads;
import com.orange.net.FrameDecoder;
import com.orange.net.MessageDecoder;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.interfaces.IMessage;
import com.orange.net.interfaces.IStreamDecoder;
import com.orange.net.util.MessageCodecUtil;

/*
 * for one file receive
 */
public class FileReceiveJob implements AsyncChannelBase.Client {

	private static final String TAG = "FileReceiveJob";
	private IStreamDecoder mStreamDecoder;
	private AsyncChannelBase mChannel;
	private FileTransferHeaderMessage mHeader;
	BlockMarks mReceivedBlockMarks;
	BlockMarks mWriteBlockMarks;
	FileOutputStream mFileOutputStream;
	File mFile;
	private Client mClient;
	private Map<Integer, FileTransferBlockMessage> mReceivedBlocks = new HashMap<Integer, FileTransferBlockMessage>();

	public static interface Client {
		void onProgressChanged(FileReceiveJob job, int progress);

		void onError(FileReceiveJob job);
	}

	public FileReceiveJob(AsyncChannelBase channel) {
		mChannel = channel;
		mChannel.setClient(this);
		MessageDecoder messageDecoder = new MessageDecoder();
		messageDecoder.add(FileTransferHeaderMessage.class.getName(),
				mFileHeaderMessageHandler);
		messageDecoder.add(FileTransferBlockMessage.class.getName(),
				mFileBlockMessageHandler);
		mStreamDecoder = new FrameDecoder(messageDecoder);

		mReceivedBlockMarks = new BlockMarks();
		mReceivedBlockMarks.setClient(new BlockMarks.Client() {
			@Override
			public void onAllFinished() {
			}

			@Override
			public void onProgressChaned(int progress) {
				Logger.getLogger(TAG).log(Level.INFO,
						"receive progress " + progress + "%");
			}

			@Override
			public void onRangeFinished(int start, int end) {
				ArrayList<FileTransferBlockMessage> toWrite = new ArrayList<FileTransferBlockMessage>();
				for (int i = start + 1; i <= end; ++i) {
					toWrite.add(mReceivedBlocks.get(i));
				}
				Logger.getLogger(TAG).log(Level.INFO,
						"write block " + start + ":" + end);
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
				Logger.getLogger(TAG).log(Level.INFO,
						"write progress " + progress + "%");
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
	
	private void startOnIOThread()
	{
		Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {
			
			@Override
			public void run() {
				mChannel.read(null);
			}
		});
	}

	class WriteBlockRunnable implements Runnable {
		private ArrayList<FileTransferBlockMessage> mBlocks;

		public WriteBlockRunnable(ArrayList<FileTransferBlockMessage> blocks) {
			mBlocks = blocks;
		}

		@Override
		public void run() {
			for (FileTransferBlockMessage block : mBlocks) {
				try {
					mFileOutputStream.write(block.getData(), 0, block.getContentLength());
				} catch (IOException e) {
					e.printStackTrace();
				}

				Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {

					@Override
					public void run() {
						mWriteBlockMarks.onBlockFinished(block.getIndex());
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
			String path = "/home/wangli/Documents/" + mHeader.getmFileName();
			mFile = new File(path);
			try {
				mFileOutputStream = new FileOutputStream(mFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			mWriteBlockMarks.init((int) mHeader.getmFileLength(),
					mHeader.getmFileBlockSize());
			mReceivedBlockMarks.init((int) mHeader.getmFileLength(),
					mHeader.getmFileBlockSize());
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
			mReceivedBlockMarks.onBlockFinished(message.getIndex());
			return true;
		}
	};

	// From AsyncChannelBase.Client
	@Override
	public boolean onReadCompleted(AsyncChannelBase channel, byte[] buffer,
			int length, Object attach) {
		Logger.getLogger(TAG).log(Level.INFO, "onReadCompleted: " + length);
		mStreamDecoder.decode(buffer, 0, length);
		mChannel.read(null);
		return true;
	}

	@Override
	public void onError(AsyncChannelBase channel, ErrorCode errorCode,
			String msg, Throwable throwable) {
	}

	@Override
	public void onConnected(AsyncChannelBase channel) {
	}

	@Override
	public void onWriteCompleted(AsyncChannelBase channel, int length,
			Object attach) {
	}

}
