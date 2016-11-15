package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.auth0.android.Auth0;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.LoginActivity;
import com.luke.lukef.lukeapp.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Auth0Fragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    View fragmentView;
    Button backButton;

    private Lock lock;
    private String idToken = "";
    private String accessToken = "";
    private LoginActivity context;

    public Auth0Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getLoginActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_auth0, container, false);
        backButton = (Button)fragmentView.findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        SetupTask setupTask = new SetupTask(getString(R.string.auth0URL));
        setupTask.execute();
    }


    private void doLogin(String clientId, String domain) {
        if(!isAdded()){
            Log.e("jeeben", "doLogin:   NOT ADDED NOT ADDED " );
        }
        Auth0 auth0 = new Auth0(clientId,domain);
        lock = Lock.newBuilder(auth0,callBack).build(context);
        startActivity(lock.newIntent(context));
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

    private LoginActivity getLoginActivity(){
        return (LoginActivity)getActivity();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backButton:
                getLoginActivity().fragmentSwitcherLogin(Constants.loginFragmentTypes.LOGIN_FRAGMENT_WELCOME);
                break;
        }
    }
    private class SetupTask extends AsyncTask<Void,Void,Void>{
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
