package com.orange.net.asio;

import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.asio.interfaces.AsyncChannelFactoryBase;
import com.orange.net.asio.interfaces.AsyncServerChannelBase;

public class AsyncChannelFactory implements AsyncChannelFactoryBase{

	@Override
	public AsyncChannelBase createAsyncChannel() {
		return new AsyncChannel();
	}

	@Override
	public AsyncServerChannelBase createAsyncServerChannel() {
		return new AsyncServerChannel();
	}

}
