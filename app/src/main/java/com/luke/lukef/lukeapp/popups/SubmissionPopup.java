package com.luke.lukef.lukeapp.popups;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.tools.SubmissionDatabase;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;

import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.model.UserFromServer;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;
import com.luke.lukef.lukeapp.tools.LukeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Handles showing submission data when submission is clicked on the map
 */
public class SubmissionPopup {
    private static final String TAG = "SubmissionPopup";
    private MainActivity mainActivity;
    private SubmissionDatabase submissionDatabase;
    private final Dialog dialog;
    private Cursor queryCursor;
    private String markerId;
    private String imageUrl;
    private boolean isAdminMarker;
    private List<String> arrayIds;
    private LinearLayout submissionCategoriesLinear;
    private ImageView submissionImage;
    private ImageView submitterProfileImage;
    private ImageButton popupButtonPositive;
    private ImageButton submissionReportButton;
    private TextView submissionDescription;
    private TextView submissionSubmitterName;
    private TextView submissionSubmitterRank;
    private TextView submissionDate;
    private TextView submissionTitle;
    private View.OnClickListener clickListener;
    private Bitmap mainImageBitmap;
    private String userId;
    private ProgressBar loadingSpinny;
    private View mainView;

    public SubmissionPopup(MainActivity mainActivity, View.OnClickListener clickListener) {
        this.mainActivity = mainActivity;
        this.dialog = new Dialog(mainActivity);
        this.clickListener = clickListener;
    }


    public void dismissPopup() {
        this.dialog.dismiss();
    }

    public void hidePopup() {
        this.dialog.hide();
    }

    public void unHidePopup() {
        if (this.dialog != null) {
            dialog.show();
        }
    }

    public void createPopupTest(String markerId, boolean isAdminMarker) {
        this.markerId = markerId;
        this.isAdminMarker = isAdminMarker;

        // Include dialog.xml file
        this.dialog.setContentView(R.layout.popup_submission);

        // find views
        this.submissionImage = (ImageView) this.dialog.findViewById(R.id.submissionImageMain);
        this.submitterProfileImage = (ImageView) this.dialog.findViewById(R.id.submissionSubmitterProfileImage);
        this.submissionDescription = (TextView) this.dialog.findViewById(R.id.reportDescription);
        this.submissionSubmitterName = (TextView) this.dialog.findViewById(R.id.submissionSubmitterName);
        this.submissionSubmitterRank = (TextView) this.dialog.findViewById(R.id.submissionSubmitterRank);
        this.submissionTitle = (TextView) this.dialog.findViewById(R.id.submissionTitle);
        this.submissionDate = (TextView) this.dialog.findViewById(R.id.submissionDate);
        this.popupButtonPositive = (ImageButton) this.dialog.findViewById(R.id.popup_button_positive);
        this.submissionReportButton = (ImageButton) this.dialog.findViewById(R.id.submissionReportButton);
        this.submissionCategoriesLinear = (LinearLayout) this.dialog.findViewById(R.id.submissionCategoriesLinear);
        this.loadingSpinny = (ProgressBar) this.dialog.findViewById(R.id.progressBarSubmissionPopup);
        this.mainView = this.dialog.findViewById(R.id.popupMainContent);

        // set click listeners
        this.popupButtonPositive.setOnClickListener(clickListener);
        this.submitterProfileImage.setOnClickListener(clickListener);
        this.submissionImage.setOnClickListener(clickListener);
        this.submissionReportButton.setOnClickListener(clickListener);
        this.submissionImage.setOnClickListener(clickListener);
        getLocalSubmissionData();

        this.dialog.show();
        if (!this.isAdminMarker) {
            GetSubmissionData getSubmissionData = new GetSubmissionData(mainActivity, this);
            getSubmissionData.execute();
        } else {
            this.loadingSpinny.setVisibility(View.GONE);
            this.mainView.setVisibility(View.VISIBLE);

        }
    }

    /**
     * Set data from cursor to dialog
     */
    private void addDataToDialog() {
        this.queryCursor.moveToFirst();
        Log.e(TAG, "addDataToDialog: size " + this.queryCursor.getCount());

        // passes if it's a submission, goes to else if admin marker
        if (this.queryCursor.getColumnIndex("submission_img_url") != -1) {

        } else {
            Log.e(TAG, "addDataToDialog: admin marker, not setting image");
            this.submissionImage.setImageResource(R.drawable.admin_marker);
        }

        if (this.queryCursor.getColumnIndex("submission_description") != -1) {
            this.submissionDescription.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_description")));
        } else if (this.queryCursor.getColumnIndex("admin_marker_description") != -1) {
            this.submissionDescription.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_description")));
        }

        if (this.queryCursor.getColumnIndex("submission_date") != -1) {
            this.submissionDate.setText(LukeUtils.parseDateFromMillis(this.queryCursor.getLong(this.queryCursor.getColumnIndexOrThrow("submission_date"))));
        } else if (this.queryCursor.getColumnIndex("admin_marker_date") != -1) {
            this.submissionDate.setText(LukeUtils.parseDateFromMillis(this.queryCursor.getLong(this.queryCursor.getColumnIndexOrThrow("admin_marker_date"))));
        }

        if (this.queryCursor.getColumnIndex("submission_title") != -1) {
            String title = this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_title"));
            if (!TextUtils.isEmpty(title)) {
                this.submissionTitle.setText(title);
            }
        } else if (this.queryCursor.getColumnIndex("admin_marker_title") != -1) {
            this.submissionTitle.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_title")));
        }

        if (this.queryCursor.getColumnIndex("admin_marker_owner") != -1) {
            this.submissionSubmitterName.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_owner")));
            this.submissionSubmitterRank.setText("");
        }

        this.submissionDatabase.closeDbConnection();
    }

