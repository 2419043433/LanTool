package com.orange.file_transfer;

import com.orange.net.interfaces.IMessage;

public class FileTransferHeaderMessage implements IMessage {
	private static final long serialVersionUID = 1L;

	private String mFileName;
	private long mFileLength;
	private int mFileBlockSize;

	public String getmFileName() {
		return mFileName;
	}

	public void setmFileName(String mFileName) {
		this.mFileName = mFileName;
	}

	public long getmFileLength() {
		return mFileLength;
	}

	public void setmFileLength(long mFileLength) {
		this.mFileLength = mFileLength;
	}

	public int getmFileBlockSize() {
		return mFileBlockSize;
	}

	public void setmFileBlockSize(int mFileBlockSize) {
		this.mFileBlockSize = mFileBlockSize;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
