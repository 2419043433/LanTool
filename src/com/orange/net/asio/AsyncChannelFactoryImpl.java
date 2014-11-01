package com.orange.net.asio;

import com.orange.net.asio.interfaces.IAsyncChannel;
import com.orange.net.asio.interfaces.IAsyncChannelFactory;
import com.orange.net.asio.interfaces.IAsyncServerChannel;

public class AsyncChannelFactoryImpl implements IAsyncChannelFactory {

	@Override
	public IAsyncChannel createAsyncChannel() {
		return new AsynChannelImpl();
	}

	@Override
	public IAsyncServerChannel createAsyncServerChannel() {
		return new AsyncServerChannelImpl();
	}

}
