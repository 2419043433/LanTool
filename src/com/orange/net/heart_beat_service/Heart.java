package com.orange.net.heart_beat_service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.orange.net.util.MessageCodecUtil;
import com.orange.util.SystemUtil;

public class Heart {
	private InetAddress mAddress;
	private DatagramPacket mPacket;
	private MulticastSocket mSocket;

	public Heart(int port, int controlPort) {
		try {
			mAddress = InetAddress.getLocalHost();
			HeartBeatMessage heartBeatMessage = new HeartBeatMessage(mAddress,
					SystemUtil.getGUID(), controlPort);
			byte[] bytes = MessageCodecUtil.writeMessage(heartBeatMessage);
			mPacket = new DatagramPacket(bytes, bytes.length, mAddress, port);
			mSocket = new MulticastSocket();
		} catch (Exception e) {
			if (null != mSocket) {
				mSocket.close();
			}
			e.printStackTrace();
		}
	}

	public void startBeat() {
		try {
			mSocket.send(mPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopBeat() {
		mSocket.close();
	}
}
