package com.orange.net.asio.interfaces;

import com.orange.base.ErrorCode;


public interface AsyncServerChannelBase {
	public static interface Client
	{
		//server channel start operation may failed on #BindException for
		//[Address already in use], in that case, the server channel will try to find
		//a usable port to bind and start, after start successfully, notify client
		//#onStartOK
		void onStartOk(int port);
		void onAccept(AsyncChannelBase channel);
		void onError(ErrorCode code);
	}
	void setPort(int port, int bindMaxTryCount);
	void setClient(Client client);
	//start service , return succeed bind port, TODO: change this kind of mechanism
	int start();
	void stop();
}
