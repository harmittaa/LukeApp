package com.luke.lukef.lukeapp.popups;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.SubmissionDatabase;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.UserFromServer;
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

        // set click listeners
        this.popupButtonPositive.setOnClickListener(clickListener);
        this.submitterProfileImage.setOnClickListener(clickListener);
        this.submissionImage.setOnClickListener(clickListener);
        this.submissionReportButton.setOnClickListener(clickListener);
        this.submissionImage.setOnClickListener(clickListener);
        if (!isAdminMarker) {
            getExternalSubmissionData();
        }
        getLocalSubmissionData();

        this.dialog.show();
    }

    /**
     * Creates a new AsyncTask to fetch the required submission data from the server
     */
    private void getExternalSubmissionData() {
        String[] taskParams = {this.markerId};
    }

    /**
     * Set data from cursor to dialog
     */
    private void addDataToDialog() {
        this.queryCursor.moveToFirst();
        Log.e(TAG, "addDataToDialog: size " + this.queryCursor.getCount());

        // passes if it's a submission, goes to else if admin marker
        if (this.queryCursor.getColumnIndex("submission_img_url") != -1) {
            String imgUrl = this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_img_url"));
            if (imgUrl != null && !imgUrl.isEmpty() && !imgUrl.equals("null")) {
                try {
                    imgUrl = imgUrl.trim();
                    if (!imgUrl.isEmpty()) {
                        String[] taskParams = {imgUrl};
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "addDataToDialog: illegal arg ", e);
                    this.submissionImage.setImageResource(R.drawable.no_img);
                } catch (NullPointerException e) {
                    this.submissionImage.setImageResource(R.drawable.no_img);
                    Log.e(TAG, "addDataToDialog: NPE ", e);
                }
            } else {
                // no img
                this.submissionImage.setImageResource(R.drawable.no_img);
            }
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
            this.submissionTitle.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_title")));
        } else if (this.queryCursor.getColumnIndex("admin_marker_title") != -1) {
            this.submissionTitle.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_title")));
        }

        this.submissionDatabase.closeDbConnection();
    }

    /**
     * Get data of the submission from the local cache
     */
    private void getLocalSubmissionData() {
        this.submissionDatabase = new SubmissionDatabase(this.mainActivity);
        if (isAdminMarker) {
            this.queryCursor = this.submissionDatabase.queryAdminMarkerById(this.markerId);
        } else {
            this.queryCursor = this.submissionDatabase.querySubmissionById(this.markerId);
        }
        addDataToDialog();
    }

    /**
     * Called from {@link GetSubmissionDataTask#onPostExecute(List)},
     * uses the list of category IDs and finds the correct categories from {@link SessionSingleton#getCategoryList()}
     * and fetches the images from those.
     *
     * @param strings The list of category IDs that the submission has.
     */
    private void setCategories(List<String> strings) {
        this.arrayIds = strings;
        for (String s : this.arrayIds) {
            for (Category c : SessionSingleton.getInstance().getCategoryList()) {
                if (c.getId().equals(s)) {
                    ImageView categoryImg = new ImageView(this.mainActivity);
                    categoryImg.setImageBitmap(c.getImage());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(
                            this.submissionCategoriesLinear.getHeight(),
                            this.submissionCategoriesLinear.getHeight()));
                    categoryImg.setLayoutParams(layoutParams);
                    this.submissionCategoriesLinear.addView(categoryImg);
                }
            }
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

    private class GetSubmissionData extends AsyncTask<Void,Void,Void>{

        View popupView;
        Activity activity;

        public GetSubmissionData(Activity activity, View v){
            this.popupView = v;
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
