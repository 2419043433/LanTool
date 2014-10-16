/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.controller;

import java.net.InetSocketAddress;

import com.orange.base.ParamKeys;
import com.orange.base.Params;
import com.orange.base.thread.Threads;
import com.orange.client_manage.ClientInfo;
import com.orange.client_manage.ClientInfoManager;
import com.orange.client_manage.EndPoint;
import com.orange.file_transfer.FileReceiveService;
import com.orange.file_transfer.FileTransferHeaderMessage;
import com.orange.file_transfer.FileTransferService;
import com.orange.interfaces.CommandId;
import com.orange.interfaces.ICommandProcessor;
import com.orange.interfaces.IMessageHandler;
import com.orange.interfaces.MessageId;
import com.orange.net.asio.AsyncChannelFactory;
import com.orange.net.controller.ControlServerChannel;
import com.orange.net.heart_beat_service.HeartBeatMessage;
import com.orange.net.heart_beat_service.HeartBeatService;
import com.orange.ui.UIManager;

/**
 * 
 * @author niuyunyun
 */
public class Controller implements IMessageHandler, ICommandProcessor {

	// multicast ip and port
	private static final String kIP = "224.0.0.0";
	private static final int kPort = 6000;
	// control channel start port
	private static final int kControlPort = 7000;
	private static final int kControlChannleBindMaxTryCount = 5;

	UIManager mUiManager;
	private HeartBeatService mHeartBeatService;
	private FileTransferService mFileTransferService;
	private FileReceiveService mFileReceiveService;
	private ClientInfoManager mClientInfoManager;
	private ControlServerChannel mControlServerChannel;

	public Controller() {
		initComponents();
	}

	private void initComponents() {
		// threads
		Threads.init();

		// UI manager
		mUiManager = new UIManager(this);

		mClientInfoManager = new ClientInfoManager();
		mControlServerChannel = new ControlServerChannel(this);
	}

	public void start() {
		if (mControlServerChannel.start(kControlPort,
				kControlChannleBindMaxTryCount) < 0) {
			System.out.println("ControlServerChannel start failed, exit");
		}
	}

	private HeartBeatService.Delegate mHeartBeatServiceDelegate = new HeartBeatService.Delegate() {
		@Override
		public void onHeartBeatMessageReceived(HeartBeatMessage msg) {
			ClientInfo clientInfo = new ClientInfo(new EndPoint(msg
					.getAddress().getHostAddress(), msg.getAddress()
					.getHostName(), msg.getControlPort()), msg.getGUID(),
					msg.getControlPort());
			mClientInfoManager.addClient(clientInfo);
			Params param = Params.obtain()
					.put(ParamKeys.ClientInfo, clientInfo);
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
			ClientInfo clientInfo = (ClientInfo) param
					.get(ParamKeys.ClientInfo);
			String path = param.getString(ParamKeys.Path);
			int progress = param.getInt(ParamKeys.Value);
			// do some check
			// notify ui
			mUiManager.processCommand(CommandId.OnFileTransferProgressChanged,
					param, result);
		}
			break;
		case OnFileTransferError:
			break;

		case OnControlChannelStartOK: {
			// show main UI
			mUiManager.processCommand(CommandId.ShowMainFrame, null, null);

			// start #HeartBeatService
			int controlPort = param.getInt(ParamKeys.Port);
			mHeartBeatService = new HeartBeatService(kIP, kPort, controlPort);
			mHeartBeatService.setDelegate(mHeartBeatServiceDelegate);
			mHeartBeatService.start();
		}
			break;

		case OnFileTransferRequest:
			mUiManager.processCommand(CommandId.OnFileTransferRequest, param,
					result);
			break;

		case OnDenyFileTransferRequest: {
			// 1.send deny msg
		}
			break;
		// on others request
		case OnAcceptFileTransferRequest: {
			// start file receive service
			// send accept msg on service start ok
			if (null == mFileReceiveService) {
				mFileReceiveService = new FileReceiveService(this);
				mFileReceiveService
						.setChannelFactory(new AsyncChannelFactory());
				int port = mFileReceiveService.start(7000, 5);
				if (port < 0) {
					// handle errro;
				}
			}
			mFileReceiveService.processCommand(CommandId.CreateFileReceiveJob,
					param, result);
			mControlServerChannel.processCommand(
					CommandId.AcceptFileTransferRequest, param, result);
		}
			break;
		// my request is accepted
		case OnFileTransferRequestAccepted: {
			if(null == mFileTransferService)
			{
				mFileTransferService = new FileTransferService(this);
				mFileTransferService.startFileTransfer(param);
			}
		}
			break;

		default:
			handled = false;
			break;
		}
		return handled;
	}
}
