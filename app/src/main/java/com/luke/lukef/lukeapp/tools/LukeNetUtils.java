package com.luke.lukef.lukeapp.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.result.UserProfile;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.NewUserActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.interfaces.Auth0Responder;
import com.luke.lukef.lukeapp.model.SessionSingleton;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by Daniel on 05/12/2016.
 */

public class LukeNetUtils {

    private Context context;
    private final String TAG = "LukeNetUtils";

    public LukeNetUtils(Context context) {
        this.context = context;
    }

    public boolean checkUsernameAvailable(final String usernameToCheck) throws ExecutionException, InterruptedException {
        Log.e(TAG, "confirmUsername: clickd");
        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String jsonString;
                try {
                    URL checkUsernameUrl = new URL("http://www.balticapp.fi/lukeA/user/available?username=" + usernameToCheck);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) checkUsernameUrl.openConnection();
                    httpURLConnection.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    httpURLConnection.setRequestProperty(context.getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                    if (httpURLConnection.getResponseCode() == 200) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line + "\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(jsonString);
                            if (!jsonObject.getBoolean("exists")) {
                                return true;
                            } else {
                                // TODO: 17/11/2016 make alert that username is taken
                                return false;
                            }
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
        FutureTask<Boolean> booleanFutureTask = new FutureTask<Boolean>(booleanCallable);
        Thread thread = new Thread(booleanFutureTask);
        thread.start();
        return booleanFutureTask.get();

    }

    public boolean setUsername(final String username) throws IOException, ExecutionException, InterruptedException {
        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                HttpURLConnection httpURLConnection;
                URL setUsernameUrl = new URL("http://www.balticapp.fi/lukeA/user/set-username?username=" + username);
                httpURLConnection = (HttpURLConnection) setUsernameUrl.openConnection();
                httpURLConnection.setRequestProperty(context.getString(R.string.authorization), context.getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                httpURLConnection.setRequestProperty(context.getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                if (httpURLConnection.getResponseCode() == 200) {
                    return true;
                } else {
                    return false;

                }
            }
        };
        FutureTask<Boolean> booleanFutureTask = new FutureTask<Boolean>(booleanCallable);
        Thread t = new Thread(booleanFutureTask);
        t.start();
        return booleanFutureTask.get();
    }

    public boolean updateUserImage(final Bitmap bitmap) {

        Callable<Boolean> booleanCallable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                HttpURLConnection conn = null;
                try {
                    //create a json object from this submission to be sent to the server and convert it to string
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("image", LukeUtils.bitapToBase64String(bitmap));
                    String urlParameters = jsonObject.toString();

                    URL url = new URL("http://www.balticapp.fi/lukeA/user/update");
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

                    //flush and close the writer
                    writer.flush();
                    writer.close();

                    //get the response, if succesful, get inurstream, if unsuccesful get errorstream
                    BufferedReader bufferedReader;
                    Log.e(TAG, "updateUserImage call: RESPONSE CODE:" + conn.getResponseCode());
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

    public void getUserImageFromAuth0(final Auth0Responder auth0Responder){
        AuthenticationAPIClient client = new AuthenticationAPIClient(
                new Auth0(SessionSingleton.getInstance().getAuth0ClienID(), SessionSingleton.getInstance().getAuth0Domain()));

        client.tokenInfo(SessionSingleton.getInstance().getIdToken())
                .start(new BaseCallback<UserProfile, AuthenticationException>() {
                    @Override
                    public void onSuccess(UserProfile payload) {
                        try {
                            auth0Responder.receiveBitmapFromAuth0(getBitmapFromURL(payload.getPictureURL()));
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(AuthenticationException error) {
                    }
                });
    }

    public Bitmap getBitmapFromURL(final String src) throws ExecutionException, InterruptedException {
        Callable<Bitmap> bitmapCallable = new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException e) {
                    // Log exception
                    return null;
                }
            }
        };
        FutureTask<Bitmap> bitmapFutureTask = new FutureTask<Bitmap>(bitmapCallable);
        Thread t = new Thread(bitmapFutureTask);
        t.start();
        return bitmapFutureTask.get();
    }

}