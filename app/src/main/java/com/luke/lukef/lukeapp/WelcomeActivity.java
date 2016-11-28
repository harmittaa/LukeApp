package com.luke.lukef.lukeapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.net.ssl.HttpsURLConnection;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "WelcomeActivity";


    private Button loginButton;
    private Button skipLoginButton;

    private Lock lock;
    private String idToken = "";
    private String accessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        loginButton = (Button) findViewById(R.id.loginButton);
        skipLoginButton = (Button) findViewById(R.id.skipLoginButton);

        loginButton.setOnClickListener(this);
        skipLoginButton.setOnClickListener(this);

        requestPermission();

        startService(new Intent(this, SubmissionFetchService.class));

    }

    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skipLoginButton:
                //TODO: switch to mainactivity
                SessionSingleton.getInstance().setUserLogged(false);
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                break;
            case R.id.loginButton:
                SetupTask setupTask = new SetupTask(getString(R.string.auth0URL));
                setupTask.execute();
                break;
        }
    }

    /*

    AUTH0 STUFF


     */


    private void doLogin(String clientId, String domain) {
        Auth0 auth0 = new Auth0(clientId, domain);
        lock = Lock.newBuilder(auth0, callBack).build(this);
        startActivity(lock.newIntent(this));
    }

    private final LockCallback callBack = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            accessToken = credentials.getAccessToken();
            idToken = credentials.getIdToken();
            SessionSingleton.getInstance().setAccessToken(accessToken);
            SessionSingleton.getInstance().setIdToken(idToken);
            Log.e(TAG, "onAuthentication: LOGIN INFO" );
            Log.e(TAG, "onAuthentication: acstoken " + accessToken );
            Log.e(TAG, "onAuthentication: idToken " + idToken);
            // TODO: 15/11/2016 login to luke, check username, change to new user screen if first time login
            LoginCallable loginTask = new LoginCallable();
            FutureTask<Boolean> booleanFutureTask = new FutureTask<Boolean>(loginTask);
            Thread thread = new Thread(booleanFutureTask);
            thread.start();
            try {
                if (booleanFutureTask.get()) {
                    // TODO: 15/11/2016 check username, if exists -> main screen, if not -> go to username creation
                    CheckUsernameTask checkUsernameTask = new CheckUsernameTask();
                    checkUsernameTask.execute();
                    SessionSingleton.getInstance().setUserLogged(true);
                    finish();
                } else {
                    System.out.println("jeenem");
                    finish();
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "onAuthentication: ", e);
            } catch (ExecutionException e) {
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
        if (lock != null) {
            lock.onDestroy(this);
            lock = null;
        }
    }

    private class SetupTask extends AsyncTask<Void, Void, Void> {
        String url;
        String jsonString;

        public SetupTask(String url) {
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
                        stringBuilder.append(line + "\n");
                    }
                    bufferedReader.close();
                    jsonString = stringBuilder.toString();
                } else {
                    //TODO: if error do something else, ERROR STREAM
                }
                httpURLConnection.disconnect();
            } catch (MalformedURLException e) {
                Log.e(TAG, "doInBackground: ", e);
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
            }
        }
    }

    private class LoginCallable implements Callable<Boolean> {
        HttpURLConnection httpURLConnection;

        @Override
        public Boolean call() throws Exception {
            try {
                URL lukeURL = new URL(getString(R.string.loginUrl));
                httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
                httpURLConnection.setRequestProperty(getString(R.string.authorization), getString(R.string.bearer) + idToken);
                httpURLConnection.setRequestProperty(getString(R.string.acstoken), accessToken);
                if (httpURLConnection.getResponseCode() == 200) {
                    return true;
                } else {
                    //TODO: if error do something else, ERROR STREAM
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

    private class CheckUsernameTask extends AsyncTask<Void, Void, Void> {
        private String uname;
        private String jsonString;
        HttpURLConnection httpURLConnection;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL lukeURL = new URL(getString(R.string.userUrl));
                httpURLConnection = (HttpURLConnection) lukeURL.openConnection();
                httpURLConnection.setRequestProperty(getString(R.string.authorization), getString(R.string.bearer) + idToken);
                httpURLConnection.setRequestProperty(getString(R.string.acstoken), accessToken);
                if (httpURLConnection.getResponseCode() == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    jsonString = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                    jsonString = stringBuilder.toString();
                    bufferedReader.close();

                } else {
                    //TODO: if error do something else, ERROR STREAM
                    Log.e(TAG, "doInBackground: vöörö");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
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
                if (jsonObject.has("image_url")) {//!TextUtils.isEmpty(jsonObject.getString("image_url"))) {
                    // TODO: 15/11/2016 parse url to bitmap
                }
                if (jsonObject.has("username")) {
                    SessionSingleton.getInstance().setUsername(uname);
                    // TODO: 15/11/2016 move to main activity
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    // TODO: 15/11/2016 move to username setting screen
                    startActivity(new Intent(getApplicationContext(), NewUserActivity.class));
                }

            } catch (JSONException e) {
                Log.e(TAG, "onPostExecute: ", e);
            }
        }
    }
}
