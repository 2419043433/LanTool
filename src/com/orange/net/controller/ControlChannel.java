package com.orange.net.controller;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.orange.base.ErrorCode;
import com.orange.base.thread.Threads;
import com.orange.file_transfer.FileTransferHeaderMessage;
import com.orange.net.FrameDecoder;
import com.orange.net.MessageDecoder;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.interfaces.IMessage;
import com.orange.net.interfaces.IStreamDecoder;
import com.orange.net.util.MessageCodecUtil;

public class ControlChannel implements AsyncChannelBase.Client {
	private static final String TAG = "ControlChannel";
	private AsyncChannelBase mChannel;
	private IStreamDecoder mStreamDecoder;
	private Client mClient;
	private String mGUID;

	public static interface Client {
		void onFileTransferRequest(ControlChannel channel,
				FileTransferHeaderMessage msg);

		void onClientIdentify(ControlChannel channel, IdentifyMessage msg);

		void onAcceptFileTransferRequest(ControlChannel channel,
				AcceptFileTransfer_ResponseMessage msg);

		void onConnected(ControlChannel channel);
	}

	public ControlChannel(AsyncChannelBase channel) {
		mChannel = channel;
		mChannel.setClient(this);
		MessageDecoder messageDecoder = new MessageDecoder();
		// add request(file or other[video, audio]) handler
		messageDecoder.add(FileTransferHeaderMessage.class.getName(),
				mFileHeaderMessageHandler);
		messageDecoder.add(IdentifyMessage.class.getName(),
				mIdentifyMessageHandler);
		messageDecoder.add(AcceptFileTransfer_ResponseMessage.class.getName(),
				mAcceptFileTransferMessageHandler);

		mStreamDecoder = new FrameDecoder(messageDecoder);

	}

	public void setGUID(String guid) {
		mGUID = guid;
	}

	public String getGUID() {
		return mGUID;
	}

	public void setClient(Client client) {
		mClient = client;
	}

	public void start() {
		startOnIOThread();
	}

	private Queue<ByteBuffer> mPendingBuffers = new LinkedList<ByteBuffer>();

	public void write(IMessage msg) {
		// serialize and send
		// TODO: add serialize logic, add mechanism to ensure data is sent ok
		byte[] data = MessageCodecUtil.writeMessage(msg);
		ByteBuffer bt = ByteBuffer.wrap(data);
		mPendingBuffers.offer(bt);
		mChannel.write(bt.array(), bt.position(), bt.remaining(), null);
	}

	private void startOnIOThread() {
		Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {

			@Override
			public void run() {
				mChannel.read(null);
			}
		});
	}
	
	void connect(SocketAddress server)
	{
		mChannel.connect(server);
	}

	// TODO: make it as template
	private com.orange.net.interfaces.IMessageHandler mFileHeaderMessageHandler = new com.orange.net.interfaces.IMessageHandler() {

		@Override
		public boolean handleMessage(IMessage msg) {
			FileTransferHeaderMessage requestMessage = MessageCodecUtil
					.convert(msg);
			if (null == requestMessage) {
				return false;
			}
			Threads.forThread(Threads.Type.UI).post(new Runnable() {
				@Override
				public void run() {
					mClient.onFileTransferRequest(ControlChannel.this,
							requestMessage);
				}
			});
			return true;
		}
	};

	private com.orange.net.interfaces.IMessageHandler mIdentifyMessageHandler = new com.orange.net.interfaces.IMessageHandler() {

		@Override
		public boolean handleMessage(IMessage msg) {
			IdentifyMessage requestMessage = MessageCodecUtil.convert(msg);
			if (null == requestMessage) {
				return false;
			}
			Threads.forThread(Threads.Type.UI).post(new Runnable() {
				@Override
				public void run() {
					ControlChannel.this.setGUID(requestMessage.getmGUID());
					mClient.onClientIdentify(ControlChannel.this,
							requestMessage);
				}
			});
			return true;
		}
	};

	private com.orange.net.interfaces.IMessageHandler mAcceptFileTransferMessageHandler = new com.orange.net.interfaces.IMessageHandler() {

		@Override
		public boolean handleMessage(IMessage msg) {
			AcceptFileTransfer_ResponseMessage message = MessageCodecUtil
					.convert(msg);
			if (null == message) {
				return false;
			}
			Threads.forThread(Threads.Type.UI).post(new Runnable() {
				@Override
				public void run() {
					mClient.onAcceptFileTransferRequest(ControlChannel.this,
							message);
				}
			});
			return true;
		}
	};

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(AsyncChannelBase channel) {
		mClient.onConnected(ControlChannel.this);

	}

	//TODO: make a common writecomplete handler
	class WriteCompletedRunnable implements Runnable {
		private int mLengthHolder;

		public WriteCompletedRunnable(AsyncChannelBase channel, int length,
				Object attach) {
			mLengthHolder = length;
		}

		@Override
		public void run() {
			ByteBuffer bt = mPendingBuffers.peek();
			bt.position(bt.position() + mLengthHolder);
			if (bt.remaining() > 0) {
				mChannel.write(bt.array(), bt.position(), bt.remaining(), null);
				return;
			}
			mPendingBuffers.poll();
			if (mPendingBuffers.size() > 0) {
				ByteBuffer next = mPendingBuffers.peek();
				mChannel.write(next.array(), next.position(), next.remaining(),
						null);
			}
		}
	}

	@Override
	public void onWriteCompleted(AsyncChannelBase channel, int length,
			Object attach) {
		Threads.forThread(Threads.Type.IO_Network).post(
				new WriteCompletedRunnable(channel, length, attach));

	}

	public InetSocketAddress getRemoteAddress() {
		return mChannel.getRemoteAddress();
	}

}
