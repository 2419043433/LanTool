package com.orange.net.asio.interfaces;

import java.net.SocketAddress;
import java.nio.channels.CompletionHandler;

public interface IAsyncServerChannel {
    IAsyncServerChannel bind(SocketAddress local);

	public <A> void accept(A attachment,
			CompletionHandler<IAsyncChannel, ? super A> handler);
}
