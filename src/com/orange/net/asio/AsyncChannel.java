package com.orange.net.asio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.orange.base.ErrorCode;
import com.orange.net.asio.interfaces.AsyncChannelBase;

public class AsyncChannel implements AsyncChannelBase {

	private AsynchronousSocketChannel mChannel;
	// default receive buffer
	private byte[] mReceiveBuffer = new byte[10 * 4096];
	private Client mClient;

	public AsyncChannel() {
		try {
			mChannel = AsynchronousSocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public AsyncChannel(AsynchronousSocketChannel channel) {
		mChannel = channel;
	}

	@Override
	public void setReceiveBuffer(byte[] buffer) {
		mReceiveBuffer = buffer;
	}

	@Override
	public void setClient(Client client) {
		mClient = client;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		try {
			mChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void read(Object attach) {
		mChannel.read(ByteBuffer.wrap(mReceiveBuffer), attach,
				new CompletionHandler<Integer, Object>() {

					@Override
					public void completed(Integer result, Object attach) {
						mClient.onReadCompleted(AsyncChannel.this,
								mReceiveBuffer, result, attach);
					}

					@Override
					public void failed(Throwable exc, Object attach) {
						mClient.onError(AsyncChannel.this,
								ErrorCode.ErrorNetworkRead, "Read error!", exc);
					}
				});
	}

	@Override
	public void connect(SocketAddress server) {
		mChannel.connect(server, this,
				new CompletionHandler<Void, AsyncChannelBase>() {

					@Override
					public void completed(Void result, AsyncChannelBase channel) {
						mClient.onConnected(channel);
					}

					@Override
					public void failed(Throwable exc, AsyncChannelBase channel) {
						mClient.onError(channel, ErrorCode.ErrorNetworkConnect,
								"connect error!", exc);
					}
				});
	}

	@Override
	public void write(byte[] data, int offset, int length, Object attach) {
		mChannel.write(ByteBuffer.wrap(data, offset, length), attach,
				new CompletionHandler<Integer, Object>() {

					@Override
					public void completed(Integer length, Object attach) {
						mClient.onWriteCompleted(AsyncChannel.this, length,
								attach);
					}

					@Override
					public void failed(Throwable exc, Object attach) {
						mClient.onError(AsyncChannel.this,
								ErrorCode.ErrorNetworkWrite, "write error!",
								exc);
					}

				});
	}

	@Override
	public SocketAddress getRemoteAddress() {
		try {
			return mChannel.getRemoteAddress();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
