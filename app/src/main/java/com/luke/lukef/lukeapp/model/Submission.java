package com.luke.lukef.lukeapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.luke.lukef.lukeapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * handles submissions made by users
 */
public class Submission {
    private Bitmap image;
    private Location location;
    private String title;
    private ArrayList<Category> category;
    private Date date;
    private String description;
    private static final String TAG = "Submission";
    Context context;

    //all values present
    public Submission(Context context, String title, ArrayList<Category> category, Date date, String description, Bitmap image, Location location) {
        this.image = image;
        this.location = location;
        this.title = title;
        this.category = category;
        this.date = date;
        this.description = description;
        this.context = context;
    }

    //only mandatory values
    public Submission(Context context, ArrayList<Category> category, Date date, String description, Location location) {
        this.location = location;
        this.category = category;
        this.date = date;
        this.description = description;
        this.context = context;
    }

    private JSONObject convertToJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject
                    .put("longitude", Submission.this.location.getLongitude())
                    .put("latitude", Submission.this.location.getLatitude())
                    .put("altitude", Submission.this.location.getAltitude())
                    .put("description", Submission.this.description)
                    .put("categoryId", convertCategoriesToJsonArray()).toString();


            if (!TextUtils.isEmpty(Submission.this.title)) {
                jsonObject.put("title", Submission.this.title);
            }
            // TODO: 25/11/2016 When image implementation on server is done, add image to json object if not null
            Log.e(TAG, "convertToJson: Created json object that looks like: " + jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Writes the submission object to the server
     */
    public boolean submitToServer() {

        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                HttpURLConnection conn = null;
                try {
                    //create a json object from this submission to be sent to the server and convert it to string
                    String urlParameters = Submission.this.convertToJson().toString();

                    URL url = new URL("http://www.balticapp.fi/lukeA/report/create");
                    conn = (HttpURLConnection) url.openConnection();
                    //set header values, (tokens, content type and charset)
                    // TODO: 25/11/2016 check if tokens are valid
                    conn.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    conn.setRequestProperty(context.getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("charset", "utf-8");

                    conn.setDoOutput(true);

                    //get the output stream of the connection
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                    //write the JSONobject to the connections output
                    writer.write(urlParameters);
                    //flush and close the writer
                    writer.flush();
                    writer.close();

                    //get the response, if succesful, get inurstream, if unsuccesful get errorstream
                    BufferedReader bufferedReader;
                    if (conn.getResponseCode() != 200) {
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                    } else {
                        // TODO: 25/11/2016 check for authorization error, respons accordingly
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    }
                    String jsonString = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    String line2;
                    while ((line2 = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line2 + "\n");
                    }
                    bufferedReader.close();
                    jsonString = stringBuilder.toString();

                    Log.e(TAG, "run: Result : " + jsonString);


                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "submitToServer: ", e);
                    return false;
                } catch (ProtocolException e) {
                    Log.e(TAG, "submitToServer: ", e);
                    return false;
                } catch (MalformedURLException e) {
                    Log.e(TAG, "submitToServer: ", e);
                    return false;
                } catch (IOException e) {
                    Log.e(TAG, "submitToServer: ", e);
                    return false;
                }

                //disconnect from the urlConnection
                if (conn != null) {
                    conn.disconnect();
                }
                return true;
            }
        };

        FutureTask<Boolean> futureTask = new FutureTask<Boolean>(booleanCallable);
        Thread t = new Thread(futureTask);
        t.start();

        try {
            boolean b = futureTask.get();
            return b;
        } catch (InterruptedException e) {
            Log.e(TAG, "submitToServer: ", e);
            return false;
        } catch (ExecutionException e) {
            Log.e(TAG, "submitToServer: ", e);
            return false;
        }

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

    public Location getLocation() {
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