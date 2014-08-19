package com.kurovsky.drawsms;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class CNetView extends View {
    DrawSMS mMainActivity;
    CNet mMainNet;
    CNet mMoveNet;
    //CNet mDemoNet;
    Point mSymbolSize;
    Point mFirst;
    Paint mPaint = new Paint();
    Paint mPaintMove = new Paint();
    Paint mPaintHighlight = new Paint();
    RectF mHighlightRect = new RectF();
    char mSymbol[] = new char[1];
    int mHighlightX = -1;
    int mHighlightY = -1;
    Point mDownPoint = new Point(0, 0);
    Point mSize = new Point(0,0);
    
    double mPixelsPerMillisecond;
    long   mLastTime;

    public CNetView(Context context, AttributeSet attr) {
        super(context, attr);
        mMainActivity = ((DrawSMS) context);
        mMainNet = mMainActivity.GetMainNet();            
        //mDemoNet = mMainActivity.GetDemoNet();
    }

    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        int Xpad = getPaddingLeft() + getPaddingRight();
        int Ypad = getPaddingTop() + getPaddingBottom();
        ViewGroup.MarginLayoutParams Lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int W = xNew - Xpad - Lp.rightMargin - Lp.leftMargin;
        int H = yNew - Ypad - Lp.topMargin - Lp.bottomMargin;

        mSize.set(xNew, yNew);
        mSymbolSize = new Point(W / mMainNet.GetSize().x, H / mMainNet.GetSize().y);
        mFirst = new Point(mSymbolSize.x / 2, mSymbolSize.y);
        mPaint.setTextSize(Math.min(mSymbolSize.x, mSymbolSize.y));
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setShadowLayer(1.0f, 0f, 0f, Color.rgb(254, 254, 254));
        mPaint.setColor(Color.rgb(0, 0, 0));

        mPaintMove.set(mPaint);
        mPaintMove.setColor(Color.rgb(127, 127, 127));
        
        mPaintHighlight.setColor(Color.parseColor("#FFFF00"));
        
        mPixelsPerMillisecond = (double)xNew / (double)5000.;
        mLastTime = 0;
    }

    /*public void OnDrawDemoNet(Canvas canvas) {
        int OffsetX = mFirst.x;
        if (mLastTime != 0) {
            double TimeLapse = System.currentTimeMillis() - mLastTime;
            double PixelsOffset = (TimeLapse * mPixelsPerMillisecond);
            OffsetX += PixelsOffset;
        }

        for (int w = 0; w < mDemoNet.GetSize().x; w++) {
            for (int h = 0; h < mDemoNet.GetSize().y; h++) {
                mSymbol[0] = mDemoNet.mNetCharacters[w][h];

                mLastTime = System.currentTimeMillis();
                canvas.drawText(mSymbol, 0, 1, OffsetX + (mSymbolSize.x * w),
                        mFirst.y + (mSymbolSize.y * h), mPaintMove);
            }
        }
    }*/
    
    public void onDrawMoveNet(Canvas canvas){
        for (int w = 0; w < mMoveNet.GetSize().x; w++) {
            for (int h = 0; h < mMoveNet.GetSize().y; h++) {
                mSymbol[0] = mMoveNet.mNetCharacters[w][h];
                canvas.drawText(mSymbol, 0, 1, mFirst.x + (mSymbolSize.x * w),
                        mFirst.y + (mSymbolSize.y * h), mPaintMove);
            }
        }
    }
    
    public void onDrawMainNet(Canvas canvas){     
        if (mHighlightX != -1) {
            int x = mSymbolSize.x * mHighlightX;
            int y = mSymbolSize.y * mHighlightY;

            mHighlightRect.set(0, y, mSize.x, y + mSymbolSize.y);
            canvas.drawRect(mHighlightRect, mPaintHighlight);
            
            mHighlightRect.set(x, 0, x + mSymbolSize.x, mSize.y);
            canvas.drawRect(mHighlightRect, mPaintHighlight);
        }
        
        for (int w = 0; w < mMainNet.GetSize().x; w++) {
            for (int h = 0; h < mMainNet.GetSize().y; h++) {
                mSymbol[0] = mMainNet.mNetCharacters[w][h];
                canvas.drawText(mSymbol, 0, 1, mFirst.x + (mSymbolSize.x * w),
                        mFirst.y + (mSymbolSize.y * h), mPaint);
            }
        }
    }
    
    public void onDraw(Canvas canvas) {
        if (mMainActivity.GetSelectedTool() == DrawSMS.Tool.MOVE && mMoveNet != null) {
            onDrawMoveNet(canvas);
        } else {
            onDrawMainNet(canvas);
        }

        super.onDraw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mMainActivity.GetSelectedTool() == DrawSMS.Tool.MOVE) {
                MoveNet(GetIndexX(event.getX()), GetIndexY(event.getY()));
            } else {
                HighlightAt(event.getX(), event.getY());
            }

            postInvalidate();

            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mMainActivity.GetSelectedTool() != DrawSMS.Tool.MOVE) {
                HighlightAt(event.getX(), event.getY());
            }

            postInvalidate();
            mDownPoint.set(GetIndexX(event.getX()), GetIndexY(event.getY()));
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mMainActivity.IsHistoryEmpty()) {
                mMainActivity.AddHistory();
            }

            HighlightAt(-1, -1);
            SetCharAt(event.getX(), event.getY());
            mMainActivity.AddHistory();
            mMainActivity.UpdateButtons();
            postInvalidate();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            HighlightAt(-1, -1);
            postInvalidate();
        }

        return false;
    }

    int GetIndexX(float x) {
        int IndexX = (int) Math.floor(x / mSymbolSize.x);
        if (IndexX >= mMainNet.mNetCharacters.length) {
            IndexX = mMainNet.mNetCharacters.length - 1;
        }
        return IndexX;
    }

    int GetIndexY(float y) {
        int IndexY = (int) Math.floor(y / mSymbolSize.y);
        if (IndexY >= mMainNet.mNetCharacters[0].length) {
            IndexY = mMainNet.mNetCharacters[0].length - 1;
        }
        return IndexY;
    }

    void SetCharAt(float x, float y) {
        int IndexX = GetIndexX(x);
        int IndexY = GetIndexY(y);
        if (IndexX < 0 || IndexY < 0) {
            mHighlightX = -1;
            return;
        }

        if (mMainActivity.GetSelectedTool() == DrawSMS.Tool.ERASE) {
            mMainNet.mNetCharacters[IndexX][IndexY] = ' ';
        } else if (mMainActivity.GetSelectedTool() == DrawSMS.Tool.MOVE) {
            mMainNet.CopyFrom(mMoveNet);
            mMoveNet = null;
        } else {
            mMainNet.mNetCharacters[IndexX][IndexY] = mMainActivity.GetSelectedSymbol();
        }

        mMainActivity.UpdateButtons();
        postInvalidate();
    }

    private void HighlightAt(float x, float y) {
        int IndexX = GetIndexX(x);
        int IndexY = GetIndexY(y);

        mHighlightX = IndexX;
        mHighlightY = IndexY;

        if (IndexX < 0 || IndexY < 0) {
            mHighlightX = -1;
        }
    }

    private void MoveNet(float x, float y) {
        int ShiftX = (int) (mDownPoint.x - x);
        int ShiftY = (int) (mDownPoint.y - y);

        if (mMoveNet == null) {
            mMoveNet = new CNet();
        }

        mMoveNet.Move(mMainNet, ShiftX, ShiftY);
    }
}
