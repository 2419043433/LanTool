package com.orange.net.controller;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.client_manage.ClientInfo;
import com.orange.file_transfer.FileTransferHeaderMessage;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.AsyncChannelFactoryImpl;
import com.orange.net.asio.interfaces.IAsyncChannel;
import com.orange.net.asio.interfaces.IAsyncChannelFactory;
import com.orange.net.asio.interfaces.IAsyncServerChannel;
import com.orange.system.SystemInfo;
import com.orange.system.SystemInfo.Keys;
import com.orange.util.SystemUtil;

public class ControlServerChannel implements ICommandProcessor
{
    private IMessageHandler mMessageHandler;
    private IAsyncChannelFactory mChannelFactory;
    private Map<String, ControlChannel> mChannels = new HashMap<String, ControlChannel>();
    private Set<ControlChannel> mWaitingChannels = new HashSet<ControlChannel>();

    public ControlServerChannel(IMessageHandler handler)
    {
        mMessageHandler = handler;
    }

    public void setChannelFactory(IAsyncChannelFactory channelFactory)
    {
        mChannelFactory = channelFactory;
    }

    public int start(int port, int bindMaxTryCount)
    {
        if (null == mChannelFactory)
        {
            mChannelFactory = new AsyncChannelFactoryImpl();
        }
        IAsyncServerChannel channel = mChannelFactory.createAsyncServerChannel();

        while (bindMaxTryCount > 0)
        {
            try
            {
                channel.bind(new InetSocketAddress(port));
                channel.accept(null, new CompletionHandler<IAsyncChannel, Void>()
                {

                    @Override
                    public void completed(IAsyncChannel result, Void attachment)
                    {
                        ControlChannel controlChannel = new ControlChannel(result);
                        controlChannel.setClient(mControlChannelClient);
                        mWaitingChannels.add(controlChannel);
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment)
                    {

                    }
                });
                break;
            }
            catch (Exception e)
            {
                port++;
                bindMaxTryCount--;
            }
        }

        return bindMaxTryCount == 0 ? -1 : port;
    }

    private void connect(ClientInfo info)
    {
        IAsyncChannel channelBase = mChannelFactory.createAsyncChannel();
        ControlChannel controlChannel = new ControlChannel(channelBase);
        controlChannel.setClient(mControlChannelClient);
        controlChannel.connect(new InetSocketAddress(info.mEndPoint.getmIp(), info.mControlPort));
    }

    private ControlChannel.Client mControlChannelClient = new ControlChannel.Client()
    {

        @Override
        public void onFileTransferRequest(ControlChannel channel, FileTransferHeaderMessage msg)
        {
            SocketAddress remoteAddress = channel.getRemoteAddress();
            if (null == remoteAddress)
            {
                // or notify some error here
                return;
            }
            Params param = Params.obtain().put(ParamKeys.Message, msg).put(ParamKeys.Address, remoteAddress).put(ParamKeys.Message, msg).put(ParamKeys.GUID, msg.getGUID());
            mMessageHandler.handleMessage(MessageId.OnFileTransferRequest, param, null);
        }

        @Override
        public void onClientIdentify(ControlChannel channel, IdentifyMessage msg)
        {
            assert (mWaitingChannels.contains(channel));
            mChannels.put(msg.getmGUID(), channel);
            mWaitingChannels.remove(channel);
        }

        @Override
        public void onAcceptFileTransferRequest(ControlChannel channel, AcceptFileTransfer_ResponseMessage msg)
        {
            Params param = Params.obtain().put(ParamKeys.JobID, msg.getJobId()).put(ParamKeys.GUID, channel.getGUID());
            mMessageHandler.handleMessage(MessageId.OnFileTransferRequestAccepted, param, null);

        }

        @Override
        public void onConnected(ControlChannel channel)
        {
            mChannels.put(channel.getGUID(), channel);
        }
    };

    @Override
    public boolean processCommand(CommandId id, Params param, Params result)
    {
        boolean ret = true;
        switch (id)
        {
            case AcceptFileTransferRequest:
                {
                    SocketAddress remoteAddress = (InetSocketAddress) param.get(ParamKeys.Address);
                    String guid = param.getString(ParamKeys.GUID);
                    FileTransferHeaderMessage msg = (FileTransferHeaderMessage) param.get(ParamKeys.Message);
                    AcceptFileTransfer_ResponseMessage response = new AcceptFileTransfer_ResponseMessage();
                    response.setJobId(msg.getJobId());
                    ControlChannel channel = mChannels.get(guid);
                    assert (null != channel);
                    channel.write(response);
                }
                break;
            case StartFileTransfer:
                {
                    // TODO: do this in filetransferjob
                    String path = param.getString(ParamKeys.Path);
                    File f = new File(path);

                    ClientInfo info = (ClientInfo) param.get(ParamKeys.ClientInfo);
                    FileTransferHeaderMessage message = new FileTransferHeaderMessage();
                    message.setFileBlockSize(2 * (4096));
                    message.setFileLength(f.length());
                    message.setFileName(f.getName());
                    message.setGUID(SystemInfo.getInstance().getString(Keys.GUID));
                    message.setJobId(SystemUtil.getLUID());
                    ControlChannel channel = mChannels.get(SystemInfo.getInstance().getString(Keys.GUID));
                    assert (null != channel);
                    channel.write(message);
                }
                break;

            default:
                break;
        }
        return ret;
    }
}
