package com.orange.net.asio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.orange.net.asio.interfaces.AsyncServerChannelBase;

public class AsyncServerChannel implements AsyncServerChannelBase {

	private AsynchronousServerSocketChannel mChannel;
	private Client mClient;
	private int mPort = 5000; // default port

	public AsyncServerChannel() {

	}

	public void start() {
		try {
			mChannel = AsynchronousServerSocketChannel.open().bind(
					new InetSocketAddress(mPort));
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
							mClient.onError();
						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setPort(int port) {
		mPort = port;
	}

	@Override
	public void setClient(Client client) {
		mClient = client;
	}

	@Override
	public void stop() {
	}

}
