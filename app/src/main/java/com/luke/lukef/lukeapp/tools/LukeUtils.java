package com.luke.lukef.lukeapp.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.luke.lukef.lukeapp.model.SubmissionFromServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Daniel on 05/12/2016.
 */

public class LukeUtils {
    private static final String TAG = "LukeUtils";

    public static String bitapToBase64String(Bitmap bmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static ArrayList<SubmissionFromServer> parseSubmissionsFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<SubmissionFromServer> submissions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            SubmissionFromServer submission = new SubmissionFromServer();
            if (jsonObject.has("id")) {
                submission.setSubmissionId(jsonObject.getString("id"));
            }
            if (jsonObject.has("image_url")) {
                submission.setImageUrl(jsonObject.getString("image_url"));
            }
            if (jsonObject.has("title")) {
                submission.setTitle(jsonObject.getString("title"));
            }
            if (jsonObject.has("description")) {
                submission.setDescription(jsonObject.getString("description"));
            }
            if (jsonObject.has("submittedId")) {
                submission.setSubmitterId(jsonObject.getString("submitterId"));
            }
            if (jsonObject.has("date")) {
                submission.setDate(jsonObject.getString("date"));
            }
            if (jsonObject.has("categoryId")) {
                submission.setSubmissionCategoryList(parseStringsFromJsonArray(jsonObject.getJSONArray("categoryId")));
            }
            if (jsonObject.has("latitude") && jsonObject.has("longitude")) {
                Location location = new Location("jea");
                location.setLatitude(jsonObject.getDouble("latitude"));
                location.setLongitude(jsonObject.getDouble("longitude"));
                submission.setLocation(location);
            }
            submissions.add(submission);
        }
        return submissions;
    }

    public static ArrayList<String> parseStringsFromJsonArray(JSONArray toParse) {
        ArrayList<String> strings = new ArrayList<>();
        try {
            for (int i = 0; i < toParse.length(); i++) {
                String jsonString = toParse.getString(i);
                strings.add(jsonString);
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseStringsFromJsonArray: ", e);
        }
        return strings;
    }

    /**
     * Parses date from MS to the defined format
     *
     * @param submission_date The amount of milliseconds from the Jan 1, 1970 GMT to the desired date
     * @return Date as String in defined format
     */
    public static String parseDateFromMillis(long submission_date) {
        Date date = new Date(submission_date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);
        format.applyPattern("hh:mm dd/MM/yyyy");
        return format.format(date);
    }

    public static String parseDateFromString(String dateToParse) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.CHINA);
            Date date = null;
            date = format.parse(dateToParse);
            format.applyPattern("hh:mm dd/MM/yyyy");
            return format.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "parseDateFromString: ", e);
            return null;
        }

    }


}
