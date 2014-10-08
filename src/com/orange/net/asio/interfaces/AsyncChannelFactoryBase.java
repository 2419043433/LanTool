package com.orange.net.asio.interfaces;


public interface AsyncChannelFactoryBase {
	AsyncChannelBase createAsyncChannel();
	AsyncServerChannelBase createAsyncServerChannel();
}
