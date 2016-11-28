package com.luke.lukef.lukeapp.tools;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;

/**
 * Created by Daniel on 08/11/2016.
 */

public class PopupMaker {
    private MainActivity mainActivity;
    public PopupMaker(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    public void createPopupTest(){
        Log.e("jeebem", "onClick: popup now?" );
        // Create custom dialog object
        final Dialog dialog = new Dialog(mainActivity);
        // Include dialog.xml file
        dialog.setContentView(R.layout.popup_test);
        // Set dialog title
        dialog.setTitle("Custom Dialog");
        dialog.findViewById(R.id.popup_button_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.submissionSubmitterProfileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_PROFILE,null);
                dialog.dismiss();
            }
        });
        dialog.show();


    }
}
