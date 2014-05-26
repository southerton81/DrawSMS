/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.nba;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class CNetView extends View {

    MainActivity mMainActivity;
    CNet mMainNet;
    
    CNet mMoveNet;
    Point mSymbolSize;
    Point mFirst;
    Paint mPaint = new Paint();
    Paint mPaintMove = new Paint();
    Rect  mHighlightRect = new Rect();
    boolean mFlag = false;
    char mSymbol[] = new char[1];
    int mHighlightX = -1;
    int mHighlightY = -1;
    Point mDownPoint = new Point(0,0);

    public CNetView(Context context, AttributeSet attr) {
        super(context, attr);
        mMainActivity = ((MainActivity) context);
        mMainNet = mMainActivity.GetMainNet();
    }

    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        int Xpad = getPaddingLeft() + getPaddingRight();
        int Ypad = getPaddingTop() + getPaddingBottom();
        ViewGroup.MarginLayoutParams Lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int W = xNew - Xpad - Lp.rightMargin - Lp.leftMargin;
        int H = yNew - Ypad - Lp.topMargin - Lp.bottomMargin;

        mSymbolSize = new Point(W / mMainNet.GetSize().x, H / mMainNet.GetSize().y);
        mFirst = new Point(mSymbolSize.x / 2, mSymbolSize.y);
        mPaint.setTextSize(Math.min(mSymbolSize.x, mSymbolSize.y));
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setShadowLayer(1.0f, 0f, 0f, Color.rgb(254, 254, 254));
        mPaint.setColor(Color.rgb(0, 0, 0));
        
        mPaintMove.set(mPaint);
        mPaintMove.setColor(Color.rgb(127, 127, 127));
    }

    /*protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
     super.onMeasure(widthMeasureSpec, heightMeasureSpec);
     int size = 0;
     int width = getMeasuredWidth();
     int height = getMeasuredHeight();

     if (width > height) {
     size = height;
     } else {
     size = width;
     }

     setMeasuredDimension(size, size);
     }*/
    public void onDraw(Canvas canvas) {
        for (int w = 0; w < mMainNet.GetSize().x; w++) {
            for (int h = 0; h < mMainNet.GetSize().y; h++) {
                mSymbol[0] = mMainNet.mNetCharacters[w][h];
                canvas.drawText(mSymbol, 0, 1, mFirst.x + (mSymbolSize.x * w),
                        mFirst.y + (mSymbolSize.y * h), mPaint);
            }
        }
        
        if (mHighlightX != -1) {
            int x = mSymbolSize.x * mHighlightX;
            int y = mSymbolSize.y * mHighlightY;
            
            mHighlightRect.set(x, y, x + mSymbolSize.x, y + mSymbolSize.y);
            canvas.drawRect(mHighlightRect, mPaint);
        }
        
        if (mMainActivity.GetSelectedTool() == MainActivity.Tool.MOVE) {
            if (mMoveNet != null) {
                for (int w = 0; w < mMoveNet.GetSize().x; w++) {
                    for (int h = 0; h < mMoveNet.GetSize().y; h++) {
                        mSymbol[0] = mMoveNet.mNetCharacters[w][h];
                        canvas.drawText(mSymbol, 0, 1, mFirst.x + (mSymbolSize.x * w),
                                mFirst.y + (mSymbolSize.y * h), mPaintMove);
                    }
                }
            }
        }
             
             
             
        super.onDraw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            HighlightAt(event.getX(), event.getY());
            
            if (mMainActivity.GetSelectedTool() == MainActivity.Tool.MOVE) 
                MoveNet(GetIndexX(event.getX()), GetIndexY(event.getY()));
       
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            HighlightAt(event.getX(), event.getY());
            mDownPoint.set(GetIndexX(event.getX()), GetIndexY(event.getY()));
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
           
            if (!mFlag) {
                mFlag = true;
                mMainActivity.AddHistory();
            }
            
            HighlightAt(-1, -1);
            SetCharAt(event.getX(), event.getY());
            mMainActivity.AddHistory();
            mMainActivity.UpdateButtons();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            HighlightAt(-1, -1);
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

        if (mMainActivity.GetSelectedTool() == MainActivity.Tool.ERASE) {
            mMainNet.mNetCharacters[IndexX][IndexY] = ' ';
        } 
        else if (mMainActivity.GetSelectedTool() == MainActivity.Tool.MOVE) {
            mMainNet.CopyFrom(mMoveNet);
            mMoveNet = null;
        }
        else {
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

        postInvalidate();
    }

    private void MoveNet(float x, float y) {
        int ShiftX = (int) (mDownPoint.x - x);
        int ShiftY = (int) (mDownPoint.y - y);

        if (mMoveNet == null) {
            mMoveNet = new CNet(mMainNet.GetSize().x, mMainNet.GetSize().y);
        }

        mMoveNet.Move(mMainNet, ShiftX, ShiftY);
    }
}
