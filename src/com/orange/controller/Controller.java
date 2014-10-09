/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.controller;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.base.thread.Threads;
import com.orange.client_manage.ClientInfo;
import com.orange.client_manage.ClientInfoManager;
import com.orange.file_transfer.FileReceiveService;
import com.orange.file_transfer.FileTransferService;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.AsyncChannelFactory;
import com.orange.net.heart_beat_service.HeartBeatMessage;
import com.orange.net.heart_beat_service.HeartBeatService;
import com.orange.ui.UIManager;

/**
 * 
 * @author niuyunyun
 */
public class Controller implements IMessageHandler, ICommandProcessor {

	private static final String kIP = "224.0.0.0";
	private static final int kPort = 6000;
	UIManager mUiManager;
	private HeartBeatService mHeartBeatService;
	private FileTransferService mFileTransferService;
	private FileReceiveService mFileReceiveService;
	private ClientInfoManager mClientInfoManager;

	public Controller() {
		initComponents();
	}

	private void initComponents() {
		// threads
		Threads.init();

		// UI manager
		mUiManager = new UIManager(this);
		// heart beat service
		mHeartBeatService = new HeartBeatService(kIP, kPort);
		mHeartBeatService.setDelegate(mHeartBeatServiceDelegate);

		mClientInfoManager = new ClientInfoManager();
		mFileTransferService = new FileTransferService(this);
		mFileReceiveService = new FileReceiveService(this);
		mFileReceiveService.setChannelFactory(new AsyncChannelFactory());
	}

	public void start() {
		// show main UI
		mUiManager.processCommand(CommandId.ShowMainFrame, null, null);
		// start heart beat listen and timer send
		mHeartBeatService.start();
		// TODO: should start at appropriate time
		mFileReceiveService.start();
	}

	private HeartBeatService.Delegate mHeartBeatServiceDelegate = new HeartBeatService.Delegate() {
		@Override
		public void onHeartBeatMessageReceived(HeartBeatMessage msg) {
			ClientInfo clientInfo = new ClientInfo(msg.getIp(), msg.getHost());
			mClientInfoManager.addClient(clientInfo);
			Params param = Params.obtain().put(ParamKeys.ClientInfo,
					clientInfo);
			mUiManager.processCommand(CommandId.AddMember, param, null);
		}
	};

	@Override
	public boolean processCommand(CommandId id, Params param, Params result) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleMessage(MessageId id, Params param, Params result) {
		boolean handled = true;
		switch (id) {

		case ShowFileTransferWidget:
			mUiManager.processCommand(CommandId.ShowFileTransferWidget, param,
					result);
			break;
		case StartFileTransfer: {
			mFileTransferService.processCommand(CommandId.StartFileTransfer,
					param, result);
		}
			break;
		case OnFileTransferProgressChanged: {
			ClientInfo clientInfo = (ClientInfo)param.get(ParamKeys.ClientInfo);
			String path = param.getString(ParamKeys.Path);
			int progress = param.getInt(ParamKeys.Value);
			//do some check 
			//notify ui
			mUiManager.processCommand(CommandId.OnFileTransferProgressChanged,
					param, result);
		}
			break;
		case OnFileTransferError:
			break;

		default:
			handled = false;
			break;
		}
		return handled;
	}

}
