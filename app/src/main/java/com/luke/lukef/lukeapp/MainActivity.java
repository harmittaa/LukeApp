package com.luke.lukef.lukeapp;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.luke.lukef.lukeapp.fragments.ConfirmationFragment;
import com.luke.lukef.lukeapp.fragments.LeaderboardFragment;
import com.luke.lukef.lukeapp.fragments.MapFragment;
import com.luke.lukef.lukeapp.fragments.NewSubmissionFragment;
import com.luke.lukef.lukeapp.fragments.PointOfInterestFragment;
import com.luke.lukef.lukeapp.fragments.ProfileFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        setBottomBarButtonsListeners();

    }


    @Override
    protected void onResume() {
        super.onResume();
        //activate map fragment as default
        fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP);
        checkPermissions();

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

    public void setBottomBarButtonsListeners(){
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
        findViewById(R.id.button_back1).setOnClickListener(cl);
        findViewById(R.id.button_back2).setOnClickListener(cl);
    }

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {

        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
