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
package com.luke.lukef.lukeapp;

import android.Manifest;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.auth0.android.Auth0;
import com.auth0.android.lock.AuthButtonSize;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.luke.lukef.lukeapp.model.Link;
import com.luke.lukef.lukeapp.model.Rank;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.popups.LinkPopup;
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
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Handles the login screen as well as logging in with Auth0
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
    TextView title;
    boolean dataServiceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        this.lukeNetUtils = new LukeNetUtils(getApplicationContext());
        this.loginButton = (Button) findViewById(R.id.loginButton);
        this.skipLoginButton = (Button) findViewById(R.id.skipLoginButton);
        this.title = (TextView) findViewById(R.id.textView);
        this.loginButton.setOnClickListener(this);
        this.skipLoginButton.setOnClickListener(this);
        requestPermission();
        if(LukeUtils.checkInternetStatus(this)) {
            startService(new Intent(this, SubmissionFetchService.class));
            this.dataServiceStarted = true;
            getCategories();
            getRanks();
            ShowLinkTask showLinkTask = new ShowLinkTask();
            showLinkTask.execute();
        }

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
                    SessionSingleton.getInstance().setUserLogged(false);if(!dataServiceStarted) {
                        startService(new Intent(WelcomeActivity.this, SubmissionFetchService.class));
                        getCategories();
                        getRanks();
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    }
                }

                break;
            case R.id.loginButton:
                if (LukeUtils.checkInternetStatus(this)) {
                    Auth0SetupTask auth0SetupTask = new Auth0SetupTask(getString(R.string.auth0URL));
                    auth0SetupTask.execute();

                }
                break;
        }
    }

    /**
     * Activates the Auth0 sign in process
     * @param clientId id of the Auth0 client
     * @param domain Auth0 domain
     */
    private void doLogin(String clientId, String domain) {
        Auth0 auth0 = new Auth0(clientId, domain);
        this.lock = Lock.newBuilder(auth0, this.callBack)
                .closable(true)
                .loginAfterSignUp(true)
                .withAuthButtonSize(AuthButtonSize.BIG)
                .build(this);
        startActivity(this.lock.newIntent(this));
    }

    /**
     * Gets called when the Auth0 login process is done
     */
    private final LockCallback callBack = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            accessToken = credentials.getAccessToken();
            idToken = credentials.getIdToken();
            SessionSingleton.getInstance().setAccessToken(accessToken);
            SessionSingleton.getInstance().setIdToken(idToken);
            Log.e(TAG, "onAuthentication: idToken " + idToken);
            lukeNetUtils.attemptLogin(WelcomeActivity.this, idToken);
            if(!dataServiceStarted) {
                startService(new Intent(WelcomeActivity.this, SubmissionFetchService.class));
                getCategories();
                getRanks();
            }

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
                Log.e(TAG, "onPostExecute: ",e );
            }
            if (parseCheck(auth0Domain, auth0ClienID)) {
                doLogin(auth0ClienID, auth0Domain);
                SessionSingleton.getInstance().setAuth0Domain(auth0Domain);
                SessionSingleton.getInstance().setAuth0ClientID(auth0ClienID);
            }
        }
    }

    /**
     * Start getCategories from LukeUtils on a new thread
     */
    private void getCategories() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (SessionSingleton.getInstance().getCategoryList().size() < 1) {
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

    /**
     * Start getAllRanks from LukeUtils on a new thread
     */
    private void getRanks() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                LukeNetUtils lukeNetUtils = new LukeNetUtils(WelcomeActivity.this);
                try {
                    ArrayList<Rank> ranks = lukeNetUtils.getAllRanks();
                    SessionSingleton.getInstance().setRanks(ranks);
                } catch (ExecutionException e) {
                    Log.e(TAG, "run: ",e );
                } catch (InterruptedException e) {
                    Log.e(TAG, "run: ",e );
                }

            }
        });
        t.start();
    }

    private class ShowLinkTask extends AsyncTask<Void,Void,Void>{

        Link link;
        @Override
        protected Void doInBackground(Void... params) {
            link = lukeNetUtils.getNewestLink();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(link != null){
                if(link.isActive()){
                    creaeLinkPopup(link);
                }
            }
            super.onPostExecute(aVoid);
        }
    }

    private void creaeLinkPopup(Link l){
        LinkPopup linkPopup = new LinkPopup(this,l);
    }

}
