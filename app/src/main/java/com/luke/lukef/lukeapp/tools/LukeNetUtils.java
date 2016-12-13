package com.luke.lukef.lukeapp.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.result.UserProfile;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.interfaces.Auth0Responder;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.model.UserFromServer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
 * Contains methods for interaction with the server
 */
public class LukeNetUtils {
    private Context context;
    private final String TAG = "LukeNetUtils";

    public LukeNetUtils(Context context) {
        this.context = context;
    }

    /**
     * Checks from server username is available
     *
     * @param usernameToCheck The username that should be checked
     * @return <b>true</b> if the username exists already, <b>false</b> if not
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public boolean checkUsernameAvailable(final String usernameToCheck) throws ExecutionException, InterruptedException {
        Log.e(TAG, "confirmUsername: checking if username available");
        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String jsonString;
                try {
                    URL checkUsernameUrl = new URL("http://www.balticapp.fi/lukeA/user/available?username=" + usernameToCheck);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) checkUsernameUrl.openConnection();
                    httpURLConnection.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    if (httpURLConnection.getResponseCode() == 200) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();
                        Log.e(TAG, "CHECK USERNAME STRING " + jsonString);
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(jsonString);
                            // TODO: 17/11/2016 make alert that username is taken
                            return !jsonObject.getBoolean("exists");
                        } catch (JSONException e) {
                            Log.e(TAG, "onPostExecute: ", e);
                            return false;
                        }
                    } else {
                        return false;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "doInBackground: ", e);
                    return false;
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: ", e);
                    return false;
                }
            }
        };
        FutureTask<Boolean> booleanFutureTask = new FutureTask<>(booleanCallable);
        Thread thread = new Thread(booleanFutureTask);
        thread.start();
        return booleanFutureTask.get();

    }

    /**
     * Sets the the username of the current user into the server
     *
     * @param username The username to be set
     * @return <b>true</b> if the request passes, <b>false</b> if it doesn't
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public boolean setUsername(final String username) throws IOException, ExecutionException, InterruptedException {
        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                HttpURLConnection httpURLConnection;
                URL setUsernameUrl = new URL("http://www.balticapp.fi/lukeA/user/set-username?username=" + username);
                httpURLConnection = (HttpURLConnection) setUsernameUrl.openConnection();
                httpURLConnection.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                httpURLConnection.setRequestProperty(context.getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                return httpURLConnection.getResponseCode() == 200;
            }
        };
        FutureTask<Boolean> booleanFutureTask = new FutureTask<>(booleanCallable);
        Thread t = new Thread(booleanFutureTask);
        t.start();
        return booleanFutureTask.get();
    }

    /**
     * Sets an image for the user into the backend
     *
     * @param bitmap The bitmap that should be set
     * @return <b>true</b> if the update passes, <b>false</b> if not
     */
    public boolean updateUserImage(final Bitmap bitmap) {

        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                HttpURLConnection conn;
                try {
                    //create a json object from this submission to be sent to the server and convert it to string
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("image", LukeUtils.bitmapToBase64String(bitmap));
                    String urlParameters = jsonObject.toString();

                    URL url = new URL("http://www.balticapp.fi/lukeA/user/update");
                    conn = (HttpURLConnection) url.openConnection();
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

                    //get the response, if successfull, get inputstream, if unsuccessful get errorStream
                    BufferedReader bufferedReader;
                    Log.e(TAG, "updateUserImage call: RESPONSE CODE:" + conn.getResponseCode());
                    if (conn.getResponseCode() != 200) {
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                    } else {
                        // TODO: 25/11/2016 check for authorization error, respond accordingly
                        bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    }
                    String jsonString;
                    StringBuilder stringBuilder = new StringBuilder();
                    String line2;
                    while ((line2 = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line2).append("\n");
                    }
                    bufferedReader.close();
                    jsonString = stringBuilder.toString();

                    Log.e(TAG, "updateUserImage run: Result : " + jsonString);


                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "updateUserImage: ", e);
                    return false;
                } catch (ProtocolException e) {
                    Log.e(TAG, "updateUserImage: ", e);
                    return false;
                } catch (MalformedURLException e) {
                    Log.e(TAG, "updateUserImage: ", e);
                    return false;
                } catch (IOException e) {
                    Log.e(TAG, "updateUserImage: ", e);
                    return false;
                }

