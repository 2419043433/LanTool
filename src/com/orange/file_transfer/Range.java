package com.orange.file_transfer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/*
 * a Range stands for a closed boundary range
 * that means Range[1, 2] include both 1 and 2.
 */
public class Range
{
    private long mStart;
    private long mEnd;
    private ArrayList<ByteBuffer> mDatas = new ArrayList<ByteBuffer>();

    public Range(long start, long end, ByteBuffer data)
    {
        mStart = start;
        mEnd = end;
        mDatas.add(data);

    }

    public long getStart()
    {
        return mStart;
    }

    public void setStart(long start)
    {
        this.mStart = start;
    }

    public long getEnd()
    {
        return mEnd;
    }

    public void setEnd(long end)
    {
        this.mEnd = end;
    }

    public boolean lessThan(Range other)
    {
        return mEnd < other.mStart;
    }

    public boolean isAdjacent(Range other)
    {
        return mStart == other.mEnd + 1 || other.mStart == mEnd + 1;
    }

    private void doMerge(Range other)
    {
        if (mStart == other.mEnd + 1)
        {
            mStart = other.mStart;
            mDatas.addAll(0, other.mDatas);
        }
        else if (other.mStart == mEnd + 1)
        {
            mEnd = other.mEnd;
            mDatas.addAll(other.mDatas);
        }
    }

    public boolean merge(Range other)
    {
        if (!isAdjacent(other))
        {
            return false;
        }

        doMerge(other);
        return true;
    }
}
