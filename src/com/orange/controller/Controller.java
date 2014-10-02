/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.controller;

import com.orange.base.thread.UIThread;
import com.orange.net.multicast.HeartBeatMessage;
import com.orange.net.multicast.HeartBeatService;
import com.orange.ui.desktop.FileTransferWidget;
import com.orange.ui.desktop.MainFrame;

/**
 * 
 * @author niuyunyun
 */
public class Controller {

	private static final String kIP = "224.0.0.0";
	private static final int kPort = 5000;
	private MainFrame mMainFrame;
	private HeartBeatService mHeartBeatService;
	private UIThread mUiThread;

	public Controller() {
		mMainFrame = new MainFrame(mMainFrameDelegate);
		mUiThread = new UIThread();
		mHeartBeatService = new HeartBeatService(kIP, kPort);
		mHeartBeatService.setUIThreadDelegate(mUiThread);
		mHeartBeatService.setDelegate(mHeartBeatServiceDelegate);
	}

	public void start() {
		// start heart beat listen and timer send
		mHeartBeatService.start();
	}

	private FileTransferWidget.Delegate mFileTransferDelegate = new FileTransferWidget.Delegate() {

		@Override
		public void sendFile(String filePath) {

		}
	};

	private HeartBeatService.Delegate mHeartBeatServiceDelegate = new HeartBeatService.Delegate() {
		@Override
		public void onHeartBeatMessageReceived(HeartBeatMessage msg) {
			String member = "[host:" + msg.getHost() + "][ip:" + msg.getIp() + "][pid"
					+ msg.getPID() + "]";
			mMainFrame.addMember(member);
		}
	};

	private MainFrame.Delegate mMainFrameDelegate = new MainFrame.Delegate() {
	};

	public FileTransferWidget.Delegate asFileTransferDelegate() {
		return mFileTransferDelegate;
	}

}
