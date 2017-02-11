/*
        BalticApp, for studying and tracking the condition of the Baltic sea
        and Gulf of Finland throug user submissions.
        Copyright (C) 2016  Daniel Zakharin, LuKe

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/> or
        the beginning of MainActivity.java file.

*/

package com.luke.lukef.lukeapp.tools;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
    private SubmissionDatabase SubmissionDatabase;

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
                SubmissionDatabase = new SubmissionDatabase(getApplicationContext());
                SubmissionDatabase.clearCache();
                getAdminMarkers();
                getSubmissions();
            }
        });
        serviceThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "SERVICE DESTROYED");
        super.onDestroy();
    }

    /**
     * Fetches all submissions from the server, and passes the resulting JSONArray to SQLiteHelper
     */
    private void getSubmissions() {
        String jsonString;
        try {
            URL getReportsUrl = new URL("http://www.balticapp.fi/lukeA/report");
            HttpURLConnection httpURLConnection = (HttpURLConnection) getReportsUrl.openConnection();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                jsonString = stringBuilder.toString();
                JSONArray jsonArray;

                try {
                    // make new JSONArray from the server's reply
                    jsonArray = new JSONArray(jsonString);
                    this.SubmissionDatabase = new SubmissionDatabase(getApplicationContext());
                    this.SubmissionDatabase.addSubmissions(jsonArray);

                    // TODO: 25/11/2016 Handle exceptions
                } catch (JSONException e) {
                    Log.e(TAG, "onPostExecute: ", e);
                }
            } else {
                Toast toast = new Toast(this.context);
                toast.setText("Couldn't download submissions, restart");
                toast.setDuration(Toast.LENGTH_LONG);
                toast.show();
                Log.e(TAG, "Responsecode = " + httpURLConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.e(TAG, "run: EXCEPTION", e);
            Log.e(TAG, "doInBackground: ", e);
        }
    }

    /**
     * Fetches all admin markers from the server, and passes the resulting JSONArray to SQLiteHelper
     */
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
                    this.SubmissionDatabase = new SubmissionDatabase(getApplicationContext());
                    this.SubmissionDatabase.addAdminMarkers(jsonArray);
                    this.SubmissionDatabase.closeDbConnection();

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
