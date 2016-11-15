package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.luke.lukef.lukeapp.LoginActivity;
import com.luke.lukef.lukeapp.R;


public class Auth0Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    View fragmentView;

    public Auth0Fragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auth0, container, false);
    }

    private LoginActivity getLoginActivity(){
        return (LoginActivity)getActivity();
    }

}
