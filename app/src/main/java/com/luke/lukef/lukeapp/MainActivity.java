package com.luke.lukef.lukeapp;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;

import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.luke.lukef.lukeapp.fragments.ConfirmationFragment;
import com.luke.lukef.lukeapp.fragments.LeaderboardFragment;
import com.luke.lukef.lukeapp.fragments.MapViewFragment;
import com.luke.lukef.lukeapp.fragments.NewSubmissionFragment;
import com.luke.lukef.lukeapp.fragments.ProfileFragment;

import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;


/**
 * Main activity of the app. Contains a view where fragments are cycled. The fragments contain all the functional parts. Also contains logic for drawer
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private int progressStatus;
    private ProgressBar progressBar;
    private ImageView fullScreenImageView;
    private ImageView drawerUserProfileImage;
    private TextView drawerUsername;
    private boolean fullsScreenIsActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup navigation drawer view
        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.fullScreenImageView = (ImageView) findViewById(R.id.fullscreenImage);
        //Menu menu = this.navigationView.getMenu();

        View hView = navigationView.getHeaderView(0);
        this.drawerUsername = (TextView) hView.findViewById(R.id.drawerUsername);
        this.drawerUserProfileImage = (ImageView) hView.findViewById(R.id.drawerUserProfileImage);


        //Custom header in Navigation Drawer
        View header = this.navigationView.getHeaderView(0);
        this.progressStatus = 25;
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.progress_bar, null);
        this.progressBar = (ProgressBar) header.findViewById(R.id.progressbar1);
        // Main Progress
        this.progressBar.setProgress(progressStatus);
        // Maximum Progress
        this.progressBar.setMax(100);
        this.progressBar.setProgressDrawable(drawable);

        //Animation when drawing process
        ObjectAnimator animation = ObjectAnimator.ofInt(this.progressBar, "progress", 0, this.progressStatus);
        animation.setDuration(990);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();


        setupDrawerContent(this.navigationView);
        //activate map fragment as default
        fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    /**
     * Switches the <code>fragment_container</code> Relative Layout from activity_main.xml to the
     * fragment which is chosen.
     *
     * @param fragmentToChange Constants enum type defined for each fragment
     * @param bundleToSend     Optional bundle to send along with the transaction
     */
    public void fragmentSwitcher(Constants.fragmentTypes fragmentToChange, Bundle bundleToSend) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        boolean addToBackStack = false;
        // cases are enumerations
        switch (fragmentToChange) {
            case FRAGMENT_CONFIRMATION:
                // create the fragment object
                fragment = new ConfirmationFragment();
                addToBackStack = true;
                break;
            case FRAGMENT_LEADERBOARD:
                fragment = new LeaderboardFragment();
                addToBackStack = true;
                break;
            case FRAGMENT_NEW_SUBMISSION:
                if (getCurrentFragment(fragmentManager) instanceof MapViewFragment) {
                    bundleToSend = constructBundleFromMap((MapViewFragment) getCurrentFragment(fragmentManager));
                }
                fragment = new NewSubmissionFragment();
                addToBackStack = true;
                break;
            case FRAGMENT_PROFILE:
                fragment = new ProfileFragment();
                addToBackStack = true;
                break;
            case FRAGMENT_MAP:
                fragment = new MapViewFragment();
                addToBackStack = false;
                break;
        }
        //replace the fragment
        if (bundleToSend != null) {
            fragment.setArguments(bundleToSend);
        }
        if (addToBackStack) {
            fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack("BackStack").commit();

        } else {
            fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
        }
    }

    /**
     * Shows the given image as full screen
     *
     * @param b The Bitmap to be shown
     */
    public void setFullScreenImageViewImage(final Bitmap b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fullScreenImageView.setImageBitmap(b);
            }
        });
    }


    /**
     * Toggles between hiding and showing a fullscreen image, when its clicked in a popup. Unhides the popup once done
     * @param isVisible whether the image is shown fullscreen, true = is shown, false = hidden
     */
    public void setFullScreenImageViewVisibility(final boolean isVisible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fullsScreenIsActive = isVisible;
                if (isVisible) {
                    MainActivity.this.fullScreenImageView.setVisibility(View.VISIBLE);
                    MainActivity.this.findViewById(R.id.fragment_container).setVisibility(View.GONE);
                    if (MainActivity.this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        MainActivity.this.drawerLayout.closeDrawer(GravityCompat.START);
                    }
                } else {
                    MainActivity.this.fullScreenImageView.setVisibility(View.GONE);
                    MainActivity.this.findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
                    if (MainActivity.this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        MainActivity.this.drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
            }
        });
    }

    /**
     * Constructs a bundle froma all values stored in a Mapfragment, to be passed to another fragment
     * @param mf Mapfragment from which the bundle will be constructed.
     * @return
     */
    private Bundle constructBundleFromMap(MapViewFragment mf) {
        Bundle bundle = new Bundle();
        Location gettedLoc = mf.getLastLoc();
        bundle.putDouble("latitude", gettedLoc.getLatitude());
        bundle.putDouble("longitude", gettedLoc.getLongitude());
        bundle.putDouble("altitude", gettedLoc.getAltitude());
        return bundle;
    }

    /**
     * Handles making submission when user has long pressed on the map.
     */
    public void makeSubmission() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment;
        Bundle bundleToSend = null;
        if (getCurrentFragment(fragmentManager) instanceof MapViewFragment) {
            bundleToSend = constructBundleFromMap((MapViewFragment) getCurrentFragment(fragmentManager));
        }
        fragment = new NewSubmissionFragment();
        if (bundleToSend != null) {
            fragment.setArguments(bundleToSend);
        }
        fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack("BackStack").commit();
    }

    private Fragment getCurrentFragment(FragmentManager fm) {
        return fm.findFragmentById(R.id.fragment_container);
    }

    // TODO: 12/12/2016 DANIEL move ot LukeUtils?
    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Implementation of Navigation Drawer
    @Override
    public void onBackPressed() {
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (fullsScreenIsActive) {
            this.setFullScreenImageViewVisibility(false);
            ((MapViewFragment) f).unhidePopup();
        } else {
            if (f instanceof MapViewFragment) {
                makeExitConfirmationPopup();
            } else if (f instanceof ProfileFragment) {
                //this is to avoid map lag
                fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP, null);
            } else {
                super.onBackPressed();
            }
        }
    }

    public void openDrawer() {
        this.drawerLayout.openDrawer(GravityCompat.START);
    }

    //Navigating between Menu Items
    private void setupDrawerContent(NavigationView navigationView) {
        setDrawerUserData();

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @SuppressWarnings("StatementWithEmptyBody")
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        // Handle navigation view item clicks here.
                        int id = item.getItemId();
                        switch (id) {
                            case R.id.my_profile:
                                if (SessionSingleton.getInstance().isUserLogged()) {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("userId", SessionSingleton.getInstance().getUserId());
                                    fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_PROFILE, bundle);
                                } else {
                                    createLoginPrompt();
                                }
                                break;
                            case R.id.logout:
                                if (SessionSingleton.getInstance().isUserLogged()) {
                                    SessionSingleton.getInstance().logOut(MainActivity.this);
                                } else {
                                    createLoginPrompt();
                                }
                                break;
                            case R.id.edit_profile:
                                if (SessionSingleton.getInstance().isUserLogged()) {
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("isEditing", true);
                                    Intent intent = new Intent(MainActivity.this, NewUserActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                } else {
                                    createLoginPrompt();
                                }
                                break;
                            default:
                        }
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });
    }

    /**
     * Creates a login prompt
     */
    private void createLoginPrompt() {
        // TODO: 10/12/2016 MOVE TO LUKEUTILS this is also used in MapFragment
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.login_prompt_message)
                .setCancelable(false)
                .setPositiveButton("Log in", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getBaseContext(), WelcomeActivity.class));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    /**
     * Sets user data to the drawer
     */
    private void setDrawerUserData() {
        Bitmap b;
        if (!TextUtils.isEmpty(SessionSingleton.getInstance().getIdToken())) {
            this.drawerUsername.setText(SessionSingleton.getInstance().getUsername());
            if (SessionSingleton.getInstance().getUserImage() != null) {
                b = SessionSingleton.getInstance().getUserImage();
            } else {
                b = BitmapFactory.decodeResource(this.getResources(), R.drawable.luke_default_profile_pic);
            }
        } else {
            this.drawerUsername.setText(getResources().getText(R.string.not_logged_in));
            b = BitmapFactory.decodeResource(this.getResources(), R.drawable.luke_default_profile_pic);
        }
        this.drawerUserProfileImage.setImageBitmap(b);
    }

    public void makeToast(String toastString) {
        Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles creating an exit confirmation pop up with buttons Yes and No.
     */
    public void makeExitConfirmationPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Application?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                        finishAffinity();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (android.support.v4.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}
