package com.luke.lukef.lukeapp.tools;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;

import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.Link;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.model.UserFromServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
            Submission submission = parseSubmissionFromJsonObject(jsonObject);
            submissions.add(submission);
        }
        return submissions;
    }


    public static Submission parseSubmissionFromJsonObject(JSONObject jsonObject) throws JSONException {
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
        if (jsonObject.has("submitterId")) {
            submission.setSubmitterId(jsonObject.getString("submitterId"));
        }
        return submission;
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

    public static ArrayList<Category> getCategoryObjectsFromSubmission(Submission submission) {
        ArrayList<Category> categories = new ArrayList<>();
        for (String s : submission.getSubmissionCategoryList()) {
            for (Category c : SessionSingleton.getInstance().getCategoryList()) {
                if (c.getId().equals(s)) {
                    categories.add(c);
                }
            }
        }
        return categories;
    }

    /**
     * Parses {@link com.luke.lukef.lukeapp.model.Category} objects from the provided <code>JSONArray</code>.
     * Compares the fetched categories to the existing categories, adds new discards old.
     *
     * @param jsonArr The JSONArray fetched from server.
     */
    public static ArrayList<Category> getCategoryObjectsFromJsonArray(JSONArray jsonArr) throws JSONException {
        ArrayList<Category> tempCategoryList = new ArrayList<>();
        for (int i = 0; i < jsonArr.length(); i++) {
            JSONObject jsonCategory = jsonArr.getJSONObject(i);
            // check that the object has ID tag
            if (jsonCategory.has("id")) {
                Boolean found = false;
                // loop through the SessionSingleton's Categories list and see if the category is already there
                for (Category ca : SessionSingleton.getInstance().getCategoryList()) {
                    if (ca.getId().equals(jsonCategory.getString("id"))) {
                        found = true;
                    }
                }
                // if the category doesn't exist yet on the list, then create it and add it to temp list
                if (!found) {
                    Category c = new Category();
                    c.setId(jsonCategory.getString("id"));
                    if (jsonCategory.has("description")) {
                        c.setDescription(jsonCategory.getString("description"));
                    } else {
                        c.setDescription("No description");
                    }
                    if (jsonCategory.has("title")) {
                        c.setTitle(jsonCategory.getString("title"));
                    } else {
                        c.setTitle("No title");
                    }
                    if (jsonCategory.has("positive")) {
                        c.setPositive(jsonCategory.getBoolean("positive"));
                    }
                    Bitmap bitmap = null;
                    if (jsonCategory.has("image_url")) {
                        String imageUrl = jsonCategory.getString("image_url");
                        try {
                            InputStream in = new URL(imageUrl).openStream();
                            bitmap = BitmapFactory.decodeStream(in);
                        } catch (MalformedURLException e) {
                            // Error downloading / parsing the image, setting to default
                            bitmap = null;//BitmapFactory.decodeResource(ContextCompat.getDrawable(context, R.drawable.no_category_image));
                        } catch (IOException e) {
                            Log.e(TAG, "parseCategories: IOException ", e);
                            bitmap = null;//BitmapFactory.decodeResource(getResources(), R.drawable.no_category_image);
                        }
                    } else {
                        // there was no image for the category, setting default
                        bitmap = null;//BitmapFactory.decodeResource(getResources(), R.drawable.no_category_image);
                    }
                    c.setImage(bitmap);
                    tempCategoryList.add(c);
                }
            }
        }
        return tempCategoryList;
    }

    public static List<Link> parseLinksFromJsonArray(JSONArray jsonArr) throws JSONException {
        ArrayList<Link> tempLinkList = new ArrayList<>();
        for (int i = 0; i < jsonArr.length(); i++) {
            JSONObject jsonLink = jsonArr.getJSONObject(i);
            // check that the object has ID tag
            if (jsonLink.has("id")) {
                Link link = new Link();
                link.setId(jsonLink.getString("id"));
                if (jsonLink.has("description")) {
                    link.setDescription(jsonLink.getString("description"));
                } else {
                    link.setDescription("");
                }
                if (jsonLink.has("title")) {
                    link.setTitle(jsonLink.getString("title"));
                } else {
                    link.setTitle("No title");
                }
                if (jsonLink.has("link")) {
                    link.setLink(jsonLink.getString("link"));
                } else {
                    link.setLink("");
                }
                if (jsonLink.has("active")) {
                    link.setActive(jsonLink.getBoolean("active"));
                } else {
                    link.setActive(false);
                }
                if (jsonLink.has("done")) {
                    JSONArray doneArray = jsonLink.getJSONArray("done");
                    link.setDone(parseStringsFromJsonArray(doneArray));
                } else {
                    link.setDone(null);
                }
                tempLinkList.add(link);
            } else {
            }
        }
        return tempLinkList;
    }

    public static UserFromServer parseUserFromJsonObject(JSONObject jsonObject) throws JSONException {
        UserFromServer userFromServer = new UserFromServer();
        if (jsonObject.has("image_url")) {
            userFromServer.setImageUrl(jsonObject.getString("image_url"));
        }
        if (jsonObject.has("id")) {
            userFromServer.setId(jsonObject.getString("id"));
        }
        if (jsonObject.has("username")) {
            userFromServer.setUsername(jsonObject.getString("username"));
        }
        if (jsonObject.has("score")) {
            userFromServer.setScore(jsonObject.getInt("score"));
        }
        if (jsonObject.has("rankingId")) {
            userFromServer.setRankId(jsonObject.getString("rankingId"));
        }
        if (jsonObject.has("score")){
            userFromServer.setScore(jsonObject.getInt("score"));
        }
        return userFromServer;
    }
}
