package com.orange.net.asio;

import java.util.HashSet;
import java.util.Set;

import com.orange.base.ErrorCode;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.asio.interfaces.AsyncServerChannelBase;
import com.orange.net.interfaces.IStreamDecoder;

public class AsyncChannelManager implements AsyncServerChannelBase.Client {
	private Set<AsyncChannelBase> mChannels = new HashSet<AsyncChannelBase>();

	private IStreamDecoder mDecoder;

	public AsyncChannelManager() {

	}

	public void setDecoder(IStreamDecoder decoder) {
		mDecoder = decoder;
	}

	// ///////////////////////Asynchronous server channel client
	@Override
	public void onAccept(AsyncChannelBase channel) {
		channel.setClient(mAsyncChannelClient);
		mChannels.add(channel);
	}
	
	@Override
	public void onStartOk(int port) {
		// TODO Auto-generated method stub
		
	}
	// asynchronous server channel error, can not accept any more
	@Override
	public void onError(ErrorCode code) {

	}

	// /////////////////////Asynchronous channel client
	class AsyncChannelClient implements AsyncChannelBase.Client {

		@Override
		public boolean onReadCompleted(AsyncChannelBase channel, byte[] buffer, int length, Object attach) {
			return true;
		}

		@Override
		public void onConnected(AsyncChannelBase channel) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(AsyncChannelBase channel, ErrorCode errorCode,
				String msg, Throwable throwable) {
			mChannels.remove(channel);
		}

		@Override
		public void onWriteCompleted(AsyncChannelBase channel, int length, Object attach) {
			// TODO Auto-generated method stub

		}

	}

	private AsyncChannelClient mAsyncChannelClient = new AsyncChannelClient();


}
