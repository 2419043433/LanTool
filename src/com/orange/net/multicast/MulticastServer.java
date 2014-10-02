package com.orange.net.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import com.orange.net.interfaces.IDatagramPacketDecoder;

/*
 * MulticastServerChannel stands for a multiCast listener with a listen looper inner.
 * You should run MulticastServerChannel in a independent thread
 * TODO: make it configurable and add stop logic
 */
public class MulticastServer {
	private IDatagramPacketDecoder mMessageHandler;
	String mIp;
	int mPort;
	/*
	 * For IP and host info, 1024 is enough It's better to make the buffer
	 * length configurable
	 */
	byte[] mReceiveBuffer = new byte[1024];

	public MulticastServer(IDatagramPacketDecoder messageHandler) {
		mMessageHandler = messageHandler;
	}

	public void start(String ip, int port) {
		mIp = ip;
		mPort = port;
		MulticastSocket ms = null;
		try {
			InetAddress address = InetAddress.getByName(mIp);
			ms = new MulticastSocket(mPort);
			ms.joinGroup(address);
			DatagramPacket packet = new DatagramPacket(mReceiveBuffer,
					mReceiveBuffer.length);
			while (true) {
				ms.receive(packet);
				if (mMessageHandler != null) {
					mMessageHandler.decode(packet);
				}
			}
		} catch (Exception e) {
			if (null != ms) {
				ms.close();
			}
			e.printStackTrace();
		}
	}
}
