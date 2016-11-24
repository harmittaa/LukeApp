package com.luke.lukef.lukeapp.model;

/**
 * Created by tehetenamasresha on 01/11/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.luke.lukef.lukeapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;


/**
 * handles submissions made by users
 */
public class Submission {
    private Bitmap image;
    private GeoPoint location;
    private String title;
    private ArrayList<Category> category;
    private Date date;
    private String description;
    private static final String TAG = "Submission";
    Context context;

    //all values present
    public Submission(Context context, String title, ArrayList<Category> category, Date date, String description, Bitmap image, GeoPoint location) {
        this.image = image;
        this.location = location;
        this.title = title;
        this.category = category;
        this.date = date;
        this.description = description;
        this.context = context;
    }

    //only mandatory values
    public Submission(Context context, ArrayList<Category> category, Date date, String description, GeoPoint location) {
        this.location = location;
        this.category = category;
        this.date = date;
        this.description = description;
        this.context = context;
    }

    /**
     * Writes the submission object to the server
     */
    public void submitToServer() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    //create a json object from this submission to be sent to the server
                    String urlParameters = new JSONObject()
                            .put("longitude", Submission.this.location.getLongitude())
                            .put("latitude", Submission.this.location.getLatitude())
                            .put("altitude", Submission.this.location.getAltitude())
                            .put("description", Submission.this.description)
                            .put("categoryId", convertCategoriesToJsonArray()).toString();
                    URL url = new URL("http://www.balticapp.fi/lukeA/report/create");
                    conn = (HttpURLConnection) url.openConnection();
                    //set header values, (tokens, content type and charset)
                    conn.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    conn.setRequestProperty(context.getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("charset", "utf-8");

                    conn.setDoOutput(true);

                    //get the output stream of the connection
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());


                    //write the JSONobject to the connection
                    writer.write(urlParameters);
                    //flush and close the writer
                    writer.flush();
                    writer.close();

                    //do something with the response, in this case check if 200 or error, for testing purposes
                    if (conn.getResponseCode() != 200) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                        String jsonString = "";
                        StringBuilder stringBuilder = new StringBuilder();
                        String line2;
                        while ((line2 = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line2 + "\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();

                        Log.e(TAG, "run: ERROR WITH CATEGORIES : " + jsonString);
                    } else {
                        Log.e(TAG, "run: was success");
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String jsonString = "";
                        StringBuilder stringBuilder = new StringBuilder();
                        String line2;
                        while ((line2 = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line2 + "\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();

                        Log.e(TAG, "run: SUCCESS BITCH!!!" + jsonString);
                    }

                    Log.e(TAG, "run: RESPONSECODE:" + conn.getResponseCode() + " " + conn.getResponseMessage());


                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "submitToServer: ", e);
                } catch (ProtocolException e) {
                    Log.e(TAG, "submitToServer: ", e);
                } catch (MalformedURLException e) {
                    Log.e(TAG, "submitToServer: ", e);
                } catch (IOException e) {
                    Log.e(TAG, "submitToServer: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "submitToServer: ", e);
                }

                //disconnect from the urlConnection
                if(conn != null) {
                    conn.disconnect();
                }

            }
        };

        Thread t = new Thread(r);
        t.start();
    }

    private JSONArray convertCategoriesToJsonArray() {
        JSONArray jsn = new JSONArray();
        for (Category c : this.category) {
            jsn.put(c);
        }
        return jsn;
    }

    //editing an existing submission
    public boolean update() {
        return true;
    }

    //deleting a submission
    public boolean delete() {
        return true;
    }

    //share submissions on other medias
    public boolean share() {
        return true;
    }

    //add a review flag
    public boolean flag() {
        return true;
    }

    public Bitmap getImage() {
        return image;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Category> getCategory() {
        return category;
    }

    public Date getDate() {
        return this.date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}

