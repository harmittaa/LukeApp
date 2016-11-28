package com.luke.lukef.lukeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * SQLiteHelper class to save fetched Submissions into SQLite
 */
public class SubmissionDatabase extends SQLiteOpenHelper {
    DateFormat format;
    private static final String TAG = "SubmissionDatabase";
    private SQLiteDatabase database;
    private Cursor cursor;
    private static final int DB_VERSION = 1;
    private static final String TABLE_SUBMISSION = "submission";
    private static final String SUBMISSION_ID = "submission_id";
    private static final String SUBMISSION_LONGITUDE = "submission_longitude";
    private static final String SUBMISSION_LATITUDE = "submission_latitude";
    private static final String SUBMISSION_IMG_URL = "submission_img_url";
    private static final String SUBMISSION_TITLE = "submission_title";
    private static final String SUBMISSION_DESCRIPTION = "submission_img_description";
    private static final String SUBMISSION_DATE = "submission_date";
    private static final String SUBMISSION_RATING = "submission_rating";
    private static final String SUBMISSION_SUBMITTER_ID = "submission_submitterId";

    private static final String SQL_CREATE_TABLE_SUBMISSION =
            "CREATE TABLE " + TABLE_SUBMISSION + " (" +
                    SUBMISSION_ID + " text not null, " +
                    SUBMISSION_LONGITUDE + " real not null, " +
                    SUBMISSION_LATITUDE + " real not null, " +
                    SUBMISSION_IMG_URL + " text, " +
                    SUBMISSION_TITLE + " text, " +
                    SUBMISSION_DESCRIPTION + " text not null, " +
                    SUBMISSION_DATE + " real not null, " +
                    SUBMISSION_RATING + " real, " +
                    SUBMISSION_SUBMITTER_ID + " text);";

    SubmissionDatabase(Context context, String name, int version) {
        super(context, name, null, version);
        database = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SUBMISSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS submission");
    }

    /**
     * Parses Submission JSONObjects from a JSONArray and adds them to the DB.
     *
     * @param jsonArray The JSONArray containing submission JSONObjects
     */
    void addSubmissions(JSONArray jsonArray) {
        // define the DateFormat in which the submission's date will be presented
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        this.database = this.getWritableDatabase();
        if (jsonArray.length() > 0) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = jsonArray.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    try {
                        //put the values into the ContentValues
                        values.put(SUBMISSION_ID, jsonObject.getString("id"));
                        values.put(SUBMISSION_LONGITUDE, jsonObject.getString("longitude"));
                        values.put(SUBMISSION_LATITUDE, jsonObject.getString("latitude"));
                        // check optional values
                        if (jsonObject.has("img_url")) {
                            values.put(SUBMISSION_IMG_URL, jsonObject.getString("img_url"));
                        }
                        if (jsonObject.has("title")) {
                            values.put(SUBMISSION_TITLE, jsonObject.getString("title"));
                        }
                        values.put(SUBMISSION_DESCRIPTION, jsonObject.getString("description"));
                        // parse the date into a Date object
                        Date date = format.parse(jsonObject.getString("date"));
                        // save milliseconds of the date to the db
                        values.put(SUBMISSION_DATE, date.getTime());
                        values.put(SUBMISSION_SUBMITTER_ID, jsonObject.getString("submitterId"));
                        // insert values
                        this.database.insert(TABLE_SUBMISSION, null, values);

                        //exampleQuery();

                    } catch (JSONException e) {
                        Log.e(TAG, "A JSON value was not found: ", e);
                    } catch (ParseException e) {
                        Log.e(TAG, "Unable to parse date: ", e);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Couldn't get jsonObject from array", e);
                }
                this.database.close();
            }

        } else {
            // TODO: 25/11/2016 No submissions, show info to user
            Log.e(TAG, "No submissions");
        }
    }




    /**
     * Example of fetching submission data from DB
     */
    private void exampleQuery() {
        this.database = this.getReadableDatabase();
        String[] projection = {
                SUBMISSION_ID,
                SUBMISSION_DATE,
                SUBMISSION_DESCRIPTION
        };

        // define the query
        this.cursor = this.database.query(
                TABLE_SUBMISSION,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        this.cursor.moveToFirst();
        // see what's inside
        if (this.cursor.getCount() > 0) {
            while (this.cursor.moveToNext()) {
                Log.e(TAG, "exampleQuery:id " + this.cursor.getString(this.cursor.getColumnIndexOrThrow(SUBMISSION_ID)));
                Date date1 = new Date(this.cursor.getLong(this.cursor.getColumnIndexOrThrow(SUBMISSION_DATE)));
                format.format(date1);
            }
        } else {
            Log.e(TAG, "exampleQuery: Count 0 ");
        }
    }
}
