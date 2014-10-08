package com.orange.client_manage;

public class ClientInfo {
	public ClientInfo(String ip, String host) {
		mIp = ip;
		mHost = host;
	}

	@Override
	public String toString() {
		return "ClientInfo [mIp=" + mIp + ", mHost=" + mHost + "]";
	}

	public String mIp;
	public String mHost;
	//this port should be set by command channel.
	public int mPort = 5000;

}
