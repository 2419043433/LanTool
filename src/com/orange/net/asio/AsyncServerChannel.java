package com.orange.net.asio;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.orange.base.ErrorCode;
import com.orange.net.asio.interfaces.AsyncServerChannelBase;

public class AsyncServerChannel implements AsyncServerChannelBase {

	private AsynchronousServerSocketChannel mChannel;
	private Client mClient;
	private int mPort = 5000; // default port
	private int mMaxTryCount = 5;

	public AsyncServerChannel() {

	}

	public int start() {
		try {
			mChannel = AsynchronousServerSocketChannel.open();
			int bindCount = 5;// TODO: config this value
			while (bindCount > 0) {
				try {
					mChannel.bind(new InetSocketAddress(mPort));
				} catch (BindException e) {
					mPort++;
					bindCount--;
					continue;
				}
				mClient.onStartOk(mPort);
				break;
			}
			if(bindCount <= 0)
			{
				return -1;
			}
			mChannel.accept(null,
					new CompletionHandler<AsynchronousSocketChannel, Void>() {
						public void completed(AsynchronousSocketChannel ch,
								Void att) {
							// accept the next connection
							mChannel.accept(null, this);

							// handle this connection
							AsyncChannel channel = new AsyncChannel(ch);
							mClient.onAccept(channel);
						}

						public void failed(Throwable exc, Void att) {
							mClient.onError(ErrorCode.ErrorNetworkAcceptFailed);
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mPort;
	}

	@Override
	public void setClient(Client client) {
		mClient = client;
	}

	@Override
	public void stop() {
	}

	@Override
	public void setPort(int port, int bindMaxTryCount) {
		mPort = port;
		mMaxTryCount = bindMaxTryCount;
	}

}
