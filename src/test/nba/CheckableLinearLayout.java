/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.nba;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 *
 * @author Kate
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

    private boolean checked = false;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    public CheckableLinearLayout(Context context) {
        super(context);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) 
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean _checked) {
        checked = _checked;
        refreshDrawableState();
    } 

    public void toggle() {
        setChecked(!checked);
    }
}
