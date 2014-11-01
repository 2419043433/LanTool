package com.orange.file_transfer;

import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.net.asio.interfaces.IAsyncChannel;
import com.orange.net.asio.interfaces.IAsyncChannelFactory;
import com.orange.net.asio.interfaces.IAsyncServerChannel;

public class FileReceiveService implements ICommandProcessor {
	private IMessageHandler mMessageHandler;
	private IAsyncChannelFactory mChannelFactory;
	private Map<IAsyncChannel, FileReceiveJob> mJobs = new HashMap<IAsyncChannel, FileReceiveJob>();
	private Map<String, FileReceiveJob> mWaitingJobs = new HashMap<String, FileReceiveJob>();

	public FileReceiveService(IMessageHandler handler) {
		mMessageHandler = handler;
	}

	public void setChannelFactory(IAsyncChannelFactory channelFactory) {
		mChannelFactory = channelFactory;
	}

	public int start(int port, int bindMaxTryCount) {
		IAsyncServerChannel channel = mChannelFactory
				.createAsyncServerChannel();
		while (bindMaxTryCount > 0) {
			try {
				channel.bind(new InetSocketAddress(port));
				channel.accept(null,
						new CompletionHandler<IAsyncChannel, Void>() {

							@Override
							public void completed(IAsyncChannel result,
									Void attachment) {

							}

							@Override
							public void failed(Throwable exc, Void attachment) {

							}
						});
				break;
			} catch (Exception e) {
				port++;
				bindMaxTryCount--;
			}
		}
		return port;
	}

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
