package com.luke.lukef.lukeapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SubmissionFetchService extends Service {
    private static final String TAG = "SubmissionFetchService";
    private Context context;
    private SubmissionDatabase submissionDatabase;

    public SubmissionFetchService(Context context) {
        this.context = context;
        System.out.println("DB created");

    }


    public SubmissionFetchService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: SubmissionFetchService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "run:Running service");
                submissionDatabase = new SubmissionDatabase(getApplicationContext());
                submissionDatabase.clearCache();
                getSubmissions();
                getAdminMarkers();
            }
        });

        serviceThread.start();
        return START_STICKY_COMPATIBILITY;
    }

    /**
     * Fetches all submissions from the server, and passes the resulting JSONArray to SQLiteHelper
     */
    private void getSubmissions() {
        String jsonString;
        try {
            URL getReportsUrl = new URL("http://www.balticapp.fi/lukeA/luke_report");
            HttpURLConnection httpURLConnection = (HttpURLConnection) getReportsUrl.openConnection();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                jsonString = "";
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                jsonString = stringBuilder.toString();
                JSONObject jsonObject;
                JSONArray jsonArray;

                try {
                    // make new JSONArray from the server's reply
                    jsonArray = new JSONArray(jsonString);
                    this.submissionDatabase = new SubmissionDatabase(getApplicationContext());
                    this.submissionDatabase.addSubmissions(jsonArray);

                    // TODO: 25/11/2016 Handle exceptions
                } catch (JSONException e) {
                    Log.e(TAG, "onPostExecute: ", e);
                }
            } else {
                // TODO: 25/11/2016 Show error when responsecode is not 200
                Log.e(TAG, "Responsecode = " + httpURLConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "run: EXCEPTION", e);
            Log.e(TAG, "doInBackground: ", e);
        }
    }

    private void getAdminMarkers() {
        String jsonString;
        try {
            URL getReportsUrl = new URL("http://www.balticapp.fi/lukeA/marker");
            HttpURLConnection httpURLConnection = (HttpURLConnection) getReportsUrl.openConnection();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                jsonString = "";
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                jsonString = stringBuilder.toString();
                JSONObject jsonObject;
                JSONArray jsonArray;

                try {
                    // make new JSONArray from the server's reply
                    jsonArray = new JSONArray(jsonString);
                    this.submissionDatabase = new SubmissionDatabase(getApplicationContext());
                    this.submissionDatabase.addAdminMarkers(jsonArray);
                    this.submissionDatabase.closeDbConnection();

                    // TODO: 25/11/2016 Handle exceptions
                } catch (JSONException e) {
                    Log.e(TAG, "onPostExecute: ", e);
                }
            } else {
                // TODO: 25/11/2016 Show error when responsecode is not 200
                Log.e(TAG, "Responsecode = " + httpURLConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "run: EXCEPTION", e);
            Log.e(TAG, "doInBackground: ", e);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
