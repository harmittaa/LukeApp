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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.Rank;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.model.UserFromServer;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;
import com.luke.lukef.lukeapp.tools.LukeUtils;
import com.luke.lukef.lukeapp.tools.SubmissionDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Daniel on 15/12/2016.
 */

/**
 * Popup to be displayed on the {@link com.luke.lukef.lukeapp.fragments.MapViewFragment MapViewFragment} when an adminmarker is clicked. Handles setting up all the view of the popup.
 */

public class AdminMarkerPopup implements View.OnClickListener {
    private Cursor queryCursor;
    private String adminMarkerId;
    private Dialog dialog;
    private MainActivity mainActivity;
    private ImageView submissionImage;
    private ImageButton popupButtonPositive;
    private TextView submissionDescription;
    private TextView submissionDate;
    private ProgressBar loadingSpinny;
    private TextView submissionSubmitterName;
    private View mainView;
    private SubmissionDatabase submissionDatabase;
    private TextView submissionTitle;

    public AdminMarkerPopup(String adminMarkerId, MainActivity mainActivity) {
        this.adminMarkerId = adminMarkerId;
        this.mainActivity = mainActivity;
    }

    public void createPopupAdmin() {

        this.dialog = new Dialog(mainActivity);
        // Include dialog.xml file
        this.dialog.setContentView(R.layout.popup_admin_marker);

        // find views
        this.submissionImage = (ImageView) this.dialog.findViewById(R.id.submissionImageMain);
        this.submissionDescription = (TextView) this.dialog.findViewById(R.id.reportDescription);
        this.submissionSubmitterName = (TextView) this.dialog.findViewById(R.id.submissionSubmitterName);
        this.submissionDate = (TextView) this.dialog.findViewById(R.id.submissionDate);
        this.popupButtonPositive = (ImageButton) this.dialog.findViewById(R.id.popup_button_positive);
        this.loadingSpinny = (ProgressBar) this.dialog.findViewById(R.id.progressBarSubmissionPopup);
        this.mainView = this.dialog.findViewById(R.id.popupMainContent);
        this.submissionTitle = (TextView) this.dialog.findViewById(R.id.submissionTitle);

        // set click listeners
        this.popupButtonPositive.setOnClickListener(this);
        this.submissionImage.setOnClickListener(this);
        this.submissionImage.setOnClickListener(this);

        getLocalSubmissionData();
        this.dialog.show();
    }


    /**
     * Get data of the submission from the local cache
     */
    private void getLocalSubmissionData() {
        submissionDatabase = new SubmissionDatabase(this.mainActivity);
        this.queryCursor = submissionDatabase.queryAdminMarkerById(this.adminMarkerId);
        addDataToDialog();
    }


    /**
     * Set data from cursor to dialog
     */
    private void addDataToDialog() {
        this.queryCursor.moveToFirst();

        if (this.queryCursor.getColumnIndex("admin_marker_description") != -1) {
            this.submissionDescription.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_description")));
        }

        if (this.queryCursor.getColumnIndex("admin_marker_date") != -1) {
            this.submissionDate.setText(LukeUtils.parseDateFromMillis(this.queryCursor.getLong(this.queryCursor.getColumnIndexOrThrow("admin_marker_date"))));
        }

        if (this.queryCursor.getColumnIndex("admin_marker_title") != -1) {
            this.submissionTitle.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_title")));
        }

        if (this.queryCursor.getColumnIndex("admin_marker_owner") != -1) {
            this.submissionSubmitterName.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_owner")));

        }

        submissionDatabase.closeDbConnection();
        loadingSpinny.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.popup_button_positive:
                dialog.dismiss();
        }
    }



}



