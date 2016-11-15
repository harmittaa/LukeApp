package com.luke.lukef.lukeapp;

import android.os.AsyncTask;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button loginButton;
    private Button skipLoginButton;

    private Lock lock;
    private String idToken = "";
    private String accessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        loginButton = (Button)findViewById(R.id.loginButton);
        skipLoginButton = (Button)findViewById(R.id.skipLoginButton);

        loginButton.setOnClickListener(this);
        skipLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.skipLoginButton:
                //TODO: switch to mainactivity
                break;
            case R.id.loginButton:
                //// TODO: 15/11/2016 activate auth0
                SetupTask setupTask = new SetupTask(getString(R.string.auth0URL));
                setupTask.execute();
                break;
        }
    }

    /*

    AUTH0 STUFF


     */


    private void doLogin(String clientId, String domain) {
        Auth0 auth0 = new Auth0(clientId,domain);
        lock = Lock.newBuilder(auth0,callBack).build(this);
        startActivity(lock.newIntent(this));
    }

    private final LockCallback callBack = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            accessToken = credentials.getAccessToken();
            idToken = credentials.getIdToken();
        }

        @Override
        public void onCanceled() {

        }

        @Override
        public void onError(LockException error) {

        }
    };

    private class SetupTask extends AsyncTask<Void,Void,Void> {
        String url;
        String jsonString;
        public SetupTask(String url){
            this.url = url;
        }

        private boolean parseCheck(String domain, String clientID){
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
                    while((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line+"\n");
                    }
                    bufferedReader.close();
                    jsonString = stringBuilder.toString();
                } else {
                    //TODO: if error do something else, ERROR STREAM
                }
                httpURLConnection.disconnect();
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
            String auth0ClienID = "";
            String auth0Domain = "";

            try {
                jsonObject = new JSONObject(jsonString);
                auth0ClienID = jsonObject.getString("AUTH0_CLIENT_ID");
                auth0Domain = jsonObject.getString("AUTH0_DOMAIN");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(parseCheck(auth0Domain,auth0ClienID)){
                doLogin(auth0ClienID,auth0Domain);
            }
        }
    }

}
