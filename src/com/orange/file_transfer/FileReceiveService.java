package com.orange.file_transfer;

import java.util.HashMap;
import java.util.Map;

import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.interfaces.AsyncChannelBase;
import com.orange.net.asio.interfaces.AsyncChannelFactoryBase;
import com.orange.net.asio.interfaces.AsyncServerChannelBase;

public class FileReceiveService {
	private IMessageHandler mMessageHandler;
	private int mPort = 5000;
	private AsyncChannelFactoryBase mChannelFactory;
	private Map<AsyncChannelBase, FileReceiveJob> mJobs = new HashMap<AsyncChannelBase, FileReceiveJob>();

	public FileReceiveService(IMessageHandler handler) {
		mMessageHandler = handler;
	}

	public void setChannelFactory(AsyncChannelFactoryBase channelFactory) {
		mChannelFactory = channelFactory;
	}

	public void start() {
		AsyncServerChannelBase channel = mChannelFactory
				.createAsyncServerChannel();
		channel.setPort(mPort);
		channel.setClient(mAsyncServerChannelClient);
		channel.start();
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
		public void onError() {

		}

		@Override
		public void onAccept(AsyncChannelBase channel) {
			FileReceiveJob job = new FileReceiveJob(channel);
			job.setClient(mFileReceiveJobClient);
			job.start();
			mJobs.put(channel, job);
		}
	};
}
