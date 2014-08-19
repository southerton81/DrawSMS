/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kurovsky.drawsms;

import android.content.SharedPreferences;

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
    
    boolean IsEmpty(){
        return mCursor == -1;
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
    
    void Store(SharedPreferences.Editor prefsEditor) {
        prefsEditor.putInt("HistoryCursor", mCursor);
        prefsEditor.putInt("HistoryLastAddedCursor", mLastAddedCursor);
        prefsEditor.putBoolean("HistoryIsNextAvailable", mIsNextAvailable);

        for (int i = 0; i < mMaxElements; i++) {
            if (mElements[i] == null) {
                prefsEditor.putString("HistoryNet" + String.valueOf(i), "null");
            } else {
                CNet net = (CNet) mElements[i];
                net.Store(prefsEditor, "HistoryNet" + String.valueOf(i));
            }
        }
    }

    void Restore(SharedPreferences prefs) {
        mCursor = prefs.getInt("HistoryCursor", -1);
        mLastAddedCursor = prefs.getInt("HistoryLastAddedCursor", mLastAddedCursor);
        mIsNextAvailable = prefs.getBoolean("HistoryIsNextAvailable", false);
        
        for (int i = 0; i < mMaxElements; i++) {
            String string = prefs.getString("HistoryNet" + String.valueOf(i), "null");

            if (string.contentEquals("null")) {
                mElements[i] = null;
            } else {
                CNet net = new CNet();
                net.Restore(prefs, "HistoryNet" + String.valueOf(i));
                mElements[i] = (E) net;
            }
        }
    }
}
