package com.orange.net.controller;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import com.orange.base.ErrorCode;
import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.client_manage.ClientInfo;
import com.orange.file_transfer.FileTransferHeaderMessage;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.AsyncChannelFactory;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.asio.interfaces.AsyncChannelFactoryBase;
import com.orange.net.asio.interfaces.AsyncServerChannelBase;
import com.orange.ui.desktop.FileTransferWidget;

public class ControlServerChannel implements ICommandProcessor {
	private IMessageHandler mMessageHandler;
	private AsyncChannelFactoryBase mChannelFactory;
	private Set<ControlChannel> mChannels = new HashSet<ControlChannel>();

	public ControlServerChannel(IMessageHandler handler) {
		mMessageHandler = handler;
	}

	public void setChannelFactory(AsyncChannelFactoryBase channelFactory) {
		mChannelFactory = channelFactory;
	}

	public int start(int port, int bindMaxTryCount) {
		if (null == mChannelFactory) {
			mChannelFactory = new AsyncChannelFactory();
		}
		AsyncServerChannelBase channel = mChannelFactory
				.createAsyncServerChannel();

		channel.setPort(port, bindMaxTryCount);
		channel.setClient(mAsyncServerChannelClient);
		return channel.start();
	}

	private ControlChannel.Client mControlChannelClient = new ControlChannel.Client() {

		@Override
		public void onFileTransferRequest(ControlChannel channel,
				FileTransferHeaderMessage msg) {
			InetSocketAddress remoteAddress = channel.getRemoteAddress();
			if (null == remoteAddress) {
				// or notify some error here
				return;
			}
			Params param = Params.obtain().put(ParamKeys.Message, msg)
					.put(ParamKeys.Address, remoteAddress)
					.put(ParamKeys.Message, msg)
					.put(ParamKeys.GUID, msg.getGUID());
			mMessageHandler.handleMessage(MessageId.OnRequestFileTransfer,
					param, null);
		}
	};

	private AsyncServerChannelBase.Client mAsyncServerChannelClient = new AsyncServerChannelBase.Client() {

		@Override
		public void onError(ErrorCode code) {

		}

		@Override
		public void onAccept(AsyncChannelBase channel) {
			ControlChannel controlChannel = new ControlChannel(channel);
			controlChannel.setClient(mControlChannelClient);
			mChannels.add(controlChannel);
		}

		@Override
		public void onStartOk(int port) {
			Params param = Params.obtain().put(ParamKeys.Port, port);
			mMessageHandler.handleMessage(MessageId.OnControlChannelStartOK,
					param, null);
		}
	};

	@Override
	public boolean processCommand(CommandId id, Params param, Params result) {
		boolean ret = true;
		switch (id) {
		case AcceptFileTransferRequest: {
			InetSocketAddress remoteAddress = (InetSocketAddress) param
					.get(ParamKeys.Address);
			String guid = param.getString(ParamKeys.GUID);
			FileTransferHeaderMessage msg = (FileTransferHeaderMessage) param
					.get(ParamKeys.Message);
			AcceptFileTransfer_ResponseMessage response = new AcceptFileTransfer_ResponseMessage();
			response.setJobId(msg.getJobId());
		}
			break;

		default:
			break;
		}
		return ret;
	}
}
