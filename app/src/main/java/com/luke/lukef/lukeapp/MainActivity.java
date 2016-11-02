package com.luke.lukef.lukeapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.luke.lukef.lukeapp.fragments.ConfirmationFragment;
import com.luke.lukef.lukeapp.fragments.LeaderboardFragment;
import com.luke.lukef.lukeapp.fragments.MapFragment;
import com.luke.lukef.lukeapp.fragments.NewSubmissionFragment;
import com.luke.lukef.lukeapp.fragments.PointOfInterestFragment;
import com.luke.lukef.lukeapp.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // switch to MAP fragment for testing
    }


    @Override
    protected void onResume() {
        super.onResume();
        fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP);

    }

    /**
     * Switches the <code>fragment_container</code> Relative Layout from activity_main.xml to the
     * fragment which is chosen.
     * @param fragmentToChange Constants enum type defined for each fragment
     */
    public void fragmentSwitcher(Constants.fragmentTypes fragmentToChange){
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // cases are enumerations
        switch (fragmentToChange){
            case FRAGMENT_CONFIRMATION:
                // create the fragment object
                ConfirmationFragment confirmationFragment = new ConfirmationFragment();
                // create the transaction to switch what ever is in the container to the fragment
                fragmentTransaction.replace(R.id.fragment_container, confirmationFragment);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_LEADERBOARD:
                LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
                fragmentTransaction.replace(R.id.fragment_container, leaderboardFragment);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_NEW_SUBMISSION:
                NewSubmissionFragment newSubmissionFragment = new NewSubmissionFragment();
                fragmentTransaction.replace(R.id.fragment_container, newSubmissionFragment);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_POINT_OF_INTEREST:
                PointOfInterestFragment pointOfInterestFragment = new PointOfInterestFragment();
                fragmentTransaction.replace(R.id.fragment_container, pointOfInterestFragment);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_PROFILE:
                ProfileFragment profileFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.fragment_container, profileFragment);
                fragmentTransaction.commit();
                break;
            case FRAGMENT_MAP:
                Log.e(TAG, "fragmentSwitcher: SWITCH" );
                MapFragment mapFragment = new MapFragment();
                fragmentTransaction.replace(R.id.fragment_container, mapFragment);
                fragmentTransaction.commit();
                break;
        }
    }
}
