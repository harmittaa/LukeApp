package com.luke.lukef.lukeapp;


import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.auth0.android.Auth0;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;
import com.luke.lukef.lukeapp.tools.LukeUtils;
import com.luke.lukef.lukeapp.tools.SubmissionFetchService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Handles parsing Auth0 response of login,
 */
public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "WelcomeActivity";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Button loginButton;
    private Button skipLoginButton;
    private Lock lock;
    private String idToken = "";
    private String accessToken = "";
    private LukeNetUtils lukeNetUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        this.lukeNetUtils = new LukeNetUtils(getApplicationContext());
        this.loginButton = (Button) findViewById(R.id.loginButton);
        this.skipLoginButton = (Button) findViewById(R.id.skipLoginButton);
        this.loginButton.setOnClickListener(this);
        this.skipLoginButton.setOnClickListener(this);
        requestPermission();
        startService(new Intent(this, SubmissionFetchService.class));
        getCategories();

    }

    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skipLoginButton:
                if (LukeUtils.checkInternetStatus(this)) {
                    SessionSingleton.getInstance().setUserLogged(false);
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                }

                break;
            case R.id.loginButton:
                if (LukeUtils.checkInternetStatus(this)) {
                    SessionSingleton.getInstance().setUserLogged(false);
                    Auth0SetupTask auth0SetupTask = new Auth0SetupTask(getString(R.string.auth0URL));
                    auth0SetupTask.execute();

                }
                break;
        }
    }


    /*
    AUTH0 STUFF
     */

    private void doLogin(String clientId, String domain) {
        Auth0 auth0 = new Auth0(clientId, domain);
        this.lock = Lock.newBuilder(auth0, this.callBack).build(this);
        startActivity(this.lock.newIntent(this));
    }

    private final LockCallback callBack = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            accessToken = credentials.getAccessToken();
            idToken = credentials.getIdToken();
            SessionSingleton.getInstance().setAccessToken(accessToken);
            SessionSingleton.getInstance().setIdToken(idToken);
            Log.e(TAG, "onAuthentication: idToken " + idToken);
            // TODO: 15/11/2016 login to luke, check username, change to new user screen if first time login
            LoginCallable loginTask = new LoginCallable();
            FutureTask<Boolean> booleanFutureTask = new FutureTask<Boolean>(loginTask);
            Thread thread = new Thread(booleanFutureTask);
            thread.start();
            try {
                if (booleanFutureTask.get()) {
                    // TODO: 15/11/2016 check username, if exists -> main screen, if not -> go to username creation
                    FetchUserDataTask fetchUserDataTask = new FetchUserDataTask();
                    fetchUserDataTask.execute();
                    SessionSingleton.getInstance().setUserLogged(true);
                    finish();
                } else {
                    Log.e(TAG, "onAuthentication: booleanFutureTask failed");
                    finish();
                }
            } catch (InterruptedException | ExecutionException e) {
                Log.e(TAG, "onAuthentication: ", e);
            }
            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }

        @Override
        public void onCanceled() {
        }

        @Override
        public void onError(LockException error) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.lock != null) {
            this.lock.onDestroy(this);
            this.lock = null;
        }
    }

    /**
     * Fetches information required for Auth0 setup
     */
    private class Auth0SetupTask extends AsyncTask<Void, Void, Void> {
        String url;
        String jsonString;

        Auth0SetupTask(String url) {
            this.url = url;
        }

        private boolean parseCheck(String domain, String clientID) {
            return !TextUtils.isEmpty(domain) && !TextUtils.isEmpty(clientID);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL lukeURL = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) lukeURL.openConnection();

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
                } else {
                    //TODO: if error do something else, ERROR STREAM
                }
                httpURLConnection.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: ", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            JSONObject jsonObject;
            String auth0ClienID = "";
            String auth0Domain = "";

            try {
                jsonObject = new JSONObject(jsonString);
                auth0ClienID = jsonObject.getString("AUTH0_CLIENT_ID");
                auth0Domain = jsonObject.getString("AUTH0_DOMAIN");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (parseCheck(auth0Domain, auth0ClienID)) {
                doLogin(auth0ClienID, auth0Domain);
                SessionSingleton.getInstance().setAuth0Domain(auth0Domain);
                SessionSingleton.getInstance().setAuth0ClientID(auth0ClienID);
            }
        }
    }

    /**
     * Pushes login to server
     */
    private class LoginCallable implements Callable<Boolean> {
        private HttpURLConnection httpURLConnection;

        @Override
        public Boolean call() throws Exception {
            try {
                URL lukeURL = new URL(getString(R.string.loginUrl));
                this.httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
                this.httpURLConnection.setRequestProperty(getString(R.string.authorization), getString(R.string.bearer) + idToken);
                if (this.httpURLConnection.getResponseCode() == 200) {
                    return true;
                } else {
                    Log.e(TAG, "call: LOGIN DIDN'T WORK" );
                    // TODO: 12/12/2016 DANIEL
                    return false;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * Fetches user data like ID, image and URL from the server
     */
    private class FetchUserDataTask extends AsyncTask<Void, Void, Void> {
        private String jsonString;
        private HttpURLConnection httpURLConnection;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL lukeURL = new URL("http://www.balticapp.fi/lukeA/user/me");
                httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
                httpURLConnection.setRequestProperty(getString(R.string.authorization), getString(R.string.bearer) + idToken);
                if (httpURLConnection.getResponseCode() == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    jsonString = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    jsonString = stringBuilder.toString();
                    bufferedReader.close();
                    Log.e(TAG, "doInBackground: STRING IS " + jsonString);

                } else {
                    //TODO: if error do something else, ERROR STREAM
                    Log.e(TAG, "doInBackground: ERROR  " + httpURLConnection.getResponseCode());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("id")) {
                    SessionSingleton.getInstance().setUserId(jsonObject.getString("id"));
                }
                SessionSingleton.getInstance().setXp(jsonObject.getInt("score"));
                if (jsonObject.has("image_url")) {
                    if (!TextUtils.isEmpty(jsonObject.getString("image_url")) && !jsonObject.getString("image_url").equals("null")) {
                        SessionSingleton.getInstance().setUserImage(lukeNetUtils.getBitmapFromURL(jsonObject.getString("image_url")));
                    }
                }
                if (jsonObject.has("username")) {
                    SessionSingleton.getInstance().setUsername(jsonObject.getString("username"));
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), NewUserActivity.class));
                }

            } catch (JSONException | InterruptedException | ExecutionException e) {
                Log.e(TAG, "onPostExecute: ", e);
            }
        }
    }


    private void getCategories() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(SessionSingleton.getInstance().getCategoryList().size() < 1) {
                    LukeNetUtils lukeNetUtils = new LukeNetUtils(WelcomeActivity.this);
                    try {
                        SessionSingleton.getInstance().getCategoryList().addAll(lukeNetUtils.getCategories());
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e(TAG, "run: ERROR ", e);
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}
