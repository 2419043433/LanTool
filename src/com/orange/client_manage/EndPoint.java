package com.orange.client_manage;

public class EndPoint {
	private String mIp;
	private String mHost;
	private int mPort;
	
	public EndPoint(String ip, String host, int port)
	{
		mIp = ip;
		mHost = host;
		mPort = port;
	}
	public String getmIp() {
		return mIp;
	}
	public void setmIp(String mIp) {
		this.mIp = mIp;
	}
	public String getmHost() {
		return mHost;
	}
	public void setmHost(String mHost) {
		this.mHost = mHost;
	}
	public int getmPort() {
		return mPort;
	}
	public void setmPort(int mPort) {
		this.mPort = mPort;
	}
	
}
