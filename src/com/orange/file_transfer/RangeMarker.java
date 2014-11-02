package com.orange.file_transfer;

import java.util.ArrayList;

//ranges may come from different sources, so we can not 
//ensure their income order
public class RangeMarker
{
    int mFinished;
    ArrayList<Range> mRanges = new ArrayList<Range>();
    private Client mClient;

    public static interface Client
    {
        void onRangeFinished(Range range);
    }

    public RangeMarker()
    {

    }

    public void setClient(Client client)
    {
        mClient = client;
    }

    public void mergeRange(Range range)
    {
        // insert new range into mRanges
        if (range.lessThan(mRanges.get(0)))
        {
            if (range.getStart() == mFinished + 1)
            {
                if (range.isAdjacent(mRanges.get(0)))
                {
                    range.merge(mRanges.get(0));
                    mRanges.remove(0);
                }
                mClient.onRangeFinished(range);
            }
            else
            {
                mRanges.add(0, range);
            }
        }
        else if (mRanges.get(mRanges.size() - 1).lessThan(range))
        {
            mRanges.add(mRanges.size(), range);
        }
        else
        {
            for (int i = 0; i < mRanges.size() - 1; ++i)
            {
                if (mRanges.get(i).lessThan(range) && range.lessThan(mRanges.get(i + 1)))
                {
                    boolean adjacentToPrev = mRanges.get(i).isAdjacent(range);
                    boolean adjacentToNext = mRanges.get(i + 1).isAdjacent(range);
                    if (adjacentToNext && adjacentToPrev)
                    {
                        range.merge(mRanges.get(i));
                        range.merge(mRanges.get(i + 1));
                        mRanges.set(i, range);
                        mRanges.remove(i + 1);
                    }
                    else if (adjacentToPrev)
                    {
                        mRanges.set(i, range);
                    }
                    else if (adjacentToNext)
                    {
                        mRanges.set(i + 1, range);
                    }
                    else
                    {
                        mRanges.add(i + 1, range);
                    }

                }
            }
        }
    }
}
