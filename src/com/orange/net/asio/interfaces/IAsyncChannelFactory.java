package com.orange.net.asio.interfaces;

public interface IAsyncChannelFactory {
	IAsyncChannel createAsyncChannel();

	IAsyncServerChannel createAsyncServerChannel();
}
