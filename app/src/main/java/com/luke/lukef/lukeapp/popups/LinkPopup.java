package com.luke.lukef.lukeapp.popups;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Link;

/**
 * Created by Daniel on 13/12/2016.
 */

public class LinkPopup {
    private  Dialog dialog;
    private Activity activity;
    private Link link;
    private TextView linkDesc;
    private TextView linkLink;
    private ImageButton linkDismissButton;

    public LinkPopup(Activity activity, Link link){
        this.activity = activity;
        this.link = link;
        this.dialog = new Dialog(activity);
        setupPopup();
    }

    private void setupPopup(){
        this.dialog.setContentView(R.layout.popup_link);
        this.linkDesc = (TextView)this.dialog.findViewById(R.id.link_popup_desc);
        this.linkLink = (TextView)this.dialog.findViewById(R.id.link_popup_link);
        this.linkDismissButton = (ImageButton)this.dialog.findViewById(R.id.link_popup_dismiss);

        this.linkDesc.setText(link.getDescription());
        this.linkLink.setText((Html.fromHtml("<a href=\""+link.getLink() + "\">"+link.getTitle()+"</a>")));
        //this.linkLink.setText("This shows and <a href=\"" + link.getLink() + "\">so does this</a> but this doesn\\'t");
        this.linkDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        this.linkLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link.getLink()));
                activity.startActivity(i);
            }
        });
        dialog.show();
    }


}
