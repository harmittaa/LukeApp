package com.luke.lukef.lukeapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.tools.LukeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Submission model class, includes functionality to create new submissions and pass them to server.
 */
public class Submission {
    private static final String TAG = "Submission";
    private String description;
    private String title;
    private ArrayList<Category> categories;
    private String date;
    private Context context;
    private File file;
    private Bitmap image;
    private Location location;

    private String submissionId;
    private String imageUrl;
    private List<String> submissionCategoryList;
    private String submitterId;


    /**
     * Constructor with only the mandatory values required when creating a submission
     *
     * @param context      For accessing resources
     * @param categoryList List of categories chosen for the submission
     * @param description  Description of the submission
     * @param location     Location of the submission
     */
    public Submission(Context context, ArrayList<Category> categoryList, String description, Location location) {
        this.location = location;
        this.categories = categoryList;
        this.description = description;
        this.context = context;
    }

    /**
     * Empty constructor when creating submissions when fetching them from the server
     */
    public Submission() {
    }


    /**
     * Checks whether the submission should be true/false/neutral based on the amount of categories
     * linked to the submission.
     *
     * @return Boolean.TRUE if mostly positive categories, Boolean.FALSE if negative and null if neutral
     */
    private Boolean parsePositive() {
        int positiveCategories = 0;
        int negativeCategories = 0;
        int neutralCategories = 0;
        for (Category c : this.categories) {
            if (c.getPositive() == null) {
                neutralCategories++;
            } else if (!c.getPositive()) {
                negativeCategories++;
            } else if (c.getPositive()) {
                positiveCategories++;
            }
        }
        if (positiveCategories > negativeCategories && positiveCategories > neutralCategories) {
            return Boolean.TRUE;
        } else if (negativeCategories > positiveCategories && negativeCategories > neutralCategories) {
            return Boolean.FALSE;
        } else if (neutralCategories > positiveCategories && neutralCategories > negativeCategories) {
            return null;
        } else {
            return null;
        }
    }

    /**
     * Turns the Submission into a JSONObject to be passed to the server.
     *
     * @return The submission as a JSONObject
     */
    private JSONObject convertToJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            if (this.title != null) {
                jsonObject.put("title", this.title);
            }
            jsonObject
                    .put("longitude", Submission.this.location.getLongitude())
                    .put("latitude", Submission.this.location.getLatitude())
                    .put("altitude", Submission.this.location.getAltitude());
            if (this.image != null) {
                jsonObject.put("image", LukeUtils.bitmapToBase64String(this.getImage()));
            }
            jsonObject.put("description", Submission.this.description);
            jsonObject.put("categoryId", convertCategoriesToJsonArray());

            if (parsePositive() != null) {
                jsonObject.put("positive", parsePositive() + "");
            }
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
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
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
                    Log.e(TAG, "call: RESPONSE CODE:" + conn.getResponseCode());
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
                        stringBuilder.append(line2).append("\n");
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
            Boolean b = futureTask.get();
            return b;
        } catch (InterruptedException e) {
            Log.e(TAG, "submitToServer: ", e);
            return false;
        } catch (ExecutionException e) {
            Log.e(TAG, "submitToServer: ", e);
            return false;
        }
    }

    /**
     * Creates a JSONArray of the category IDs selected by the user
     *
     * @return A JSONArray of categories
     */
    private JSONArray convertCategoriesToJsonArray() {
        JSONArray jsn = new JSONArray();
        for (Category c : this.categories) {
            jsn.put(c.getId());
        }
        Log.e(TAG, "convertCategoriesToJsonArray: categories array" + jsn);
        return jsn;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }


    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSubmissionCategoryList(List<String> submissionCategoryList) {
        this.submissionCategoryList = submissionCategoryList;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }


    public String getSubmitterId() {
        return submitterId;
    }

    public List<String> getSubmissionCategoryList() {
        return submissionCategoryList;
    }
}