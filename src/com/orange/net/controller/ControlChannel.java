package com.orange.net.controller;

import java.net.InetSocketAddress;
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

	public static interface Client {
		void onFileTransferRequest(ControlChannel channel,
				FileTransferHeaderMessage msg);
	}

	public ControlChannel(AsyncChannelBase channel) {
		mChannel = channel;
		mChannel.setClient(this);
		MessageDecoder messageDecoder = new MessageDecoder();
		// add request(file or other[video, audio]) handler
		messageDecoder.add(FileTransferHeaderMessage.class.getName(),
				mFileHeaderMessageHandler);

		mStreamDecoder = new FrameDecoder(messageDecoder);

	}

	public void setClient(Client client) {
		mClient = client;
	}

	public void start() {
		startOnIOThread();
	}

	private void startOnIOThread() {
		Threads.forThread(Threads.Type.IO_Network).post(new Runnable() {

			@Override
			public void run() {
				mChannel.read(null);
			}
		});
	}

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onWriteCompleted(AsyncChannelBase channel, int length,
			Object attach) {
		// TODO Auto-generated method stub

	}
	
	public InetSocketAddress getRemoteAddress()
	{
		return mChannel.getRemoteAddress();
	}

}
