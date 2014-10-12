package com.orange.net.heart_beat_service;

import java.net.InetAddress;

import com.orange.net.interfaces.IMessage;

public class HeartBeatMessage implements IMessage {
	private static final long serialVersionUID = 1L;
	private InetAddress mAddress;
	private String mGUID;// just for local multiple-instance communication
							// test
	private int mControlPort;// control channel listen port

	public int getControlPort() {
		return mControlPort;
	}

	public void setControlPort(int mControlPort) {
		this.mControlPort = mControlPort;
	}

	public HeartBeatMessage(InetAddress address, String guid, int port) {
		mAddress = address;
		mGUID = guid;
		mControlPort = port;
	}

	public void setAddress(InetAddress address) {
		mAddress = address;
	}

	public InetAddress getAddress() {
		return mAddress;
	}

	public String getGUID() {
		return mGUID;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "HeartBeatMessage [mAddress=" + mAddress + ", mGUID="
				+ mGUID + ", mControlPort=" + mControlPort + "]";
	}
}
