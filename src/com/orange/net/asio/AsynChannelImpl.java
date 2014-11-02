package com.orange.net.asio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.TimeUnit;

import com.orange.net.asio.interfaces.IAsyncChannel;

public class AsynChannelImpl implements IAsyncChannel
{

    private AsynchronousSocketChannel mChannel;

    public AsynChannelImpl()
    {
        try
        {
            mChannel = AsynchronousSocketChannel.open();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public AsynChannelImpl(AsynchronousSocketChannel channel)
    {
        mChannel = channel;
    }

    @Override
    public <A> void connect(SocketAddress remote, A attachment, CompletionHandler<Void, ? super A> handler)
    {
        mChannel.connect(remote, attachment, handler);
    }

    @Override
    public <A> void read(ByteBuffer dst, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        mChannel.read(dst, timeout, unit, attachment, handler);
    }

    @Override
    public <A> void write(ByteBuffer src, long timeout, TimeUnit unit, A attachment, CompletionHandler<Integer, ? super A> handler)
    {
        mChannel.write(src, timeout, unit, attachment, handler);
    }

    @Override
    public SocketAddress getLocalAddress()
    {
        try
        {
            return mChannel.getLocalAddress();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public SocketAddress getRemoteAddress()
    {
        try
        {
            return mChannel.getRemoteAddress();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

}
