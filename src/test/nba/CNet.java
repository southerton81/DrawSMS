/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.nba;

import android.graphics.Point;

/**
 *
 * @author Kate
 */
public class CNet {

    char mNetCharacters[][];
    Point mSize;

    CNet(int w, int h) {
        mNetCharacters = new char[w][h];

        for (int width = 0; width < mNetCharacters.length; width++) {
            for (int height = 0; height < mNetCharacters[0].length; height++) {
                mNetCharacters[width][height] = ' ';
            }
        }
    }

    CNet(CNet anotherNet) {
        CopyFrom(anotherNet);
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
}
