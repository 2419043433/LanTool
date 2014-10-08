package com.orange.net.asio.interfaces;

import java.net.SocketAddress;

import com.orange.base.ErrorCode;

public interface AsyncChannelBase {
	public static interface Client {
		boolean onReadCompleted(AsyncChannelBase channel, byte[] buffer,  int length, Object attach);

		void onError(AsyncChannelBase channel, ErrorCode errorCode, String msg, Throwable throwable);

		void onConnected(AsyncChannelBase channel);
		
		void onWriteCompleted(AsyncChannelBase channel, int length, Object attach);
	}

	void setReceiveBuffer(byte[] buffer);

	void setClient(Client client);

	void start();

	void stop();

	void read(Object attach);

	void connect(SocketAddress server);
	
	void write(byte[] data, Object attach);
}