    /**
     * Get data of the submission from the local cache
     */
    private void getLocalSubmissionData() {
        this.submissionDatabase = new SubmissionDatabase(this.mainActivity);
        if (this.isAdminMarker) {
            this.queryCursor = this.submissionDatabase.queryAdminMarkerById(this.markerId);
        } else {
            this.queryCursor = this.submissionDatabase.querySubmissionById(this.markerId);
        }
        addDataToDialog();
    }


    /**
     * Adds imageviews to the categories section of the popup, with the thumbnails of the categories.
     * Dimensions of the parent view can only be retreived once they are drawn, so a GlobalLayoutListener is needed.
     *
     * @param categories list of categories whose images are to be added to the category list
     */
    private void setCategories(List<Category> categories) {
        for (Category c : categories) {
            final ImageView categoryImg = new ImageView(this.mainActivity);
            categoryImg.setImageBitmap(c.getImage());
            final LinearLayout.LayoutParams[] layoutParams = new LinearLayout.LayoutParams[1];
            this.submissionCategoriesLinear.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layoutParams[0] = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(
                            SubmissionPopup.this.submissionCategoriesLinear.getHeight(),
                            SubmissionPopup.this.submissionCategoriesLinear.getHeight()));
                    categoryImg.setLayoutParams(layoutParams[0]);
                    if (categoryImg.getParent() != null) {
                        ((ViewGroup) categoryImg.getParent()).removeView(categoryImg);
                    }
                    SubmissionPopup.this.submissionCategoriesLinear.addView(categoryImg);
                }
            });
        }
    }


    /**
     * Sets the provided bitmap into the ImageView view.
     *
     * @param bitmap The bitmap for the submission.
     */
    private void setSubmissionImage(Bitmap bitmap) {
        this.submissionImage.setImageBitmap(bitmap);
    }

    public Bitmap getMainImageBitmap() {
        return mainImageBitmap;
    }

    public void setMainImageBitmap(Bitmap mainImageBitmap) {
        this.mainImageBitmap = mainImageBitmap;
    }

    public String getSubmissionID() {
        return this.markerId;
    }

    public void setSubmissionID(String submissionID) {
        this.markerId = submissionID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Inner AsyncTask class to fetch submission data from the server, includes user and submission images
     * as well as categories.
     */
    private class GetSubmissionData extends AsyncTask<Void, Void, Void> {

        Activity activity;
        SubmissionPopup submissionPopup;
        Bitmap submissionImage;
        Bitmap submitterImage;
        String submitterName;
        List<Category> categories;

        GetSubmissionData(Activity activity, SubmissionPopup submissionPopup) {
            this.activity = activity;
            this.submissionPopup = submissionPopup;
            this.categories = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... params) {
            final LukeNetUtils lukeNetUtils = new LukeNetUtils(activity);
            final Submission s = lukeNetUtils.getSubmissionFromId(this.submissionPopup.markerId);
            if (s != null) {
                if (!TextUtils.isEmpty(s.getImageUrl()) && !s.getImageUrl().equals("null")) {
                    try {
                        this.submissionImage = lukeNetUtils.getBitmapFromURL(s.getImageUrl());
                        setMainImageBitmap(this.submissionImage);
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e(TAG, "doInBackground: ", e);
                    }

                }
                if (!TextUtils.isEmpty(s.getSubmitterId()) && !s.getSubmitterId().equals("null")) {
                    submissionPopup.setUserId(s.getSubmitterId());
                }
                this.categories = LukeUtils.getCategoryObjectsFromSubmission(s);
                UserFromServer userFromServer = null;
                try {
                    userFromServer = lukeNetUtils.getUserFromUserId(s.getSubmitterId());
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "doInBackground: ", e);
                }
                if (userFromServer != null) {
                    try {
                        this.submitterImage = lukeNetUtils.getBitmapFromURL(userFromServer.getImageUrl());
                        this.submitterName = userFromServer.getUsername();
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e(TAG, "doInBackground: ", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO: 12/12/2016 hide spinny thing and show content AFTER setting values
                    if (submissionImage != null) {
                        submissionPopup.submissionImage.setImageBitmap(submissionImage);
                    } else {
                        submissionPopup.submissionImage.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.no_img));
                    }
                    if (submitterImage != null) {
                        submissionPopup.submitterProfileImage.setImageBitmap(submitterImage);
                    } else {
                        submissionPopup.submitterProfileImage.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.luke_default_profile_pic));
                    }
                    if (categories.size() > 0) {
                        setCategories(categories);
                    }
                    if (!isAdminMarker) {
                        submissionPopup.submissionSubmitterName.setText(submitterName);
                    }
                    submissionPopup.loadingSpinny.setVisibility(View.GONE);
                    submissionPopup.mainView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

}


