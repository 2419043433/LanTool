package com.orange.net.asio.interfaces;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

public interface IAsyncChannel
{
    <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler);

    <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler);

    <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler);

    SocketAddress getLocalAddress();

    SocketAddress getRemoteAddress();
}
