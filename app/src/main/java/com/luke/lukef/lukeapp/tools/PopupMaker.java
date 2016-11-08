package com.luke.lukef.lukeapp.tools;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.luke.lukef.lukeapp.R;

/**
 * Created by Daniel on 08/11/2016.
 */

public class PopupMaker {
    private Context context;
    public PopupMaker(Context context){
        this.context = context;
    }

    public void createPopupTest(){
        Log.e("jeebem", "onClick: popup now?" );
        // Create custom dialog object
        final Dialog dialog = new Dialog(context);
        // Include dialog.xml file
        dialog.setContentView(R.layout.popup_test);
        // Set dialog title
        dialog.setTitle("Custom Dialog");
        dialog.show();


    }
}