                //disconnect from the urlConnection
                conn.disconnect();
                return true;
            }


        };

        FutureTask<Boolean> futureTask = new FutureTask<>(booleanCallable);
        Thread t = new Thread(futureTask);
        t.start();

        try {
            return futureTask.get();
        } catch (InterruptedException e) {
            Log.e(TAG, "submitToServer: ", e);
            return false;
        } catch (ExecutionException e) {
            Log.e(TAG, "submitToServer: ", e);
            return false;
        }
    }

    // TODO: 12/12/2016 DANIEL
    public void getUserImageFromAuth0(final Auth0Responder auth0Responder) {
        AuthenticationAPIClient client = new AuthenticationAPIClient(
                new Auth0(SessionSingleton.getInstance().getAuth0ClientID(), SessionSingleton.getInstance().getAuth0Domain()));

        client.tokenInfo(SessionSingleton.getInstance().getIdToken())
                .start(new BaseCallback<UserProfile, AuthenticationException>() {
                    @Override
                    public void onSuccess(UserProfile payload) {
                        try {
                            auth0Responder.receiveBitmapFromAuth0(getBitmapFromURL(payload.getPictureURL()));
                        } catch (ExecutionException | InterruptedException e) {
                            Log.e(TAG, "onSuccess: Error fetching Auth0 image", e);
                        }
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                    }
                });
    }

    /**
     * Fetches Bitmap from the given URL
     *
     * @param imageUrl The URL of the image
     * @return The image as an Bitmap object, otherwise <b>null</b>
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Bitmap getBitmapFromURL(final String imageUrl) throws ExecutionException, InterruptedException {
        Callable<Bitmap> bitmapCallable = new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                try {
                    Log.e(TAG, "call: URL " + imageUrl);
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    return BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
                    // Log exception
                    return null;
                }
            }
        };
        FutureTask<Bitmap> bitmapFutureTask = new FutureTask<>(bitmapCallable);
        Thread t = new Thread(bitmapFutureTask);
        t.start();
        return bitmapFutureTask.get();
    }

    /**
     * Fetches user data from the backend based on given userId
     *
     * @param userId The ID of the user whose data is fetched
     * @return {@link UserFromServer} object containing the user data
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public UserFromServer getUserFromUserId(final String userId) throws ExecutionException, InterruptedException {
        Callable<UserFromServer> bitmapCallable = new Callable<UserFromServer>() {
            @Override
            public UserFromServer call() throws Exception {
                String jsonString;
                UserFromServer userFromServer = new UserFromServer();
                URL lukeURL;
                try {
                    lukeURL = new URL("http://www.balticapp.fi/lukeA/user?id=" + userId);

                    HttpURLConnection httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
                    if (httpURLConnection.getResponseCode() == 200) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        jsonString = stringBuilder.toString();
                        bufferedReader.close();
                        Log.e(TAG, "getSubmitterData: jsonString " + jsonString);

                        if (!TextUtils.isEmpty(jsonString)) {
                            try {
                                final JSONObject jsonObject = new JSONObject(jsonString);
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
                                    userFromServer.setScore(jsonObject.getDouble("score"));
                                }
                                if (jsonObject.has("rankingId")) {
                                    userFromServer.setRankId(jsonObject.getString("rankingId"));
                                }
                                return userFromServer;
                            } catch (JSONException e) {
                                Log.e(TAG, "getBitmapFromUserId: ", e);
                                return null;
                            }
                        }
                    } else {
                        //TODO: if error do something else, ERROR STR
                        Log.e(TAG, "response code something else");
                        return null;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "getBitmapFromUserId: ", e);
                    return null;
                } catch (IOException e) {
                    Log.e(TAG, "getBitmapFromUserId: ", e);
                    return null;
                }
                return null;
            }
        };
        FutureTask<UserFromServer> bitmapFutureTask = new FutureTask<>(bitmapCallable);
        Thread t = new Thread(bitmapFutureTask);
        t.start();
        return bitmapFutureTask.get();

    }

    /**
     * Creates a report for the submission into the server.
     *
     * @param submissionId The ID of the given submission.
     * @return <b>true</b> if the request passes, <b>false</b> if it doesn't.
     */
    public String reportSubmission(final String submissionId) {
        Callable<String> booleanCallable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                URL reportUrl;
                try {
                    reportUrl = new URL("http://www.balticapp.fi/lukeA/report/flag?id=" + submissionId);

                    HttpURLConnection connection = (HttpURLConnection) reportUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("charset", "utf-8");

                    BufferedReader bufferedReader;
                    Log.e(TAG, "updateUserImage call: RESPONSE CODE:" + connection.getResponseCode());
                    if (connection.getResponseCode() != 200) {
                        bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        return "Error reporting";
                    } else {
                        // TODO: 25/11/2016 check for authorization error, respons accordingly
                        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    }
                    String jsonString;
                    StringBuilder stringBuilder = new StringBuilder();
                    String line2;
                    while ((line2 = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line2).append("\n");
                    }
                    bufferedReader.close();
                    jsonString = stringBuilder.toString();
                    JSONObject josn = new JSONObject(jsonString);
                    Log.e(TAG, "call: josn boii" + josn.toString());
                    if (josn.has("action")) {
                        if (josn.getBoolean("action")) {
                            Log.e(TAG, "call: TOAST FLAGED");
                            return "Submission reported";
                        } else {
                            Log.e(TAG, "call: TOAST UNFALGED");
                          return "Report removed";
                        }
                    }

                    Log.e(TAG, "updateUserImage run: Result : " + jsonString);
                    return "Error reporting";
                } catch (MalformedURLException e) {
                    Log.e(TAG, "reportSubmission: ", e);
                    return "Error reporting";
                } catch (ProtocolException e) {
                    Log.e(TAG, "reportSubmission: ", e);
                    return "Error reporting";
                } catch (IOException e) {
                    Log.e(TAG, "reportSubmission: ", e);
                    return "Error reporting";
                }
            }
        };
        FutureTask<String> booleanFutureTask = new FutureTask<>(booleanCallable);
        Thread t = new Thread(booleanFutureTask);
        t.start();
        try {
            return booleanFutureTask.get();
        } catch (InterruptedException e) {
            Log.e(TAG, "reportSubmission: ", e);
            return "Error reporting";
        } catch (ExecutionException e) {
            Log.e(TAG, "reportSubmission: ", e);
            return "Error reporting";
        }

    }

    /**
     * Fetches user's submissions from the server.
     *
     * @param userID The ID of the user whose submissions should be fetched.
     * @return Returns an ArrayList of {@link Submission} objects.
     */
    public ArrayList<Submission> getSubmissionsByUser(final String userID) {
        Callable<ArrayList<Submission>> booleanCallable = new Callable<ArrayList<Submission>>() {
            @Override
            public ArrayList<Submission> call() throws Exception {
                URL reportUrl;
                try {
                    reportUrl = new URL("http://www.balticapp.fi/lukeA/report?submitterId=" + userID);

                    HttpURLConnection connection = (HttpURLConnection) reportUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    connection.setRequestProperty(context.getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("charset", "utf-8");

                    BufferedReader bufferedReader;
                    Log.e(TAG, "updateUserImage call: RESPONSE CODE:" + connection.getResponseCode());
                    if (connection.getResponseCode() != 200) {
                        bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    } else {
                        // TODO: 25/11/2016 check for authorization error, respons accordingly
                        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    }
                    String jsonString;
                    StringBuilder stringBuilder = new StringBuilder();
                    String line2;
                    while ((line2 = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line2).append("\n");
                    }
                    bufferedReader.close();
                    jsonString = stringBuilder.toString();
                    if (!TextUtils.isEmpty(jsonString)) {
                        JSONArray jsonArray = new JSONArray(jsonString);
                        Log.e(TAG, "call: jsonArray" + jsonArray.toString());
                        return LukeUtils.parseSubmissionsFromJsonArray(jsonArray);
                    } else {
                        return null;
                    }

                } catch (MalformedURLException e) {
                    Log.e(TAG, "reportSubmission: ", e);
                    return null;
                } catch (ProtocolException e) {
                    Log.e(TAG, "reportSubmission: ", e);
                    return null;
                } catch (IOException e) {
                    Log.e(TAG, "reportSubmission: ", e);
                    return null;
                }
            }
        };
        FutureTask<ArrayList<Submission>> booleanFutureTask = new FutureTask<>(booleanCallable);
        Thread t = new Thread(booleanFutureTask);
        t.start();
        try {
            return booleanFutureTask.get();
        } catch (InterruptedException e) {
            Log.e(TAG, "reportSubmission: ", e);
            return null;
        } catch (ExecutionException e) {
            Log.e(TAG, "reportSubmission: ", e);
            return null;
        }
    }

    /**
     * Fetches a thumbnail of the map from Google's API based on given location.
     *
     * @param center Center of the wanted thumbnail.
     * @param width  Width in px of the wanted thumbnail.
     * @param height Height in px of the wanted thumbnail.
     * @return The thumbnail of the map as a Bitmap.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Bitmap getMapThumbnail(final Location center, final int width, final int height) throws ExecutionException, InterruptedException {
        final String urlString1 = "https://maps.googleapis.com/maps/api/staticmap?center=" +
                center.getLatitude() + ",%20" + center.getLongitude() + "&zoom=18&size=" +
                width + "x" + height + "&maptype=normal&markers=size:mid|color:red|";
        return getBitmapFromURL(urlString1);
    }

    /**
     * Fetches the Submission by ID from the server
     *
     * @param id The ID of the submission which data needs to be fetched
     * @return Submission object
     */
    public Submission getSubmissionFromId(String id) {
        try {
            URL lukeURL = new URL(this.context.getString(R.string.report_id_url) + id);
            HttpURLConnection httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
            if (httpURLConnection.getResponseCode() == 200) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String jsonString = "";
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                jsonString = stringBuilder.toString();
                bufferedReader.close();

                JSONArray jsonArray = new JSONArray(jsonString);
                Submission submission = LukeUtils.parseSubmissionFromJsonObject(jsonArray.getJSONObject(0));
                return submission;

            } else {
                //TODO: if error do something else, ERROR STREAM
                Log.e(TAG, "response code something else");
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Exception with fetching data: " + e.toString());
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "getSubmissionFromId: ", e);
            return null;
        }
    }


    /**
     * Fetches categories from the server, parses them and adds new ones to the {@link SessionSingleton#getCategoryList()}
     */
    public ArrayList<Category> getCategories() throws ExecutionException, InterruptedException {
        Callable<ArrayList<Category>> categoriesCallable = new Callable<ArrayList<Category>>() {
            @Override
            public ArrayList<Category> call() throws Exception {
                String jsonString;

                try {
                    URL categoriesUrl = new URL("http://www.balticapp.fi/lukeA/category");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) categoriesUrl.openConnection();
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
                        JSONArray jsonArr;
                        jsonArr = new JSONArray(jsonString);
                        return LukeUtils.getCategoryObjectsFromJsonArray(jsonArr);
                    } else {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                        jsonString = "";
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();
                        Log.e(TAG, "run: ERROR WITH CATEGORIES : " + jsonString);
                        return null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: ", e);
                    return null;
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing exception");
                    return null;
                }
            }
        };
        FutureTask<ArrayList<Category>> arrayListFutureTask = new FutureTask<ArrayList<Category>>(categoriesCallable);
        Thread t = new Thread(arrayListFutureTask);
        t.start();
        return arrayListFutureTask.get();
    }

}
