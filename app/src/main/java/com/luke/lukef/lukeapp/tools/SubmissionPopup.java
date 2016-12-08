package com.luke.lukef.lukeapp.tools;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


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
        new GetSubmissionDataTask().execute(taskParams);
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
                        new GetSubmissionImage().execute(taskParams);
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
            this.submissionDate.setText(parseDate(this.queryCursor.getLong(this.queryCursor.getColumnIndexOrThrow("submission_date"))));
        } else if (this.queryCursor.getColumnIndex("admin_marker_date") != -1) {
            this.submissionDate.setText(parseDate(this.queryCursor.getLong(this.queryCursor.getColumnIndexOrThrow("admin_marker_date"))));
        }

        if (this.queryCursor.getColumnIndex("submission_title") != -1) {
            this.submissionTitle.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_title")));
        } else if (this.queryCursor.getColumnIndex("admin_marker_title") != -1) {
            this.submissionTitle.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("admin_marker_title")));
        }

        this.submissionDatabase.closeDbConnection();
    }

    /**
     * Parses date from MS to the defined format
     *
     * @param submission_date The amount of milliseconds from the Jan 1, 1970 GMT to the desired date
     * @return Date as String in defined format
     */
    private String parseDate(long submission_date) {
        Date date = new Date(submission_date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        format.applyPattern("hh:mm dd/MM/yyyy");
        return format.format(date);
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

    /**
     * Used to fetch submission data from the server, pass submission ID as first parameter, return values is a List<String>
     * that includes the category IDs.
     */
    private class GetSubmissionDataTask extends AsyncTask<String, Void, List<String>> {
        private String jsonString;
        private HttpURLConnection httpURLConnection;

        @Override
        protected List<String> doInBackground(String... params) {
            try {
                URL lukeURL = new URL(mainActivity.getString(R.string.report_id_url) + params[0]);
                httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
                if (httpURLConnection.getResponseCode() == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    jsonString = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    jsonString = stringBuilder.toString();
                    bufferedReader.close();

                } else {
                    //TODO: if error do something else, ERROR STREAM
                    mainActivity.makeToast("Error");
                    Log.e(TAG, "response code something else");
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception with fetching data: " + e.toString());
            }

            List<String> categoryIds = new ArrayList<>();
            if (jsonString != null && !jsonString.equals("[]")) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if (jsonObject.has("categoryId")) {
                        JSONArray categoryArray = jsonObject.getJSONArray("categoryId");
                        for (int i = 0; i < categoryArray.length(); i++) {
                            categoryIds.add(categoryArray.get(i).toString());
                        }
                    }
                    if (jsonObject.has("submitterId")) {
                        getSubmitterData(jsonObject.getString("submitterId"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception parsing JSONObject from string: ", e);
                }
            }
            Log.e(TAG, "doInBackground: size of categories " + categoryIds.size());
            return categoryIds;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            // set the categories
            setCategories(strings);
        }
    }

    private void getSubmitterData(String userId) {
        String jsonString = "";
        try {
            URL lukeURL = new URL("http://www.balticapp.fi/lukeA/user?id=" + userId);
            HttpURLConnection httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                jsonString = stringBuilder.toString();
                bufferedReader.close();
                Log.e(TAG, "getSubmitterData: jsonString " + jsonString);

            } else {
                //TODO: if error do something else, ERROR STREAM
                mainActivity.makeToast("Error");
                Log.e(TAG, "response code something else");
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception with fetching data: " + e.toString());
        }

        if (!TextUtils.isEmpty(jsonString)) {
            try {
                final JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("image_url")) {
                    Bitmap bitmap = null;
                    imageUrl = jsonObject.getString("image_url");
                    try {
                        InputStream in = new URL(imageUrl).openStream();
                        bitmap = BitmapFactory.decodeStream(in);
                        final Bitmap finalBitmap = bitmap;
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "run: Setting image");
                                submitterProfileImage.setImageBitmap(finalBitmap);
                            }
                        });
                    } catch (IOException e) {
                        Log.e(TAG, "doInBackground: Exception parsing image ", e);
                    }
                } else {
                    Log.e(TAG, "getSubmitterData: NO USER IMG");
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "run: Setting image");
                            submitterProfileImage.setImageResource(R.drawable.admin_marker);
                        }
                    });
                }

                if (jsonObject.has("username")) {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "run: Setting image");
                            try {
                                submissionSubmitterName.setText(jsonObject.getString("username"));
                            } catch (JSONException e) {
                                submissionSubmitterName.setText("not availble");
                                Log.e(TAG, "run: error parsing username ", e );
                            }
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the submission's image from the provided URL (first parameter)
     */
    private class GetSubmissionImage extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: Exception parsing image ", e);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            setSubmissionImage(bitmap);
            SubmissionPopup.this.mainImageBitmap = bitmap;
        }
    }


}
