package com.orange.file_transfer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.orange.base.ErrorCode;
import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.asio.interfaces.AsyncChannelFactoryBase;
import com.orange.net.asio.interfaces.AsyncServerChannelBase;

public class FileReceiveService implements ICommandProcessor {
	private IMessageHandler mMessageHandler;
	private AsyncChannelFactoryBase mChannelFactory;
	private Map<AsyncChannelBase, FileReceiveJob> mJobs = new HashMap<AsyncChannelBase, FileReceiveJob>();
	private Map<String, FileReceiveJob> mWaitingJobs = new HashMap<String, FileReceiveJob>();

	public FileReceiveService(IMessageHandler handler) {
		mMessageHandler = handler;
	}

	public void setChannelFactory(AsyncChannelFactoryBase channelFactory) {
		mChannelFactory = channelFactory;
	}

	public int start(int port, int bindMaxTryCount) {
		AsyncServerChannelBase channel = mChannelFactory
				.createAsyncServerChannel();
		channel.setPort(port, bindMaxTryCount);
		channel.setClient(mAsyncServerChannelClient);
		return channel.start();
	}

	private FileReceiveJob.Client mFileReceiveJobClient = new FileReceiveJob.Client() {

		@Override
		public void onProgressChanged(FileReceiveJob job, int progress) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(FileReceiveJob job) {
			// TODO Auto-generated method stub

		}
	};

	private AsyncServerChannelBase.Client mAsyncServerChannelClient = new AsyncServerChannelBase.Client() {

		@Override
		public void onError(ErrorCode code) {

		}

		@Override
		public void onAccept(AsyncChannelBase channel) {
			FileReceiveJob job = new FileReceiveJob(channel);
			job.setClient(mFileReceiveJobClient);
			job.start();
			mJobs.put(channel, job);
		}

		@Override
		public void onStartOk(int port) {
			Params param = Params.obtain().put(ParamKeys.Port, port);
			mMessageHandler.handleMessage(
					MessageId.OnFileReceivedServiceStartOk, param, null);
		}
	};

	@Override
	public boolean processCommand(CommandId id, Params param, Params result) {
		boolean ret = true;
		switch (id) {
		case CreateFileReceiveJob: {
			InetSocketAddress remoteAddress = (InetSocketAddress) param
					.get(ParamKeys.Address);
			String guid = param.getString(ParamKeys.GUID);
			FileTransferHeaderMessage msg = (FileTransferHeaderMessage) param
					.get(ParamKeys.Message);
			FileReceiveJob job = new FileReceiveJob(null);
			job.setHeader(msg);
			job.setGUID(guid);
			mWaitingJobs.put(msg.getJobId(), job);
			
		}
			break;

		default:
			break;
		}
		return ret;
	}
}
