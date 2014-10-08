package com.orange.net.controller;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.file_transfer.FileTransferHeaderMessage;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.asio.interfaces.AsyncChannelFactoryBase;
import com.orange.net.asio.interfaces.AsyncServerChannelBase;

public class ControlServerChannel {
	private IMessageHandler mMessageHandler;
	private int mPort = 6000;
	private AsyncChannelFactoryBase mChannelFactory;
	private Set<ControlChannel> mChannels = new HashSet<ControlChannel>();

	public ControlServerChannel(IMessageHandler handler) {
		mMessageHandler = handler;
	}

	public void setChannelFactory(AsyncChannelFactoryBase channelFactory) {
		mChannelFactory = channelFactory;
	}
	
	public void setPort(int port)
	{
		mPort = port;
	}

	public void start() {
		AsyncServerChannelBase channel = mChannelFactory
				.createAsyncServerChannel();
		channel.setPort(mPort);
		channel.setClient(mAsyncServerChannelClient);
		channel.start();
	}
	
	private ControlChannel.Client mControlChannelClient = new ControlChannel.Client() {
		
		@Override
		public void onFileTransferRequest(ControlChannel channel,
				FileTransferHeaderMessage msg) {
			SocketAddress remoteAddress = channel.getRemoteAddress();
			if(null == remoteAddress)
			{
				//or notify some error here
				return;
			}
			Params param = Params.obtain().put(ParamKeys.Message, msg).put(ParamKeys.Ip, remoteAddress.toString());
			mMessageHandler.handleMessage(MessageId.OnRequestFileTransfer, param, null);
		}
	};


	private AsyncServerChannelBase.Client mAsyncServerChannelClient = new AsyncServerChannelBase.Client() {

		@Override
		public void onError() {

		}

		@Override
		public void onAccept(AsyncChannelBase channel) {
			ControlChannel controlChannel = new ControlChannel(channel);
			controlChannel.setClient(mControlChannelClient);
			mChannels.add(controlChannel);
		}
	};
}
