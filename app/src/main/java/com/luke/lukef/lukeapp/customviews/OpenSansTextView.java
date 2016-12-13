package com.luke.lukef.lukeapp.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Daniel on 13/12/2016.
 */

public class OpenSansTextView extends TextView {
    public OpenSansTextView(Context context) {
        super(context);
    }

    public OpenSansTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OpenSansTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setTypeface(Typeface tf, int style) {
        Typeface tff = Typeface.createFromAsset(getContext().getAssets(),  "fonts/open_sans_regular.ttf");
        this.setTypeface(tff ,1);
    }
}
