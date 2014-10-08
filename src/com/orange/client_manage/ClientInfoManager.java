package com.orange.client_manage;

import java.util.ArrayList;

//in java currently implemented by DefaultTreeModel
public class ClientInfoManager {
	ArrayList<ClientInfo> mClientInfos = new ArrayList<ClientInfo>();
	
	void addClient(ClientInfo info)
	{
		mClientInfos.add(info);
	}
	
	void removeClient(ClientInfo info)
	{
		mClientInfos.remove(info);
	}
	
	
}
