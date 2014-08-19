/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kurovsky.drawsms;

import android.content.SharedPreferences;
import android.graphics.Point;

public class CNet {
    public final static int mWidth = 10;
    private final static int mHeight = 10;
    char mNetCharacters[][];
    Point mSize;

    CNet() {
        mNetCharacters = new char[mWidth][mHeight];
        Clear();
    }

    CNet(CNet anotherNet) {
        CopyFrom(anotherNet);
    }

    public void Clear() {
        for (int width = 0; width < mNetCharacters.length; width++) {
            for (int height = 0; height < mNetCharacters[0].length; height++) {
                mNetCharacters[width][height] = ' ';
            }
        }
    }

    final void CopyFrom(CNet anotherNet) {
        int w = anotherNet.GetSize().x;
        int h = anotherNet.GetSize().y;

        mNetCharacters = new char[w][h];

        for (int width = 0; width < mNetCharacters.length; width++) {
            for (int height = 0; height < mNetCharacters[0].length; height++) {
                mNetCharacters[width][height] = anotherNet.mNetCharacters[width][height];
            }
        }
    }
    
    final void Move(CNet anotherNet, int x, int y) {
        for (int width = 0; width < mNetCharacters.length; width++) {
            for (int height = 0; height < mNetCharacters[0].length; height++) {
                int s_width = width + x;
                int s_height = height + y;

                if ((s_width >= 0 && s_width < anotherNet.GetSize().x)
                        && (s_height >= 0 && s_height < anotherNet.GetSize().y)) {
                    mNetCharacters[width][height] = anotherNet.mNetCharacters[s_width][s_height];
                } else {
                    mNetCharacters[width][height] = ' ';
                }
            }
        }
    }

    Point GetSize() {
        if (mSize == null) {
            mSize = new Point(mNetCharacters.length, mNetCharacters[0].length);
        }
        return mSize;
    }
    
    final void SetLine(String line, int lineNum) {
        for (int width = 0; width < mNetCharacters.length; width++) {
            if (width < line.length()) {
                mNetCharacters[width][lineNum] = line.charAt(width);
            } else {
                mNetCharacters[width][lineNum] = ' ';
            }
        }
    }

    final String GetLine(int lineNum) {
        StringBuilder Line = new StringBuilder();

        for (int width = 0; width < mNetCharacters.length; width++) {
            Line.append(mNetCharacters[width][lineNum]);
        }
        return Line.toString();
    }

    public boolean IsEmpty() {
        for (int width = 0; width < mNetCharacters.length; width++) {
            for (int height = 0; height < mNetCharacters[0].length; height++) {
                if (mNetCharacters[width][height] != ' ') {
                    return false;
                }
            }
        }

        return true;
    }

    void Store(SharedPreferences.Editor prefsEditor, String name) {
        StringBuilder SB = new StringBuilder();
        for (int width = 0; width < mNetCharacters.length; width++) {
            for (int height = 0; height < mNetCharacters[0].length; height++) {
                SB.append(mNetCharacters[width][height]);
            }
        }
        String string = SB.toString();
        prefsEditor.putString(name, string);
    }

    void Restore(SharedPreferences prefs, String name) {
        String string = prefs.getString(name, "");

        int charIndex = 0;
        for (int width = 0; width < mNetCharacters.length; width++) {
            for (int height = 0; height < mNetCharacters[0].length; height++) {
                if (charIndex < string.length()) {
                    mNetCharacters[width][height] = string.charAt(charIndex);
                } else {
                    mNetCharacters[width][height] = ' ';
                }
                charIndex++;
            }
        }
    }
}
