package com.luke.lukef.lukeapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.luke.lukef.lukeapp.model.SessionSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NewUserActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText username;
    private Button confirmButton;
    private String usernameString;
    private static final String TAG = "NewUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        confirmButton = (Button) findViewById(R.id.newUserConfirmButton);
        confirmButton.setOnClickListener(this);
        username = (EditText) findViewById(R.id.newUserName);
        username.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (checkUsernameValid(username.getText().toString())) {
                    confirmUsername(username.getText().toString());
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newUserConfirmButton:
                if (checkUsernameValid(username.getText().toString())) {
                    confirmUsername(username.getText().toString());
                }
                break;
        }
    }

    private boolean checkUsernameValid(String uname) {
        if (!TextUtils.isEmpty(uname)) {
            if (uname.length() > 3 && uname.length() < 10) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void confirmUsername(final String usernameToCheck) {
        Log.e(TAG, "confirmUsername: clickd");
        Runnable checkUsernameRunnable = new Runnable() {
            String jsonString;

            @Override
            public void run() {
                try {
                    URL checkUsernameUrl = new URL("http://www.balticapp.fi/lukeA/user/available?username=" + usernameToCheck);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) checkUsernameUrl.openConnection();
                    httpURLConnection.setRequestProperty(getString(R.string.authorization), getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                    httpURLConnection.setRequestProperty(getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
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
                        JSONObject jsonObject;
                        try {
                            jsonObject = new JSONObject(jsonString);
                            if (!jsonObject.getBoolean("exists")) {
                                URL setUsernameUrl = new URL("http://www.balticapp.fi/lukeA/user/set-username?username=" + usernameToCheck);
                                httpURLConnection = (HttpURLConnection) setUsernameUrl.openConnection();
                                httpURLConnection.setRequestProperty(getString(R.string.authorization), getString(R.string.bearer) + SessionSingleton.getInstance().getIdToken());
                                httpURLConnection.setRequestProperty(getString(R.string.acstoken), SessionSingleton.getInstance().getAccessToken());
                                if (httpURLConnection.getResponseCode() == 200) {
                                    // TODO: 17/11/2016 success, move to main screen
                                    NewUserActivity.this.startActivity(new Intent(NewUserActivity.this, MainActivity.class));

                                } else {
                                    // TODO: 17/11/2016 display errormessage for each type of error

                                }
                            } else {
                                // TODO: 17/11/2016 make alert that username is taken

                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onPostExecute: ", e);
                        }
                    } else {

                    }
                } catch (MalformedURLException e) {
                    Log.e(TAG, "doInBackground: ", e);
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: ", e);
                }
            }
        };

        Thread thread = new Thread(checkUsernameRunnable);
        thread.start();

    }
}
