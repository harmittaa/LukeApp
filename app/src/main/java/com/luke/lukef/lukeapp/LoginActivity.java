package com.luke.lukef.lukeapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.luke.lukef.lukeapp.fragments.Auth0Fragment;
import com.luke.lukef.lukeapp.fragments.ConfirmationFragment;
import com.luke.lukef.lukeapp.fragments.LeaderboardFragment;
import com.luke.lukef.lukeapp.fragments.MapFragment;
import com.luke.lukef.lukeapp.fragments.NewSubmissionFragment;
import com.luke.lukef.lukeapp.fragments.PointOfInterestFragment;
import com.luke.lukef.lukeapp.fragments.ProfileFragment;
import com.luke.lukef.lukeapp.fragments.UserDataInputFragment;
import com.luke.lukef.lukeapp.fragments.WelcomeFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fragmentSwitcherLogin(Constants.loginFragmentTypes.LOGIN_FRAGMENT_WELCOME);
    }

    public void fragmentSwitcherLogin(Constants.loginFragmentTypes fragmentToChange){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        switch (fragmentToChange){
            case LOGIN_FRAGMENT_AUTH0:
                fragment = new Auth0Fragment();
                break;
            case LOGIN_FRAGMENT_USERDATA:
                fragment = new UserDataInputFragment();
                break;
            case LOGIN_FRAGMENT_WELCOME:
                fragment = new WelcomeFragment();
                break;
        }
        if(fragment != null) {
            fragmentTransaction.replace(R.id.fragment_container_login, fragment);
            fragmentTransaction.commit();
        }
    }
}
