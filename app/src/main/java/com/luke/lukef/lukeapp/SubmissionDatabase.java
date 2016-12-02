package com.luke.lukef.lukeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

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
    private static final int DB_VERSION = 2;
    private static final String TABLE_SUBMISSION = "submission";
    private static final String SUBMISSION_ID = "submission_id";
    private static final String SUBMISSION_LONGITUDE = "submission_longitude";
    private static final String SUBMISSION_LATITUDE = "submission_latitude";
    private static final String SUBMISSION_IMG_URL = "submission_img_url";
    private static final String SUBMISSION_TITLE = "submission_title";
    private static final String SUBMISSION_DESCRIPTION = "submission_description";
    private static final String SUBMISSION_DATE = "submission_date";
    private static final String SUBMISSION_RATING = "submission_rating";
    private static final String SUBMISSION_SUBMITTER_ID = "submission_submitterId";
    private static final String SUBMISSION_POSITIVE = "submission_positive";

    private static final String TABLE_ADMIN_MARKER = "admin_marker";
    private static final String ADMIN_MARKER_ID = "admin_marker_id";
    private static final String ADMIN_MARKER_DATE = "admin_marker_date";
    private static final String ADMIN_MARKER_OWNER = "admin_marker_owner";
    private static final String ADMIN_MARKER_DESCRIPTION = "admin_marker_description";
    private static final String ADMIN_MARKER_TITLE = "admin_marker_title";
    private static final String ADMIN_MARKER_LATITUDE = "admin_marker_latitude";
    private static final String ADMIN_MARKER_LONGITUDE = "admin_marker_longitude";


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
                    SUBMISSION_POSITIVE + " text, " +
                    SUBMISSION_SUBMITTER_ID + " text);";

    private static final String SQL_CREATE_TABLE_ADMIN_MARKER =
            "CREATE TABLE " + TABLE_ADMIN_MARKER + " (" +
                    ADMIN_MARKER_ID + " text not null, " +
                    ADMIN_MARKER_DATE + " real not null, " +
                    ADMIN_MARKER_DESCRIPTION + " text, " +
                    ADMIN_MARKER_OWNER + " text, " +
                    ADMIN_MARKER_TITLE + " text not null, " +
                    ADMIN_MARKER_LATITUDE + " text not null, " +
                    ADMIN_MARKER_LONGITUDE + " text not null);";

    public SubmissionDatabase(Context context) {
        super(context, "LukeBase", null, DB_VERSION);
        this.database = this.getReadableDatabase();
    }

    public void clearCache() {
        Log.e(TAG, "clearCache: Emptying the cache");
        this.database = this.getWritableDatabase();
        this.database.execSQL("DROP TABLE IF EXISTS submission");
        this.database.execSQL("DROP TABLE IF EXISTS admin_marker");
        this.database.execSQL(SQL_CREATE_TABLE_SUBMISSION);
        this.database.execSQL(SQL_CREATE_TABLE_ADMIN_MARKER);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SUBMISSION);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_ADMIN_MARKER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS submission");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS admin_marker");
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
            Log.e(TAG, "addSubmissions: JSONarray length" + jsonArray.length());
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
                        if (jsonObject.has("image_url")) {
                            values.put(SUBMISSION_IMG_URL, jsonObject.getString("image_url"));
                        }
                        if (jsonObject.has("title")) {
                            values.put(SUBMISSION_TITLE, jsonObject.getString("title"));
                        }
                        values.put(SUBMISSION_DESCRIPTION, jsonObject.getString("description"));

                        // see if the submission has positive value true/false, if not put neutral
                        if (jsonObject.has("positive")) {
                            values.put(SUBMISSION_POSITIVE, jsonObject.getString("positive"));
                        } else {
                            values.put(SUBMISSION_POSITIVE, "neutral");
                        }
                        values.put(SUBMISSION_DESCRIPTION, jsonObject.getString("description"));
                        // parse the date into a Date object
                        Date date = format.parse(jsonObject.getString("date"));
                        // save milliseconds of the date to the db
                        values.put(SUBMISSION_DATE, date.getTime());
                        values.put(SUBMISSION_SUBMITTER_ID, jsonObject.getString("submitterId"));
                        // insert values
                        this.database.insert(TABLE_SUBMISSION, null, values);
                    } catch (JSONException e) {
                        Log.e(TAG, "A JSON value was not found: ", e);
                    } catch (ParseException e) {
                        Log.e(TAG, "Unable to parse date: ", e);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Couldn't get jsonObject from array", e);
                }
            }

        } else {
            // TODO: 25/11/2016 No submissions, show info to user
            Log.e(TAG, "No submissions");
        }
    }

    /**
     * Parses AdminMarker JSONObjects from a JSONArray and adds them to the DB.
     *
     * @param jsonArray The JSONArray containing submission JSONObjects
     */
    void addAdminMarkers(JSONArray jsonArray) {
        // define the DateFormat in which the AdminMarker's date will be presented
        format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        this.database = this.getWritableDatabase();
        if (jsonArray.length() > 0) {
            Log.e(TAG, "Add AdminMarkers: JSONarray length" + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = jsonArray.getJSONObject(i);
                    ContentValues values = new ContentValues();
                    try {
                        //put the values into the ContentValues
                        values.put(ADMIN_MARKER_ID, jsonObject.getString("id"));
                        values.put(ADMIN_MARKER_LONGITUDE, jsonObject.getString("longitude"));
                        values.put(ADMIN_MARKER_LATITUDE, jsonObject.getString("latitude"));
                        // check optional values
                        if (jsonObject.has("owner")) {
                            values.put(ADMIN_MARKER_OWNER, jsonObject.getString("owner"));
                        }
                        values.put(ADMIN_MARKER_TITLE, jsonObject.getString("title"));
                        if (jsonObject.has("description")) {
                            values.put(ADMIN_MARKER_DESCRIPTION, jsonObject.getString("description"));

                        }
                        // parse the date into a Date object
                        Date date = format.parse(jsonObject.getString("date"));
                        // save milliseconds of the date to the db
                        values.put(ADMIN_MARKER_DATE, date.getTime());
                        // insert values
                        this.database.insert(TABLE_ADMIN_MARKER, null, values);

                        //exampleQuery();

                    } catch (JSONException e) {
                        Log.e(TAG, "A JSON value was not found: ", e);
                    } catch (ParseException e) {
                        Log.e(TAG, "Unable to parse date: ", e);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Couldn't get jsonObject from array", e);
                }
            }

        } else {
            Log.e(TAG, "No admin markers");
        }
    }


    /**
     * Queries SQLite DB based on the <code>VisibleRegion</code>
     *
     * @param visibleRegion Currently visible region on the mpa
     * @return Cursor with query contents
     */
    public Cursor querySubmissions(VisibleRegion visibleRegion) {
        this.database = this.getReadableDatabase();
        double swLat = visibleRegion.latLngBounds.southwest.latitude;
        double swLng = visibleRegion.latLngBounds.southwest.longitude;
        double neLat = visibleRegion.latLngBounds.northeast.latitude;
        double neLng = visibleRegion.latLngBounds.northeast.longitude;


        this.cursor = this.database.rawQuery(
                "SELECT " + SUBMISSION_ID + ", " + SUBMISSION_LATITUDE + ", " + SUBMISSION_LONGITUDE + ", " + SUBMISSION_DATE + ", " + SUBMISSION_POSITIVE +
                        " FROM " + TABLE_SUBMISSION +
                        " WHERE " + SUBMISSION_LATITUDE +
                        " BETWEEN " + swLat + " AND " + neLat +
                        " AND " + SUBMISSION_LONGITUDE +
                        " BETWEEN " + swLng + " AND " + neLng
                , null, null);
        return this.cursor;
    }


    public Cursor queryAdminMarkers() {
        this.database = this.getReadableDatabase();
        String[] projection = {
                ADMIN_MARKER_ID,
                ADMIN_MARKER_LATITUDE,
                ADMIN_MARKER_LONGITUDE,
                ADMIN_MARKER_TITLE
        };

        // define the query
        this.cursor = this.database.query(
                TABLE_ADMIN_MARKER,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return this.cursor;
    }

    /**
     * Queries Submission table by the submission id provided
     *
     * @param submissionId The ID of the submission which data is queried
     * @return Cursor with query result
     */
    public Cursor querySubmissionById(String submissionId) {
        this.database = this.getReadableDatabase();
        String[] projection = {
                SUBMISSION_TITLE,
                SUBMISSION_IMG_URL,
                SUBMISSION_DESCRIPTION,
                SUBMISSION_DATE,
                SUBMISSION_POSITIVE,
                SUBMISSION_RATING
        };

        String whereClause = "submission_id = ?";
        String[] whereArgs = new String[]{
                submissionId
        };

        // define the query
        this.cursor = this.database.query(
                TABLE_SUBMISSION,
                projection,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return this.cursor;
    }

    /**
     * Queries admin_marker table by the id provided
     *
     * @param adminMarkerId The ID of the admin marker which data is queried
     * @return Cursor with query result
     */
    public Cursor queryAdminMarkerById(String adminMarkerId) {
        this.database = this.getReadableDatabase();
        String[] projection = {
                ADMIN_MARKER_TITLE,
                ADMIN_MARKER_DESCRIPTION,
                ADMIN_MARKER_OWNER,
                ADMIN_MARKER_DATE
        };

        String whereClause = "admin_marker_id = ?";
        String[] whereArgs = new String[]{
                adminMarkerId
        };

        // define the query
        this.cursor = this.database.query(
                TABLE_ADMIN_MARKER,
                projection,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return this.cursor;
    }


    /**
     * Closes database connection.
     */
    public void closeDbConnection() {
        this.database.close();
    }
}
