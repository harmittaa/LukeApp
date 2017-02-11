/*
        BalticApp, for studying and tracking the condition of the Baltic sea
        and Gulf of Finland throug user submissions.
        Copyright (C) 2016  Daniel Zakharin, LuKe

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/> or
        the beginning of MainActivity.java file.

*/

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
 * Handles creating and displaying the link popup that comes up when starting the app.
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
