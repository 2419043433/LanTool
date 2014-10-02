package com.orange.net.multicast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.orange.base.thread.IOThread;
import com.orange.interfaces.ThreadDelegate;
import com.orange.net.FrameDecoder;
import com.orange.net.MessageDecoder;
import com.orange.net.udp.DatagramPacketDecoder;

public class HeartBeatService implements
		HeartBeatMessageHandler.UIThreadDelegate {

	private MulticastServer mServer;
	private MessageDecoder mMessageDecoder;
	private ThreadDelegate mHeartBeatListenThread;
	private ThreadDelegate mUIThread;
	private Heart mHeart;
	private String mIp;
	private int mPort;
	private Delegate mDelegate;
	
	public static interface Delegate
	{
		void onHeartBeatMessageReceived(HeartBeatMessage msg);
	}

	public HeartBeatService(String ip, int port) {
		mIp = ip;
		mPort = port;
		mHeartBeatListenThread = new IOThread();
		mHeart = new Heart(port);
		mMessageDecoder = new MessageDecoder();
		setupMessageHandlers();
		mServer = new MulticastServer(new DatagramPacketDecoder(
				new FrameDecoder(mMessageDecoder)));
	}
	
	public void setDelegate(Delegate delegate)
	{
		mDelegate = delegate;
	}
	
	public void setUIThreadDelegate(ThreadDelegate uiThread)
	{
		mUIThread = uiThread;
	}

	private void setupMessageHandlers() {
		mMessageDecoder.add(HeartBeatMessage.class.getName(),
				new HeartBeatMessageHandler(this));
	}

	@Override
	public void onHeartBeatMessageReceived(HeartBeatMessage msg) {
		Logger.getLogger("HeartBeatService").log(Level.INFO, "receive " + msg);
		
		mUIThread.post(new HeartBeatMessageRunnable(msg));
	}
	
	private class HeartBeatMessageRunnable implements Runnable
	{
		private HeartBeatMessage mMsg;
		public HeartBeatMessageRunnable(HeartBeatMessage msg)
		{
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
		mHeartBeatListenThread.post(new HeartBeatListenCommand(mServer, mIp,
				mPort));
		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				mHeart.startBeat();
			}
		}, 0, 3000);
	}

	private class HeartBeatListenCommand implements Runnable {
		private String mIp;
		private int mPort;
		private MulticastServer mHeartBeatServer;

		public HeartBeatListenCommand(MulticastServer server, String ip,
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
