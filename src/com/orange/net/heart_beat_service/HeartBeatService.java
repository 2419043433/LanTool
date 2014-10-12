package com.orange.net.heart_beat_service;

import java.util.Timer;
import java.util.TimerTask;

import com.orange.base.thread.IOThread;
import com.orange.base.thread.Threads;
import com.orange.net.FrameDecoder;
import com.orange.net.MessageDecoder;
import com.orange.net.multicast.MulticastServer;
import com.orange.net.udp.DatagramPacketDecoder;

public class HeartBeatService implements
		HeartBeatMessageHandler.UIThreadDelegate {

	private MulticastServer mServer;
	private MessageDecoder mMessageDecoder;
	private IOThread mHeartBeatListenThread;
	private Heart mHeart;
	private String mIp;
	private int mPort;
	private Delegate mDelegate;

	public static interface Delegate {
		void onHeartBeatMessageReceived(HeartBeatMessage msg);
	}

	public HeartBeatService(String ip, int port, int controlPort) {
		mIp = ip;
		mPort = port;
		//better by setting it
		mHeartBeatListenThread = new IOThread();
		mHeart = new Heart(port, controlPort);
		mMessageDecoder = new MessageDecoder();
		setupMessageHandlers();
		mServer = new MulticastServer(new DatagramPacketDecoder(
				new FrameDecoder(mMessageDecoder)));
	}

	public void setDelegate(Delegate delegate) {
		mDelegate = delegate;
	}

	private void setupMessageHandlers() {
		mMessageDecoder.add(HeartBeatMessage.class.getName(),
				new HeartBeatMessageHandler(this));
	}

	@Override
	public void onHeartBeatMessageReceived(HeartBeatMessage msg) {
		Threads.forThread(Threads.Type.UI).post(new HeartBeatMessageRunnable(msg));
	}

	private class HeartBeatMessageRunnable implements Runnable {
		private HeartBeatMessage mMsg;

		public HeartBeatMessageRunnable(HeartBeatMessage msg) {
			mMsg = msg;
		}

		@Override
		public void run() {
			mDelegate.onHeartBeatMessageReceived(mMsg);
		}
	}

	public void start() {
		startOnIOThread();
	}

	private void startOnIOThread() {
		mHeartBeatListenThread.post(new HeartBeatListenRunnable(mServer, mIp,
				mPort));
		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mHeart.startBeat();
			}
		}, 0, 3000);
	}

	private class HeartBeatListenRunnable implements Runnable {
		private String mIp;
		private int mPort;
		private MulticastServer mHeartBeatServer;

		public HeartBeatListenRunnable(MulticastServer server, String ip,
				int port) {
			mHeartBeatServer = server;
			mIp = ip;
			mPort = port;
		}

		@Override
		public void run() {
			mHeartBeatServer.start(mIp, mPort);
		}
	}
}
