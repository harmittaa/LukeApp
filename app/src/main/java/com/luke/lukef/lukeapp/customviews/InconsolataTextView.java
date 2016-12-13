package com.luke.lukef.lukeapp.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Daniel on 13/12/2016.
 */

public class InconsolataTextView extends TextView {
    public InconsolataTextView(Context context) {
        super(context);
    }

    public InconsolataTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InconsolataTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        Typeface tff = Typeface.createFromAsset(getContext().getAssets(),  "fonts/inconsolata_regular.ttf");
        this.setTypeface(tff ,1);
    }
}
