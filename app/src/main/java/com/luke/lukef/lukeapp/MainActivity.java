package com.luke.lukef.lukeapp;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.luke.lukef.lukeapp.fragments.AchievementFragment;
import com.luke.lukef.lukeapp.fragments.ConfirmationFragment;
import com.luke.lukef.lukeapp.fragments.LeaderboardFragment;
import com.luke.lukef.lukeapp.fragments.MapFragment;
import com.luke.lukef.lukeapp.fragments.NewSubmissionFragment;
import com.luke.lukef.lukeapp.fragments.PointOfInterestFragment;
import com.luke.lukef.lukeapp.fragments.ProfileFragment;
import com.luke.lukef.lukeapp.fragments.UserSubmissionFragment;

import static android.R.id.progress;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton midButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private int progressStatus;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup navigation drawer view
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);
        Menu menu = navigationView.getMenu();
        //Notification switch handler
        MenuItem menuItem = menu.findItem(R.id.notification);
        View actionView = MenuItemCompat.getActionView(menuItem);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Do the function here
            }
        });

        //Custom header in Navigation Drawer
        View header = navigationView.getHeaderView(0);
        progressStatus = 25;
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null);
        progressBar = (ProgressBar) header.findViewById(R.id.progressbar1);
        // Main Progress
        progressBar.setProgress(progressStatus);
        // Maximum Progress
        progressBar.setMax(100);
        progressBar.setProgressDrawable(drawable);

        //Animation when drawing process
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, progressStatus);
        animation.setDuration(990);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        leftButton = (ImageButton) findViewById(R.id.button_left);
        rightButton = (ImageButton) findViewById(R.id.button_right);
        midButton = (ImageButton) findViewById(R.id.button_mid);

        setBottomBarButtonsListeners();
        leftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView);
            }
        });

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
     *
     * @param fragmentToChange Constants enum type defined for each fragment
     */
    public void fragmentSwitcher(Constants.fragmentTypes fragmentToChange) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // cases are enumerations
        switch (fragmentToChange) {
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
                Log.e(TAG, "fragmentSwitcher: SWITCH");
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
     *
     * @param state Constants enum type defined for each different bottom bar
     */
    public void setBottomBarButtons(final Constants.bottomActionBarStates state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (state) {
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

    public void setBottomBarButtonsListeners() {
        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
        findViewById(R.id.button_back1).setOnClickListener(cl);
        findViewById(R.id.button_back2).setOnClickListener(cl);
    }

    //Implementation of Navigation Drawer
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Navigating between Menu Items
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @SuppressWarnings("StatementWithEmptyBody")
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        // Create a new fragment and specify the fragment to show based on nav item clicked
                        Fragment fragment = null;
                        Class fragmentClass;
                        // Handle navigation view item clicks here.
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.achievements:
                                fragmentClass = AchievementFragment.class;
                                break;
                            case R.id.my_findings:
                                fragmentClass = UserSubmissionFragment.class;
                                break;
                            case R.id.leaderboard:
                                fragmentClass = LeaderboardFragment.class;
                                break;
//                            case R.id.notification:
//                                fragmentSwitcher();
//                                break;
                            default:
                                fragmentClass = MapFragment.class;
                        }
                        try {
                            fragment = (Fragment) fragmentClass.newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Insert the fragment by replacing any existing fragment
                        // FragmentManager fragmentManager = getFragmentManager();
                        // fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                        // Highlight the selected item has been done by NavigationView
                        item.setChecked(true);

                        // Close the navigation drawer after select item
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
