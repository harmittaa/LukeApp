package com.luke.lukef.lukeapp.popups;

import android.app.Activity;
import android.app.Dialog;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Link;

/**
 * Created by Daniel on 13/12/2016.
 */

public class LinkPopup {
    private  Dialog dialog;
    private Activity activity;
    private Link link;

    public LinkPopup(Activity activity, Link link){
        this.activity = activity;
        this.link = link;
        this.dialog = new Dialog(activity);
    }

    public void setupPopup(){
        this.dialog.setContentView(R.layout.popup_link);
    }
}
