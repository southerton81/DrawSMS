/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.nba;

import java.util.Arrays;

/**
 *
 * @author Kate
 */
public class CircularBuffer<E> {
    private E[] mElements;
    private int mCursor = -1;
    private int mLastAddedCursor;
    private boolean mIsNextAvailable = false;
    private final int mMaxElements;

    CircularBuffer(int maxElements) {
        mElements = (E[]) new Object[maxElements];
        mMaxElements = mElements.length;
    }

    public void Add(E element) {
        mIsNextAvailable = false;
        
        mCursor++;
        
        if (mCursor >= mMaxElements) {
            mCursor = 0;
        }
        
        mLastAddedCursor = mCursor;
        mElements[mCursor] = element;
    }

    public E Prev() {
        mIsNextAvailable = true;
        if ((mCursor - 1) < 0) {
            mCursor = mMaxElements;
        }
        return mElements[--mCursor];
    }

    public E Next() {
        if (!mIsNextAvailable)
            return null;
        
        mCursor++;
        if (mCursor >= mMaxElements) {
            mCursor = 0;
        }
        if (mCursor == mLastAddedCursor)
            mIsNextAvailable = false;
        return mElements[mCursor];
    }

    public boolean IsPrevExists() {
        int Prev = mCursor - 1;
        if (Prev < 0) {
            Prev = mMaxElements - 1;
        }
        
        if (Prev == mLastAddedCursor) {
            return false;
        }
        return mElements[Prev] != null;
    }

    public boolean IsNextExists() {
        return mIsNextAvailable;
    }
}
