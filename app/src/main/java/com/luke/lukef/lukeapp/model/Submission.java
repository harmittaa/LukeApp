package com.luke.lukef.lukeapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.luke.lukef.lukeapp.R;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import okio.BufferedSink;

/**
 * handles submissions made by users
 */
public class Submission {
    private Bitmap image;
    private Location location;
    private String title;
    private ArrayList<String> category;
    private Date date;
    private String description;
    private static final String TAG = "Submission";
    private Context context;
    private File file;
    private Boolean positive;
    private ArrayList<Category> categories;

    //all values present
    public Submission(Context context, String title, ArrayList<Category> category, Date date, String description, Bitmap image, Location location) {
        this.image = image;
        this.location = location;
        this.title = title;
        this.categories = category;
        this.date = date;
        this.description = description;
        this.context = context;
        this.positive = positive;
    }

    public Submission() {
        //For testing the dummy cardView
    }

    private Boolean parsePositive() {
        int positiveCategories = 0;
        int negativeCategories = 0;
        int neutralCategories = 0;
        for (Category c : categories) {
            if (c.getPositive() == null ) {
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

    //only mandatory values
    public Submission(Context context, ArrayList<Category> category, Date date, String description, Location location) {
        this.location = location;
        this.categories = category;
        this.date = date;
        this.description = description;
        this.context = context;
    }

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
                jsonObject.put("image", bitapToBase64String(this.getImage()));
            }
            jsonObject.put("description", Submission.this.description);
            jsonObject.put("categoryId", convertCategoriesToJsonArray());

            if(parsePositive() != null){
                jsonObject.put("positive",parsePositive()+"");
            }
            // TODO: 25/11/2016 When image implementation on server is done, add image to json object if not null
            Log.e(TAG, "convertToJson: Created json object that looks like: " + jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

        /*
         * MULTIPART MADNESS

        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);


        if (this.title != null) {
            builder.addFormDataPart("title", this.title);
        }
        builder
                .addFormDataPart("longitude", Submission.this.location.getLongitude() + "")
                .addFormDataPart("latitude", Submission.this.location.getLatitude() + "")
                .addFormDataPart("altitude", Submission.this.location.getAltitude() + "");
        if (this.image != null) {
            Log.e(TAG, "convertToJson: Created file with lenght" + file.length());
            Log.e(TAG, "convertToJson: Created file with total space" + file.getTotalSpace());
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpg"), this.file);
            builder.addPart(requestBody);
        }
        builder.addFormDataPart("description", Submission.this.description);
        builder.addFormDataPart("categoryId", convertCategoriesToJsonArray().toString());

        if (!TextUtils.isEmpty(Submission.this.title)) {
            builder.addFormDataPart("title", Submission.this.title);
        }
        return builder.build();*/

    }

    private String bitapToBase64String(Bitmap bmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
                    conn.setRequestProperty(context.getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("charset", "utf-8");
                    conn.setDoOutput(true);

                    //get the output stream of the connection
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                    //write the JSONobject to the connections output
                    writer.write(urlParameters);

                    /*
                     * FILE MADNESS
                     *//*
                    DataOutputStream request = new DataOutputStream(conn.getOutputStream());

                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"" +
                            attachmentName + "\";filename=\"" +
                            attachmentFileName + "\"" + crlf);
                    request.writeBytes(crlf);

                    byte[] pixels = new byte[Submission.this.image.getWidth() * Submission.this.image.getHeight()];
                    for (int i = 0; i < Submission.this.image.getWidth(); ++i) {
                        for (int j = 0; j < Submission.this.image.getHeight(); ++j) {
                            //we're interested only in the MSB of the first byte,
                            //since the other 3 bytes are identical for B&W images
                            pixels[i + j] = (byte) ((Submission.this.image.getPixel(i, j) & 0x80) >> 7);
                        }
                    }
                    request.writeBytes(crlf);
                    request.writeBytes(twoHyphens + boundary +
                            twoHyphens + crlf);

                    request.write(pixels);

                    request.flush();
                    //request.close();

                    *//*
                     * END FILE MADNESS
                     */


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

        /*
        *
        * OKHTTP MADNESS
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder().url("http://www.balticapp.fi/lukeA/report/create")
                        //.method("POST", convertToJson())
                        .addHeader("Authorization", "Bearer " + SessionSingleton.getInstance().getIdToken())
                        .addHeader("acstoken", SessionSingleton.getInstance().getAccessToken())
                        .addHeader("charset", "utf-8")
                        //.addHeader("Content-Type", "application/json")
                        .post(convertToJson())
                        .build();
                Log.e(TAG, "call: " + request);


                //Response response = client.newCall(request).execute();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (response.isSuccessful()) {
                            Log.e(TAG, "call: Success" + response.message());
                        } else {
                            Log.e(TAG, "call: FAilure" + response.message() + "\n" + response.toString() + response.body().toString());
                        }
                    }
                });
                // Do something with the response.

                return true;
            }
        };*/

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

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Location getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getCategory() {
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

    public Boolean getPositive() {
        return positive;
    }

    public void setPositive(Boolean positive) {
        this.positive = positive;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }
}