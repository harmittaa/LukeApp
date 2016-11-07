package com.luke.lukef.lukeapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.luke.lukef.lukeapp.fragments.ConfirmationFragment;
import com.luke.lukef.lukeapp.fragments.LeaderboardFragment;
import com.luke.lukef.lukeapp.fragments.MapFragment;
import com.luke.lukef.lukeapp.fragments.NewSubmissionFragment;
import com.luke.lukef.lukeapp.fragments.PointOfInterestFragment;
import com.luke.lukef.lukeapp.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton midButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leftButton = (ImageButton)findViewById(R.id.button_left);
        rightButton = (ImageButton)findViewById(R.id.button_right);
        midButton = (ImageButton)findViewById(R.id.button_mid);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //activate map fragment as default
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
                //setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);

                break;
            case FRAGMENT_LEADERBOARD:
                LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
                fragmentTransaction.replace(R.id.fragment_container, leaderboardFragment).addToBackStack(null);
                fragmentTransaction.commit();
                //setBottomBarButtons(Constants.bottomActionBarStates.BACK_ONLY);
                break;
            case FRAGMENT_NEW_SUBMISSION:
                NewSubmissionFragment newSubmissionFragment = new NewSubmissionFragment();
                fragmentTransaction.replace(R.id.fragment_container, newSubmissionFragment).addToBackStack(null);
                fragmentTransaction.commit();
                //setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);
                break;
            case FRAGMENT_POINT_OF_INTEREST:
                PointOfInterestFragment pointOfInterestFragment = new PointOfInterestFragment();
                fragmentTransaction.replace(R.id.fragment_container, pointOfInterestFragment).addToBackStack(null);
                fragmentTransaction.commit();
                //setBottomBarButtons(Constants.bottomActionBarStates.BACK_ONLY);
                break;
            case FRAGMENT_PROFILE:
                ProfileFragment profileFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.fragment_container, profileFragment).addToBackStack(null);
                fragmentTransaction.commit();
                //setBottomBarButtons(Constants.bottomActionBarStates.BACK_ONLY);
                break;
            case FRAGMENT_MAP:
                Log.e(TAG, "fragmentSwitcher: SWITCH" );
                MapFragment mapFragment = new MapFragment();
                fragmentTransaction.replace(R.id.fragment_container, mapFragment);
                fragmentTransaction.commit();
                //setBottomBarButtons(Constants.bottomActionBarStates.MAP_CAMERA);
                break;
        }
    }

    /**
     * Switches bottom bar buttons depending on state, use in conjunction with fragment change
     * Tick button hidden depending on need
     * @param state Constants enum type defined for each different bottom bar
     */
    public void setBottomBarButtons(final Constants.bottomActionBarStates state){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (state){
                    case BACK_ONLY:
                        findViewById(R.id.bottomBarMap).setVisibility(View.GONE);
                        findViewById(R.id.bottomBarBackReport).setVisibility(View.GONE);
                        findViewById(R.id.bottomBarBackTick).setVisibility(View.VISIBLE);
                        findViewById(R.id.button_tick).setVisibility(View.GONE);
                        break;
                    case BACK_REPORT:
                        findViewById(R.id.bottomBarMap).setVisibility(View.GONE);
                        findViewById(R.id.bottomBarBackReport).setVisibility(View.VISIBLE);
                        findViewById(R.id.bottomBarBackTick).setVisibility(View.GONE);
                        break;
                    case BACK_TICK:
                        findViewById(R.id.bottomBarMap).setVisibility(View.GONE);
                        findViewById(R.id.bottomBarBackReport).setVisibility(View.GONE);
                        findViewById(R.id.bottomBarBackTick).setVisibility(View.VISIBLE);
                        findViewById(R.id.button_tick).setVisibility(View.VISIBLE);
                        break;
                    case MAP_CAMERA:
                        findViewById(R.id.bottomBarMap).setVisibility(View.VISIBLE);
                        findViewById(R.id.bottomBarBackReport).setVisibility(View.GONE);
                        findViewById(R.id.bottomBarBackTick).setVisibility(View.GONE);
                        break;
                }
            }
        });

    }
}
