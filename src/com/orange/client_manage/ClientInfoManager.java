package com.orange.client_manage;

import java.util.ArrayList;

//in java currently implemented by DefaultTreeModel
public class ClientInfoManager {
	ArrayList<ClientInfo> mClientInfos = new ArrayList<ClientInfo>();

	public ClientInfoManager() {
	}

	public void addClient(ClientInfo clientInfo) {
		mClientInfos.add(clientInfo);
	}

	public void removeClient(ClientInfo clientInfo) {
		mClientInfos.remove(clientInfo);
	}

}
