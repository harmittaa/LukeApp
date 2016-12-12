package com.luke.lukef.lukeapp.tools;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.model.Submission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility methods to be used from anywhere
 */
public class LukeUtils {
    private static final String TAG = "LukeUtils";
    private static final String noInternet = "Making a submission requires an Internet Connection. Enable Internet now?";
    private static final String noGps = "Making a submission requires GPS to be enabled. Enable GPS now?";

    /**
     * Turns a Bitmap object into a Base64 type String.
     *
     * @param bitmap Bitmap that should be parsed.
     * @return String of the Bitmap in Base64 enconding.
     */
    public static String bitmapToBase64String(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Parses submissions from JSONArray.
     *
     * @param jsonArray JSONArray with Submission JSONObjects.
     * @return ArrayList of the Submission objects.
     * @throws JSONException
     */
    static ArrayList<Submission> parseSubmissionsFromJsonArray(JSONArray jsonArray) throws JSONException {
        ArrayList<Submission> submissions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Submission submission = new Submission();
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

    /**
     * Parses all strings inside the given JSONArray and adds them into a list.
     *
     * @param jsonArrayToParse The JSONArray that should be parsed.
     * @return ArrayList of Strings.
     */
    private static ArrayList<String> parseStringsFromJsonArray(JSONArray jsonArrayToParse) {
        ArrayList<String> strings = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArrayToParse.length(); i++) {
                String jsonString = jsonArrayToParse.getString(i);
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

    /**
     * Parses the given String into a date object and outputs it in a different pattern.
     *
     * @param dateToParse Date as a String in the following pattern: <b>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</b>.
     * @return The given date, but in the following format: <b>hh:mm dd/MM/yyyy</b>.
     */
    public static String parseDateFromString(String dateToParse) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date;
            date = format.parse(dateToParse);
            format.applyPattern("hh:mm dd/MM/yyyy");
            return format.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "parseDateFromString: ", e);
            return null;
        }
    }

    /**
     * Checks the current GPS status, if GPS is not enabled then calls
     * {@link LukeUtils#alertDialogBuilder(Context, String, String)} to create a prompt for the user
     * to enable GPS.
     *
     * @param context Context, needed to create the alert.
     * @return <b>true</b> if the GPS is enabled, <b>false</b> if it's not.
     */
    public static boolean checkGpsStatus(Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertDialogBuilder(context, noGps, android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Checks the current Internet status, if Internet is not enabled then calls
     * {@link LukeUtils#alertDialogBuilder(Context, String, String)} to create a prompt for the user
     * to enable Internet.
     *
     * @param context Context, needed to create the alert.
     * @return <b>true</b> if the GPS is enabled, <b>false</b> if it's not.
     */
    public static boolean checkInternetStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            alertDialogBuilder(context, noInternet, android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            return false;
        }
    }

    /**
     * Creates an alert with <b>Yes</b> and <b>No</b> buttons.
     *
     * @param context   Context, needed to start the given activity.
     * @param alertText The text to show in the alert.
     * @param settings  The required android.provider.settings to open if user clicks <b>Yes</b>.
     */
    private static void alertDialogBuilder(final Context context, String alertText, final String settings) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        context.startActivity(new Intent(settings));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}
