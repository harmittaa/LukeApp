package com.luke.lukef.lukeapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.LoginActivity;
import com.luke.lukef.lukeapp.R;

import static android.content.ContentValues.TAG;


public class WelcomeFragment extends Fragment implements View.OnClickListener{

    View fragmentView;
    Button loginButton;
    Button skipLoginButton;

    public WelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: welcome created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_welcome, container, false);


        return fragmentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.skipLoginButton:
                //TODO: switch to mainactivity
                break;
            case R.id.loginButton:
                getLoginActivity().fragmentSwitcherLogin(Constants.loginFragmentTypes.LOGIN_FRAGMENT_AUTH0);
                break;
        }
    }

    private LoginActivity getLoginActivity(){
        return (LoginActivity)getActivity();
    }
}
