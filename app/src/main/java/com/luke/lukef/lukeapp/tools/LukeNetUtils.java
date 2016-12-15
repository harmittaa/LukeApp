package com.luke.lukef.lukeapp.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.result.UserProfile;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.NewUserActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.WelcomeActivity;
import com.luke.lukef.lukeapp.interfaces.Auth0Responder;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.Link;
import com.luke.lukef.lukeapp.model.Rank;
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
        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    String response = getMethod("http://www.balticapp.fi/lukeA/user/available?username=" + usernameToCheck);
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(response);
                        // TODO: 17/11/2016 make alert that username is taken
                        if (jsonObject.has("exists")) {
                            return !jsonObject.getBoolean("exists");
                        } else {
                            return false;
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "onPostExecute: ", e);
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
                //create a json object from this submission to be sent to the server and convert it to string
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("image", LukeUtils.bitmapToBase64String(bitmap));
                String urlParameters = jsonObject.toString();

                return postMethod("http://www.balticapp.fi/lukeA/user/update", urlParameters);
            }
        };

        FutureTask<Boolean> futureTask = new FutureTask<>(booleanCallable);
        Thread t = new Thread(futureTask);
        t.start();

        try {
            return futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "submitToServer: ", e);
            return false;
        }
    }

    /**
     * Sends a request to auth0 to receive a user profile image
     *
     * @param auth0Responder the receiver of the bitmap that auth0 provides
     */
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
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    return BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
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
        Callable<UserFromServer> userFromServerCallable = new Callable<UserFromServer>() {
            @Override
            public UserFromServer call() throws Exception {
                String jsonString;
                jsonString = getMethod("http://www.balticapp.fi/lukeA/user?id=" + userId);


                if (!TextUtils.isEmpty(jsonString)) {
                    final JSONObject jsonObject = new JSONObject(jsonString);
                    return LukeUtils.parseUserFromJsonObject(jsonObject);
                } else {
                    return null;
                }
            }
        };
        FutureTask<UserFromServer> userFromServerFutureTask = new FutureTask<>(userFromServerCallable);
        Thread t = new Thread(userFromServerFutureTask);
        t.start();
        return userFromServerFutureTask.get();

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
                String jsonString = getMethod("http://www.balticapp.fi/lukeA/report/flag?id=" + submissionId);
                Log.e(TAG, "updateUserImage run: Result : " + jsonString);
                return "Error reporting";

            }
        };
        FutureTask<String> booleanFutureTask = new FutureTask<>(booleanCallable);
        Thread t = new Thread(booleanFutureTask);
        t.start();
        try {
            return booleanFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "reportSubmission: ", e);
            return null;
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
                String jsonString = getMethod("http://www.balticapp.fi/lukeA/report?submitterId=" + userID);
                if (!TextUtils.isEmpty(jsonString)) {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    Log.e(TAG, "call: jsonArray" + jsonArray.toString());
                    return LukeUtils.parseSubmissionsFromJsonArray(jsonArray);
                } else {
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
                center.getLatitude() + ",%20" + center.getLongitude() + "&zoom=16&size=" +
                width + "x" + height + "&maptype=normal&markers=color:red|" + center.getLatitude() + "," + center.getLongitude();
        Log.e(TAG, "getMapThumbnail: Marker url: \n" + urlString1);
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
            String jsonString = getMethod(this.context.getString(R.string.report_id_url) + id);
            JSONArray jsonArray = new JSONArray(jsonString);
            Submission submission = LukeUtils.parseSubmissionFromJsonObject(jsonArray.getJSONObject(0));
            return submission;
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

                String jsonString = getMethod("http://www.balticapp.fi/lukeA/category");
                JSONArray jsonArr = new JSONArray(jsonString);
                return LukeUtils.getCategoryObjectsFromJsonArray(jsonArr);

            }
        };
        FutureTask<ArrayList<Category>> arrayListFutureTask = new FutureTask<ArrayList<Category>>(categoriesCallable);
        Thread t = new Thread(arrayListFutureTask);
        t.start();
        return arrayListFutureTask.get();
    }

    private void startFetchUserDataTask(WelcomeActivity welcomeActivity) {
        FetchUserDataTask fetchUserDataTask = new FetchUserDataTask(welcomeActivity);
        fetchUserDataTask.execute();

    }

    public UserFromServer getOwnUser() throws IOException, ExecutionException, InterruptedException {
        Callable<UserFromServer> userFromServerCallable = new Callable<UserFromServer>() {
            @Override
            public UserFromServer call() throws Exception {
                String jsonString = getMethod("http://www.balticapp.fi/lukeA/user/me");
                JSONObject jsonObject = new JSONObject(jsonString);
                return LukeUtils.parseUserFromJsonObject(jsonObject);

            }
        };
        FutureTask<UserFromServer> userFromServerFutureTask = new FutureTask<UserFromServer>(userFromServerCallable);
        Thread t = new Thread(userFromServerFutureTask);
        t.start();
        return userFromServerFutureTask.get();

    }

    /**
     * Fetches user data like ID, image and URL from the server
     */
    private class FetchUserDataTask extends AsyncTask<Void, Void, Void> {
        private WelcomeActivity welcomeActivity;
        UserFromServer userFromServer;

        FetchUserDataTask(WelcomeActivity welcomeActivity) {
            this.welcomeActivity = welcomeActivity;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                userFromServer = getOwnUser();
            } catch (IOException | ExecutionException | InterruptedException e) {
                Log.e(TAG, "doInBackground: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            SessionSingleton.getInstance().setUserId(userFromServer.getId());
            SessionSingleton.getInstance().setUserLogged(true);
            SessionSingleton.getInstance().setScore(userFromServer.getScore());
            String usrnm = userFromServer.getUsername();

            try {
                SessionSingleton.getInstance().setUserImage(getBitmapFromURL(userFromServer.getImageUrl()));
            } catch (ExecutionException | InterruptedException e1) {
                Log.e(TAG, "onPostExecute: ", e1);
            }
            if (!TextUtils.isEmpty(usrnm)) {
                SessionSingleton.getInstance().setUsername(userFromServer.getUsername());
                this.welcomeActivity.startActivity(new Intent(this.welcomeActivity, MainActivity.class));
            } else {
                this.welcomeActivity.startActivity(new Intent(this.welcomeActivity, NewUserActivity.class));
            }


        }

    }

    public void attemptLogin(final WelcomeActivity welcomeActivity, final String idToken) {

        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    URL lukeURL = new URL(welcomeActivity.getString(R.string.loginUrl));
                    HttpURLConnection httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
                    httpURLConnection.setRequestProperty(welcomeActivity.getString(R.string.authorization), welcomeActivity.getString(R.string.bearer) + idToken);
                    if (httpURLConnection.getResponseCode() == 200) {
                        return true;
                    } else {
                        Log.e(TAG, "call: LOGIN DIDN'T WORK");
                        // TODO: 12/12/2016 DANIEL
                        return false;
                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "call: ", e);
                    return false;
                } catch (IOException e) {
                    Log.e(TAG, "call: ", e);
                    return false;
                }

            }
        };
        FutureTask<Boolean> booleanFutureTask = new FutureTask<>(booleanCallable);
        Thread thread = new Thread(booleanFutureTask);
        thread.start();

        try {
            if (booleanFutureTask.get()) {
                startFetchUserDataTask(welcomeActivity);
            } else {
                Log.e(TAG, "onAuthentication: booleanFutureTask failed");
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "onAuthentication: ", e);
        }

    }

    public Link getNewestLink() {
        Callable<Link> linkCallable = new Callable<Link>() {
            @Override
            public Link call() throws Exception {
                String jsonString = getMethod("http://www.balticapp.fi/lukeA/link");
                JSONArray jsonArray = new JSONArray(jsonString);
                Log.e(TAG, "getlinks call: jsonArray" + jsonArray.toString());
                List<Link> links = LukeUtils.parseLinksFromJsonArray(jsonArray);
                return links.get(links.size() - 1);

            }
        };
        FutureTask<Link> linkFutureTask = new FutureTask<>(linkCallable);
        Thread t = new Thread(linkFutureTask);
        t.start();
        try {
            return linkFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "reportSubmission: ", e);
            return null;
        }
    }

    public ArrayList<UserFromServer> getAllUsers() throws ExecutionException, InterruptedException {

        Callable<ArrayList<UserFromServer>> userFromServerCallable = new Callable<ArrayList<UserFromServer>>() {
            @Override
            public ArrayList<UserFromServer> call() throws Exception {
                String jsonString = getMethod("http://www.balticapp.fi/lukeA/user/get-all");
                Log.e(TAG, "doInBackground: STRING IS " + jsonString);
                if (!TextUtils.isEmpty(jsonString)) {
                    ArrayList<UserFromServer> returnjeeben = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(jsonString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        returnjeeben.add(LukeUtils.parseUserFromJsonObject(jsonArray.getJSONObject(i)));
                    }
                    return returnjeeben;
                } else {
                    return null;
                }
            }
        };
        FutureTask<ArrayList<UserFromServer>> userFromServerFutureTask = new FutureTask<>(userFromServerCallable);
        Thread t = new Thread(userFromServerFutureTask);
        t.start();
        return userFromServerFutureTask.get();

    }

    public ArrayList<Rank> getAllRanks() throws ExecutionException, InterruptedException {
        Callable<ArrayList<Rank>> arrayListCallable = new Callable<ArrayList<Rank>>() {
            @Override
            public ArrayList<Rank> call() throws Exception {
                String allRanks = getMethod("http://www.balticapp.fi/lukeA/rank");
                JSONArray allRanksJson = new JSONArray(allRanks);
                ArrayList<Rank> ranks = LukeUtils.parseRanksFromJsonArray(allRanksJson);
                return ranks;
            }
        };
        FutureTask<ArrayList<Rank>> arrayListFutureTask = new FutureTask<ArrayList<Rank>>(arrayListCallable);
        Thread t = new Thread(arrayListFutureTask);
        t.start();
        return arrayListFutureTask.get();
    }

    private String getMethod(String urlString) throws IOException {
        URL lukeURL = new URL(urlString);
        HttpURLConnection httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
        httpURLConnection.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
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
            return jsonString;

        } else {
            //TODO: if error do something else, ERROR STREAM
            Log.e(TAG, "doInBackground: ERROR  " + httpURLConnection.getResponseCode());
            return null;
        }
    }

    private boolean postMethod(String urlString, String params) {
        try {
            HttpURLConnection conn;
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("charset", "utf-8");
            conn.setDoOutput(true);

            //get the output stream of the connection
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            //write the JSONobject to the connections output
            writer.write(params);

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
            conn.disconnect();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "postMethod: ", e);
            return false;
        }
    }

    public static void imageSetupTask(ImageView imageViewToSet, String url, final int defaultImageId, Activity activity){
        class LoadImageTask extends AsyncTask<Void, Void, Void> {

            private ImageView imageView;
            private String urlString;
            private Activity activity;
            private Bitmap bitmap = null;
            int defaultId;

            LoadImageTask(ImageView imageView, String urlString, Activity activity, int defaultId) {
                this.activity = activity;
                this.urlString = urlString;
                this.imageView = imageView;
                this.defaultId = defaultId;
            }

            @Override
            protected Void doInBackground(Void... params) {
                if (this.urlString != null) {
                    LukeNetUtils lukeNetUtils = new LukeNetUtils(this.activity);
                    try {
                        this.bitmap = lukeNetUtils.getBitmapFromURL(urlString);
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e("PERKELE", "doInBackground: ", e);
                    }
                } else {
                    this.bitmap = null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                this.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LoadImageTask.this.bitmap == null) {
                            LoadImageTask.this.imageView.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), defaultImageId, null));
                        } else {
                            LoadImageTask.this.imageView.setImageBitmap(LoadImageTask.this.bitmap);
                        }
                    }
                });
                super.onPostExecute(aVoid);
            }
        }
        LoadImageTask bitmapTask = new LoadImageTask(imageViewToSet,url,activity,defaultImageId);
        bitmapTask.execute();
    }
}
