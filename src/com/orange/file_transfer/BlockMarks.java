package com.orange.file_transfer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.BitSet;

import org.w3c.dom.ranges.Range;

public class BlockMarks {
	BitSet mBlockMarks;
	private int mBlockNum = -1;
	private int mFinishedMark = -1;
	private Client mClient;

	public static interface Client {
		void onAllFinished();

		void onProgressChaned(int progress);
		//( ] range 
		void onRangeFinished(int start, int end);
	}

	public BlockMarks() {

	}

	public void setClient(Client client) {
		mClient = client;
	}

	public void init(int total, int blockSize) {
		mBlockNum = (int) total / blockSize;
		if (total % blockSize != 0) {
			mBlockNum += 1;
		}
		mBlockMarks = new BitSet(mBlockNum);
		
	}
	
	public void onBlockFinished(int index) {
		mBlockMarks.set(index);
		int progress = (mBlockMarks.cardinality() * 100 / mBlockNum);
		mClient.onProgressChaned(progress);
		boolean newRangeFinished = false;
		if(mFinishedMark == index -1)
		{
			newRangeFinished = true;
		}
		if(newRangeFinished)
		{
			int newFinishedIndex = index;
			for(int i = index; i < mBlockNum; ++ i)
			{
				if(!mBlockMarks.get(i))
				{
					newFinishedIndex = i - 1;
					break;
				}
			}
			mClient.onRangeFinished(mFinishedMark, newFinishedIndex);
			mFinishedMark = newFinishedIndex;
		}
		
		if (mBlockMarks.cardinality() == mBlockNum) {
			mClient.onAllFinished();
		}
	}
	
	int getBlockNum()
	{
		return mBlockNum;
	}

	public void writeObject(ObjectOutputStream stream) {
		try {
			stream.writeObject(mBlockMarks);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
