package com.orange.net.controller;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import com.orange.base.thread.Threads;
import com.orange.file_transfer.FileTransferHeaderMessage;
import com.orange.net.FrameDecoder;
import com.orange.net.MessageDecoder;
import com.orange.net.MessageEncoder;
import com.orange.net.asio.interfaces.IAsyncChannel;
import com.orange.net.interfaces.IMessage;
import com.orange.net.interfaces.IStreamDecoder;
import com.orange.net.util.MessageCodecUtil;

public class ControlChannel
{
    private static final String TAG = "ControlChannel";
    private IAsyncChannel mChannel;
    private IStreamDecoder mStreamDecoder;
    private MessageEncoder mMessageEncoder;
    private Client mClient;
    private String mGUID;
    private ByteBuffer mReceiveBuffer = ByteBuffer.wrap(new byte[40960]);
    private Queue<ByteBuffer> mPendingBuffers = new LinkedList<ByteBuffer>();

    public static interface Client
    {
        void onFileTransferRequest(ControlChannel channel, FileTransferHeaderMessage msg);

        void onClientIdentify(ControlChannel channel, IdentifyMessage msg);

        void onAcceptFileTransferRequest(ControlChannel channel, AcceptFileTransfer_ResponseMessage msg);

        void onConnected(ControlChannel channel);
    }

    public ControlChannel(IAsyncChannel channel)
    {
        mChannel = channel;
        MessageDecoder messageDecoder = new MessageDecoder();
        // add request(file or other[video, audio]) handler
        messageDecoder.add(FileTransferHeaderMessage.class.getName(), mFileHeaderMessageHandler);
        messageDecoder.add(IdentifyMessage.class.getName(), mIdentifyMessageHandler);
        messageDecoder.add(AcceptFileTransfer_ResponseMessage.class.getName(), mAcceptFileTransferMessageHandler);

        mStreamDecoder = new FrameDecoder(messageDecoder);
        mMessageEncoder = new MessageEncoder();

    }

    public void setGUID(String guid)
    {
        mGUID = guid;
    }

    public String getGUID()
    {
        return mGUID;
    }

    public void setClient(Client client)
    {
        mClient = client;
    }

    public void start()
    {
        startOnIOThread();
    }

    public SocketAddress getRemoteAddress()
    {
        return mChannel.getRemoteAddress();
    }

    public void write(IMessage msg)
    {
        // serialize and send
        // TODO: add serialize logic, add mechanism to ensure data is sent ok
        byte[] data = mMessageEncoder.message(msg).encode();
        ByteBuffer bt = ByteBuffer.wrap(data);
        mPendingBuffers.offer(bt);
        mChannel.write(bt, 0, TimeUnit.MILLISECONDS, null, mWriteCompletionHandler);
    }

    private CompletionHandler<Integer, Void> mWriteCompletionHandler = new CompletionHandler<Integer, Void>()
    {
        @Override
        public void completed(Integer result, Void attachment)
        {
            Threads.forThread(Threads.Type.IO_Network).post(new Runnable()
            {
                @Override
                public void run()
                {
                    ByteBuffer bt = mPendingBuffers.peek();
                    bt.position(bt.position() + result);
                    if (bt.remaining() > 0)
                    {
                        mChannel.write(bt, 0, TimeUnit.MILLISECONDS, null, mWriteCompletionHandler);
                        return;
                    }
                    mPendingBuffers.poll();
                    if (mPendingBuffers.size() > 0)
                    {
                        ByteBuffer next = mPendingBuffers.peek();
                        mChannel.write(next, 0, TimeUnit.MILLISECONDS, null, mWriteCompletionHandler);
                    }
                }
            });
        }

        @Override
        public void failed(Throwable exc, Void attachment)
        {
            // TODO Auto-generated method stub

        }
    };

    private void doRead()
    {
        mReceiveBuffer.clear();
        mChannel.read(mReceiveBuffer, 0L, TimeUnit.MILLISECONDS, null, mReadCompletionHandler);
    }

    private void startOnIOThread()
    {
        Threads.forThread(Threads.Type.IO_Network).post(new Runnable()
        {
            @Override
            public void run()
            {
                doRead();
            }
        });
    }

    public void connect(SocketAddress server)
    {
        mChannel.connect(server, null, new CompletionHandler<Void, Void>()
        {
            @Override
            public void completed(Void result, Void attachment)
            {
                Threads.forThread(Threads.Type.UI).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mClient.onConnected(ControlChannel.this);
                    }
                });
            }

            @Override
            public void failed(Throwable exc, Void attachment)
            {

            }
        });
    }

    private CompletionHandler<Integer, Void> mReadCompletionHandler = new CompletionHandler<Integer, Void>()
    {

        @Override
        public void completed(Integer result, Void attachment)
        {
            /*
             * Callbacks of IAsyncChannel are from different thread, but we need
             * handle all the decode OP on IO_Network Thread, so just post this
             * runnable to IO_Network thread to handle read complete OP
             */
            Threads.forThread(Threads.Type.IO_Network).post(new Runnable()
            {
                @Override
                public void run()
                {
                    // do decode and continue read from channel
                    mStreamDecoder.decode(mReceiveBuffer.array(), 0, result);
                    doRead();
                }
            });
        }

        @Override
        public void failed(Throwable exc, Void attachment)
        {

        }
    };

    // TODO: make it as template
    private com.orange.net.interfaces.IMessageHandler mFileHeaderMessageHandler = new com.orange.net.interfaces.IMessageHandler()
    {

        @Override
        public boolean handleMessage(IMessage msg)
        {
            FileTransferHeaderMessage requestMessage = MessageCodecUtil.convert(msg);
            if (null == requestMessage)
            {
                return false;
            }
            Threads.forThread(Threads.Type.UI).post(new Runnable()
            {
                @Override
                public void run()
                {
                    mClient.onFileTransferRequest(ControlChannel.this, requestMessage);
                }
            });
            return true;
        }
    };

    private com.orange.net.interfaces.IMessageHandler mIdentifyMessageHandler = new com.orange.net.interfaces.IMessageHandler()
    {

        @Override
        public boolean handleMessage(IMessage msg)
        {
            IdentifyMessage requestMessage = MessageCodecUtil.convert(msg);
            if (null == requestMessage)
            {
                return false;
            }
            Threads.forThread(Threads.Type.UI).post(new Runnable()
            {
                @Override
                public void run()
                {
                    ControlChannel.this.setGUID(requestMessage.getmGUID());
                    mClient.onClientIdentify(ControlChannel.this, requestMessage);
                }
            });
            return true;
        }
    };

    private com.orange.net.interfaces.IMessageHandler mAcceptFileTransferMessageHandler = new com.orange.net.interfaces.IMessageHandler()
    {

        @Override
        public boolean handleMessage(IMessage msg)
        {
            AcceptFileTransfer_ResponseMessage message = MessageCodecUtil.convert(msg);
            if (null == message)
            {
                return false;
            }
            Threads.forThread(Threads.Type.UI).post(new Runnable()
            {
                @Override
                public void run()
                {
                    mClient.onAcceptFileTransferRequest(ControlChannel.this, message);
                }
            });
            return true;
        }
    };

}
