package com.orange.net.asio;

import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.orange.net.asio.interfaces.IAsyncChannel;
import com.orange.net.asio.interfaces.IAsyncServerChannel;

public class AsyncServerChannelImpl implements IAsyncServerChannel {

	private AsynchronousServerSocketChannel mChannel;

	@Override
	public IAsyncServerChannel bind(SocketAddress local) {
		try {
			if (null == mChannel) {
				mChannel = AsynchronousServerSocketChannel.open().bind(local);
			}
		} catch (Exception e) {
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> void accept(A attachment,
			CompletionHandler<IAsyncChannel, ? super A> handler) {
		assert (null != mChannel);
		mChannel.accept(attachment, new CompletionHandlerAdapter(handler));
	}

	private class CompletionHandlerAdapter<T> implements
			CompletionHandler<AsynchronousSocketChannel, T> {
		private CompletionHandler<IAsyncChannel, T> mHandler;

		public CompletionHandlerAdapter(
				CompletionHandler<IAsyncChannel, T> handler) {
			mHandler = handler;
		}

		@Override
		public void completed(AsynchronousSocketChannel result, T attachment) {
			mHandler.completed(new AsynChannelImpl(result), attachment);
		}

		@Override
		public void failed(Throwable exc, T attachment) {
			mHandler.failed(exc, attachment);
		}
	}

}
