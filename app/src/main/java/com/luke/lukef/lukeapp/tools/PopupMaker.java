package com.luke.lukef.lukeapp.tools;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.NewUserActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.SubmissionDatabase;
import com.luke.lukef.lukeapp.model.SessionSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Daniel on 08/11/2016.
 */

public class PopupMaker {
    private static final String TAG = "PopupMaker";
    private MainActivity mainActivity;
    private SubmissionDatabase submissionDatabase;
    private String submissionId;
    private String imageUrl;
    private final Dialog dialog;
    private View.OnClickListener clickListener;
    private Cursor queryCursor;
    private List<String> arrayIds;

    private ImageView submissionImage;
    private ImageView submitterProfileImage;
    private ImageButton popupButtonPositive;
    private ImageButton submissionReportButton;
    private TextView submissionDescription;
    private TextView submissionSubmitterName;
    private TextView submissionSubmitterRank;
    private TextView submissionDate;
    private TextView submissionTitle;

    public PopupMaker(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.dialog = new Dialog(mainActivity);
        // Create custom dialog object
        setupListeners();
    }

    private void setupListeners() {
        clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.popup_button_positive:
                        break;
                    case R.id.submissionReportButton:
                        break;
                    case R.id.submissionSubmitterProfileImage:
                        mainActivity.fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_PROFILE, null);
                        dialog.dismiss();
                        break;
                    case R.id.submissionImageMain:
                        break;
                }
            }
        };
    }

    public void createPopupTest(String submissionId) {
        this.submissionId = submissionId;

        // Include dialog.xml file
        this.dialog.setContentView(R.layout.popup_test);

        // find views
        this.submissionImage = (ImageView) dialog.findViewById(R.id.submissionImageMain);
        this.submitterProfileImage = (ImageView) dialog.findViewById(R.id.submissionSubmitterProfileImage);
        this.submissionDescription = (TextView) dialog.findViewById(R.id.reportDescription);
        this.submissionSubmitterName = (TextView) dialog.findViewById(R.id.submissionSubmitterName);
        this.submissionSubmitterRank = (TextView) dialog.findViewById(R.id.submissionSubmitterRank);
        this.submissionTitle = (TextView) dialog.findViewById(R.id.submissionTitle);
        this.submissionDate = (TextView) dialog.findViewById(R.id.submissionDate);
        this.popupButtonPositive = (ImageButton) dialog.findViewById(R.id.popup_button_positive);
        this.submissionReportButton = (ImageButton) dialog.findViewById(R.id.submissionReportButton);

        // set click listeners
        this.popupButtonPositive.setOnClickListener(clickListener);
        this.submitterProfileImage.setOnClickListener(clickListener);
        this.submissionImage.setOnClickListener(clickListener);
        this.submissionReportButton.setOnClickListener(clickListener);

        getExternalSubmissionData();
        getLocalSubmissionData();

        dialog.show();
    }

    private void getExternalSubmissionData() {
        String[] taskParams = {this.submissionId};
        new GetSubmissionDataTask().execute(taskParams);
    }

    /**
     * Set data from cursor to dialog
     */
    private void addDataToDialog() {
        this.queryCursor.moveToFirst();
        Log.e(TAG, "addDataToDialog: queryCursor getColumnIndex " + this.queryCursor.getColumnIndex("submission_description"));

        if (this.queryCursor.getColumnIndex("submission_img_url") != -1) {
            try {

                String imgUrl = this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_img_url"));
                Log.e(TAG, "addDataToDialog: " + this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_img_url")));
                imgUrl = imgUrl.trim();
                if (!imgUrl.isEmpty()) {
                    String[] taskParams = {imgUrl};
                    new GetSubmissionImage().execute(taskParams);
                }
            } catch (IllegalArgumentException e) {
                // TODO: 02/12/2016 SET DEFAULT IMAGE
                Log.e(TAG, "addDataToDialog: illegal arg ", e);
            } catch (NullPointerException e) {
                // TODO: 02/12/2016 same as before
                Log.e(TAG, "addDataToDialog: NPE ", e);
            }
        }

        if (this.queryCursor.getColumnIndex("submission_description") != -1) {
            this.submissionDescription.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_description")));
        }
        if (this.queryCursor.getColumnIndex("submission_date") != -1) {
            this.submissionDate.setText(parseDate(this.queryCursor.getLong(this.queryCursor.getColumnIndexOrThrow("submission_date"))));
        }
        if (this.queryCursor.getColumnIndex("submission_title") != -1) {
            this.submissionTitle.setText(this.queryCursor.getString(this.queryCursor.getColumnIndexOrThrow("submission_title")));
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
        this.queryCursor = this.submissionDatabase.querySubmissionById(this.submissionId);
        addDataToDialog();
    }

    private void setCategories(List<String> strings) {
        this.arrayIds = strings;
        // TODO: 02/12/2016 Start getting the category images from the server here
    }

    private void setSubmissionImage(Bitmap bitmap) {
        this.submissionImage.setImageBitmap(bitmap);
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
                    Log.e(TAG, "doInBackground: JSON string " + jsonString);
                    bufferedReader.close();

                } else {
                    //TODO: if error do something else, ERROR STREAM
                    Log.e(TAG, "response code something else");
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception with fetching data: " + e.toString());
            }

            List<String> categoryIds = new ArrayList<>();
            if (jsonString != null) {

                try {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    if (jsonObject.has("categoryId")) {
                        JSONArray categoryArray = jsonObject.getJSONArray("categoryId");
                        for (int i = 0; i < categoryArray.length(); i++) {
                            categoryIds.add(categoryArray.get(i).toString());
                        }
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

    private class GetSubmissionImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
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
        }
    }
}
