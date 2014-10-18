package com.orange.client_manage;

import java.net.InetSocketAddress;

public class ClientInfo {
	@Override
	public String toString() {
		return "ClientInfo [mEndPoint=" + mEndPoint + ", mControlPort="
				+ mControlPort + ", mClientGUID=" + mClientGUID + "]";
	}

	public ClientInfo(EndPoint endPoint, String clientGUID, int controlPort) {
		mEndPoint = endPoint;
		mControlPort = controlPort;
		mClientGUID = clientGUID;
	}

	public EndPoint mEndPoint;
	// this port should be set by command channel.
	public int mControlPort = -1;
	public String mClientGUID;

	public boolean equalsWith(InetSocketAddress other) {
		return mEndPoint.getmIp().equals(other.getAddress().getHostAddress());
	}
	
	public boolean equalsWith(String otherGUID)
	{
		return mClientGUID.equals(otherGUID);
	}
}
