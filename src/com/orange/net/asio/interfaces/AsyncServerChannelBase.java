package com.orange.net.asio.interfaces;


public interface AsyncServerChannelBase {
	public static interface Client
	{
		void onAccept(AsyncChannelBase channel);
		void onError();
	}
	void setPort(int port);
	void setClient(Client client);
	void start();
	void stop();
}
