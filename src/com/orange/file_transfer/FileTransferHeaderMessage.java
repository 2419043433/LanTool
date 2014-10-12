package com.orange.file_transfer;

import com.orange.net.interfaces.IMessage;

public class FileTransferHeaderMessage implements IMessage {
	private static final long serialVersionUID = 1L;

	private String mJobId;
	private String mGUID;
	private String mFileName;
	private long mFileLength;
	private int mFileBlockSize;

	
	public String getJobId() {
		return mJobId;
	}

	public void setJobId(String mJobId) {
		this.mJobId = mJobId;
	}

	
	public String getGUID() {
		return mGUID;
	}

	public void setGUID(String mGUID) {
		this.mGUID = mGUID;
	}

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String mFileName) {
		this.mFileName = mFileName;
	}

	public long getFileLength() {
		return mFileLength;
	}

	public void setFileLength(long mFileLength) {
		this.mFileLength = mFileLength;
	}

	public int getFileBlockSize() {
		return mFileBlockSize;
	}

	public void setFileBlockSize(int mFileBlockSize) {
		this.mFileBlockSize = mFileBlockSize;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
