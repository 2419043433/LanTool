package com.orange.file_transfer;

/*
 * a Range should map with a set of blocks
 */
public class Range {
	private long mStart;
	private long mEnd;
	
	public Range(long start, long end)
	{
		mStart = start;
		mEnd = end;
	}

	public long getStart() {
		return mStart;
	}

	public void setStart(long start) {
		this.mStart = start;
	}

	public long getEnd() {
		return mEnd;
	}

	public void setmEnd(long end) {
		this.mEnd = end;
	}
	
	private boolean isAdjacent(Range other)
	{
		return mStart == other.mEnd + 1 || other.mStart == mEnd + 1;
	}
	
	private void mergeSelf(Range other)
	{
		if(mStart == other.mEnd + 1)
		{
			mStart = other.mStart;
		}
		else if(other.mStart == mEnd + 1)
		{
			mEnd = other.mEnd;
		}
	}
	
	public boolean merge(Range other)
	{
		if(!isAdjacent(other))
		{
			return false;
		}
		
		mergeSelf(other);
		return true;
	}
}
