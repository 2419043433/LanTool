package com.orange.file_transfer;

import java.util.HashSet;
import java.util.Set;

import com.orange.base.ErrorCode;
import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.client_manage.ClientInfo;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.AsyncChannelFactory;
import com.orange.net.asio.AsyncChannelFactoryImpl;

public class FileTransferService implements ICommandProcessor,
		FileTransferJob.Client {
	private IMessageHandler mMessageHandler;
	private Set<FileTransferJob> mJobs = new HashSet<FileTransferJob>();

	public FileTransferService(IMessageHandler handler) {
		mMessageHandler = handler;
	}

	public void startFileTransfer(Params param) {
		ClientInfo info = (ClientInfo) param.get(ParamKeys.ClientInfo);
		String path = param.getString(ParamKeys.Path);
		FileTransferJob job = new FileTransferJob();
		mJobs.add(job);
		job.setClientInfo(info);
		job.setFilePath(path);
		job.setAsyncChannelFactory(new AsyncChannelFactoryImpl());
		job.setClient(this);
		job.start();
	}

	@Override
	public boolean processCommand(CommandId id, Params param, Params result) {
		boolean ret = true;
		switch (id) {
		case StartFileTransfer:
			startFileTransfer(param);
			break;

		default:
			break;
		}
		return ret;
	}

	@Override
	public void onProgressChanged(FileTransferJob job, int progress) {
		Params param = Params.obtain()
				.put(ParamKeys.ClientInfo, job.getClientInfo())
				.put(ParamKeys.Path, job.getFilePath())
				.put(ParamKeys.Value, progress);
		mMessageHandler.handleMessage(MessageId.OnFileTransferProgressChanged,
				param, null);
	}

	@Override
	public void onError(FileTransferJob job, ErrorCode errorCode, String msg,
			Throwable throwable) {
		Params param = Params.obtain()
				.put(ParamKeys.ClientInfo, job.getClientInfo())
				.put(ParamKeys.Path, job.getFilePath())
				.put(ParamKeys.Path, job.getFilePath());
		mMessageHandler.handleMessage(MessageId.OnFileTransferError, param,
				null);
	}

	@Override
	public void onFinished(FileTransferJob job) {
		mJobs.remove(job);
	}
}
