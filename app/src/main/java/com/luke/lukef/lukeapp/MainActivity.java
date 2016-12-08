package com.luke.lukef.lukeapp;

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
import android.support.v4.content.ContextCompat;
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
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.luke.lukef.lukeapp.fragments.AchievementFragment;
import com.luke.lukef.lukeapp.fragments.ConfirmationFragment;
import com.luke.lukef.lukeapp.fragments.LeaderboardFragment;
import com.luke.lukef.lukeapp.fragments.MapViewFragment;
import com.luke.lukef.lukeapp.fragments.NewSubmissionFragment;
import com.luke.lukef.lukeapp.fragments.PointOfInterestFragment;
import com.luke.lukef.lukeapp.fragments.ProfileFragment;
import com.luke.lukef.lukeapp.fragments.UserSubmissionFragment;

import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;

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


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageButton leftButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private int progressStatus;
    private ProgressBar progressBar;
    private ImageView fullScreenImageView;
    private boolean fullsScreenIsActive = false;


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

        setBottomBarButtonsListeners();


        getCategories();

        this.fullScreenImageView = (ImageView) findViewById(R.id.fullscreenImage);

        //activate map fragment as default
        fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP, null);
        setStatusBarFlag();

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
            case FRAGMENT_POINT_OF_INTEREST:
                fragment = new PointOfInterestFragment();
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
        if (fragment != null) {
            if (bundleToSend != null) {
                fragment.setArguments(bundleToSend);
            }
            if (addToBackStack) {
                fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack("BackStack").commit();

            } else {
                fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
            }
        }
    }

    public void setFullScreenImageViewImage(final Bitmap b) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.fullScreenImageView.setImageBitmap(b);
            }
        });
    }

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

    private void setStatusBarFlag() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    private Bundle constructBundleFromMap(MapViewFragment mf) {
        Bundle bundle = new Bundle();
        Location gettedLoc = mf.getLastLoc();
        bundle.putDouble("latitude", gettedLoc.getLatitude());
        bundle.putDouble("longitude", gettedLoc.getLongitude());
        bundle.putDouble("altitude", gettedLoc.getAltitude());
        return bundle;
    }

    public void makeSubmission() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        Bundle bundleToSend = null;
        if (getCurrentFragment(fragmentManager) instanceof MapViewFragment) {
            bundleToSend = constructBundleFromMap((MapViewFragment) getCurrentFragment(fragmentManager));
        }
        fragment = new NewSubmissionFragment();
        if (fragment != null) {
            if (bundleToSend != null) {
                fragment.setArguments(bundleToSend);
            }
            fragmentTransaction.replace(R.id.fragment_container, fragment).addToBackStack("BackStack").commit();
        }
    }

    private Fragment getCurrentFragment(FragmentManager fm) {
        return fm.findFragmentById(R.id.fragment_container);
    }

    public void setBottomBarButtonsListeners() {
        View.OnClickListener clBack = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
        View.OnClickListener clNewSub = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        };
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
            //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
                } else if (location) {
                    //Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    //Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //Implementation of Navigation Drawer
    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (fullsScreenIsActive) {
            this.setFullScreenImageViewVisibility(false);
            Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof MapViewFragment) {
                ((MapViewFragment)f).unhidePopup();
            }
        } else {
            Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof MapViewFragment) {
                makeExitConfirmationPopup();
            } else {
                super.onBackPressed();
            }
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
                            default:
                                fragmentClass = MapViewFragment.class;
                        }
                        try {
                            fragment = (Fragment) fragmentClass.newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // Insert the fragment by replacing any existing fragment
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack("BackStack").commit();
                        // Highlight the selected item has been done by NavigationView
                        item.setChecked(true);

                        // Close the navigation drawer after select item
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });
    }

    public void makeToast(String toastString) {
        Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
    }

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

    /**
     * Fetches categories from the server, parses them and adds new ones to the {@link SessionSingleton#getCategoryList()}
     */
    private void getCategories() {
        Runnable checkUsernameRunnable = new Runnable() {
            String jsonString;

            @Override
            public void run() {
                try {
                    URL categoriesUrl = new URL("http://www.balticapp.fi/lukeA/category");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) categoriesUrl.openConnection();
                    if (httpURLConnection.getResponseCode() == 200) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        jsonString = "";
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();
                        JSONArray jsonArr;
                        jsonArr = new JSONArray(jsonString);
                        parseCategories(jsonArr);
                    } else {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                        jsonString = "";
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        jsonString = stringBuilder.toString();
                        Log.e(TAG, "run: ERROR WITH CATEGORIES : " + jsonString);
                        Toast.makeText(getApplicationContext(), "Couldn't fetch categories", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing exception");
                }
            }

            /**
             * Parses {@link com.luke.lukef.lukeapp.model.Category} objects from the provided <code>JSONArray</code>.
             * Compares the fetched categories to the existing categories, adds new discards old.
             * @param jsonArr The JSONArray fetched from server.
             */
            private void parseCategories(JSONArray jsonArr) {
                try {
                    List<Category> tempCategoryList = new ArrayList<>();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject jsonCategory = jsonArr.getJSONObject(i);
                        // check that the object has ID tag
                        if (jsonCategory.has("id")) {
                            Boolean found = false;
                            // loop through the SessionSingleton's Categories list and see if the category is already there
                            for (Category ca : SessionSingleton.getInstance().getCategoryList()) {
                                if (ca.getId().equals(jsonCategory.getString("id"))) {
                                    found = true;
                                }
                            }
                            // if the category doesn't exist yet on the list, then create it and add it to temp list
                            if (!found) {
                                Category c = new Category();
                                c.setId(jsonCategory.getString("id"));
                                if (jsonCategory.has("description")) {
                                    c.setDescription(jsonCategory.getString("description"));
                                } else {
                                    c.setDescription("No description");
                                }
                                if (jsonCategory.has("title")) {
                                    c.setTitle(jsonCategory.getString("title"));
                                } else {
                                    c.setTitle("No title");
                                }
                                Bitmap bitmap = null;
                                if (jsonCategory.has("image_url")) {
                                    String imageUrl = jsonCategory.getString("image_url");
                                    try {
                                        InputStream in = new URL(imageUrl).openStream();
                                        bitmap = BitmapFactory.decodeStream(in);
                                    } catch (MalformedURLException e) {
                                        // Error downloading / parsing the image, setting to default
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_category_image);
                                    } catch (IOException e) {
                                        Log.e(TAG, "parseCategories: IOException ", e);
                                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_category_image);
                                    }
                                } else {
                                    // there was no image for the category, setting default
                                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_category_image);
                                }
                                c.setImage(bitmap);
                                tempCategoryList.add(c);
                            }
                        }
                    }
                    if (!tempCategoryList.isEmpty()) {
                        // add the temporary list to the SubmissionSingleton's list
                        SessionSingleton.getInstance().getCategoryList().addAll(tempCategoryList);
                    } else {
                        Log.e(TAG, "parseCategories: no new categories to add");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onPostExecute: ", e);
                }
            }
        };
        Thread thread = new Thread(checkUsernameRunnable);
        thread.start();
    }
}
