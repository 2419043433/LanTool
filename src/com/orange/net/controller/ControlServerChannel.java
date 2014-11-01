package com.orange.net.controller;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import com.orange.system.SystemInfo;
import com.orange.system.SystemInfo.Keys;
import com.orange.util.SystemUtil;

public class ControlServerChannel implements ICommandProcessor {
	private IMessageHandler mMessageHandler;
	private AsyncChannelFactoryBase mChannelFactory;
	private Map<String, ControlChannel> mChannels = new HashMap<String, ControlChannel>();
	private Set<ControlChannel> mWaitingChannels = new HashSet<ControlChannel>();

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
	
	private void connect(ClientInfo info)
	{
		AsyncChannelBase channelBase = mChannelFactory.createAsyncChannel();
		ControlChannel controlChannel = new ControlChannel(channelBase);
		channelBase.setClient(controlChannel);
		controlChannel.setClient(mControlChannelClient);
		controlChannel.connect(new InetSocketAddress(info.mEndPoint
				.getmIp(), info.mControlPort));
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
			mMessageHandler.handleMessage(MessageId.OnFileTransferRequest,
					param, null);
		}

		@Override
		public void onClientIdentify(ControlChannel channel, IdentifyMessage msg) {
			assert (mWaitingChannels.contains(channel));
			mChannels.put(msg.getmGUID(), channel);
			mWaitingChannels.remove(channel);
		}

		@Override
		public void onAcceptFileTransferRequest(ControlChannel channel,
				AcceptFileTransfer_ResponseMessage msg) {
			Params param = Params.obtain().put(ParamKeys.JobID, msg.getJobId())
					.put(ParamKeys.GUID, channel.getGUID());
			mMessageHandler.handleMessage(
					MessageId.OnFileTransferRequestAccepted, param, null);

		}

		@Override
		public void onConnected(ControlChannel channel) {
			mChannels.put(channel.getGUID(), channel);
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
			mWaitingChannels.add(controlChannel);
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
			ControlChannel channel = mChannels.get(guid);
			assert (null != channel);
			channel.write(response);
		}
			break;
		case StartFileTransfer: {
			//TODO: do this in filetransferjob
			String path = param.getString(ParamKeys.Path);
			File f = new File(path);
			
			ClientInfo info = (ClientInfo)param.get(ParamKeys.ClientInfo);
			FileTransferHeaderMessage message = new FileTransferHeaderMessage();
			message.setFileBlockSize(2 * (4096));
			message.setFileLength(f.length());
			message.setFileName(f.getName());
			message.setGUID(SystemInfo.getInstance().getString(Keys.GUID));;
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
