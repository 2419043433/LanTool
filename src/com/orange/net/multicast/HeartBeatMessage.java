package com.orange.net.multicast;

import com.orange.net.interfaces.IMessage;
import com.orange.util.SystemUtil;

public class HeartBeatMessage implements IMessage {
	private static final long serialVersionUID = 1L;
	private String mIp;
	private String mHost;
	private long mTimeStamp;
	private long mProcessId;// just for local multiple-instance communication
							// test

	public HeartBeatMessage(String ip, String host, long timeStamp) {
		mIp = ip;
		mHost = host;
		mTimeStamp = timeStamp;
		mProcessId = SystemUtil.getPID();
	}

	public String getIp() {
		return mIp;
	}

	public void setIp(String mIp) {
		this.mIp = mIp;
	}

	public String getHost() {
		return mHost;
	}

	public void setHost(String mHost) {
		this.mHost = mHost;
	}

	public long getTimeStamp() {
		return mTimeStamp;
	}

	public void setTimeStamp(long mTimeStamp) {
		this.mTimeStamp = mTimeStamp;
	}
	
	public long getPID()
	{
		return mProcessId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "HeartBeatMessage [mIp=" + mIp + ", mHost=" + mHost
				+ ", mTimeStamp=" + mTimeStamp + "]";
	}

}
