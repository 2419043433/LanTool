package com.orange.file_transfer;

import com.orange.net.interfaces.IMessage;

public class FileTransferBlockMessage implements IMessage {
	private static final long serialVersionUID = 1L;

	private int mIndex;
	private int mContentLength;
	private byte[] mData;

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int mIndex) {
		this.mIndex = mIndex;
	}

	public int getContentLength() {
		return mContentLength;
	}

	public void setContentLength(int mSize) {
		this.mContentLength = mSize;
	}

	public byte[] getData() {
		return mData;
	}

	public void setData(byte[] mData) {
		this.mData = mData;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
