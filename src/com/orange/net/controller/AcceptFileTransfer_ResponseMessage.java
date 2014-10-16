package com.orange.net.controller;

import com.orange.net.interfaces.IMessage;

public class AcceptFileTransfer_ResponseMessage implements IMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mJobId;

	public String getJobId() {
		return mJobId;
	}

	public void setJobId(String mJobId) {
		this.mJobId = mJobId;
	}
}
