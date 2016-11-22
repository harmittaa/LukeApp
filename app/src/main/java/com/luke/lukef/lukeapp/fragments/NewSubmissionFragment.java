package com.luke.lukef.lukeapp.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.NewUserActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.Submission;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NewSubmissionFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button cameraButton;
    Button categoryButton;
    EditText submissionTitle;
    EditText submissionDescription;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView photoThumbnail;
    MapView thumbnailMap;
    GeoPoint location;
    private final static String TAG = NewSubmissionFragment.class.toString();
    private Bitmap bitmap;
    private String mCurrentPhotoPath;
    ArrayList<Category> selectedCategries;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_new_submission, container, false);
        cameraButton = (Button) fragmentView.findViewById(R.id.activateCameraButton);
        categoryButton = (Button) fragmentView.findViewById(R.id.buttonCategory);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);
        this.setBottomButtonListeners();
        fetchBundleFromArguments();
        setupThumbnailMap();
        selectedCategries = new ArrayList<>();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmation_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_CONFIRMATION, null);
                break;
            case R.id.button_back1:
                getMainActivity().onBackPressed();
                break;
            case R.id.button_back2:
                getMainActivity().onBackPressed();
                break;
            case R.id.button_tick:
                // TODO: 18/11/2016 check the submission valididty, then submit it
                break;
            case R.id.activateCameraButton:
                // TODO: 18/11/2016 activate camera
                dispatchTakePictureIntent();
                break;
            case R.id.buttonCategory:
                makeCategoryListPopup();
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupButtons() {
        cameraButton.setOnClickListener(this);
        categoryButton.setOnClickListener(this);
    }

    private void fetchBundleFromArguments() {
        Bundle b = getArguments();  // getMainActivity().getIntent().getExtras();
        if (b != null) {
            location = new GeoPoint(b.getDouble("latitude"), b.getDouble("longitude"), b.getDouble("altitude"));
            Log.e(TAG, "onCreateView: bundle received: " + location.toString());
        }
    }

    private void setBottomButtonListeners() {
        LinearLayout v = getMainActivity().getBottomBar();
        final int childcount = v.getChildCount();
        for (int i = 0; i < childcount; i++) {
            View view = v.getChildAt(i);
            if (view instanceof Button || view instanceof ImageButton) {
                view.setOnClickListener(this);
            }
        }
    }

    private void setupThumbnailMap() {
        photoThumbnail = (ImageView) fragmentView.findViewById(R.id.photoThumbnail);
        thumbnailMap = (MapView) fragmentView.findViewById(R.id.thumbnailmap);
        thumbnailMap.setTileSource(TileSourceFactory.MAPNIK);
        /*thumbnailMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
*/

        thumbnailMap.setBuiltInZoomControls(true);
        IMapController mapController = thumbnailMap.getController();
        mapController.setZoom(100);
        if (location != null) {
            Log.e(TAG, "setupThumbnailMap: geopoint is : " + location);
            mapController.setCenter(location);
        } else {
            mapController.setCenter(new GeoPoint(60.0, 25.0));
        }

    }

    //activates camera intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getMainActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getMainActivity(), "com.luke.lukef.lukeapp", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photoThumbnail.setImageBitmap(imageBitmap);
            Log.e(TAG, "onActivityResult: size of image: " + imageBitmap.getHeight() + " : " + imageBitmap.getWidth());
            Log.e(TAG, "onActivityResult: size of photothmbnail: " + photoThumbnail.getHeight() + " : " + photoThumbnail.getWidth());
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "lukeImage";//"JPEG_" + timeStamp + "_";
        File storageDir = getMainActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private Bitmap getBitmapFromStorage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap b = BitmapFactory.decodeFile(this.mCurrentPhotoPath, options);
        return b;
    }

    private void makeSubmission() {
        if(checkFieldsValidity()){
            // TODO: 22/11/2016 create submission object, make httprequest and send to server(put this request into submission?)
        }
    }

    private boolean checkFieldsValidity() {
        // TODO: 22/11/2016 check if location != null , check if
        if (!TextUtils.isEmpty(submissionDescription.getText().toString())) {
            if (location != null) {
                if (selectedCategries.size() < 1) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    private void makeCategoryListPopup() {
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(getMainActivity());
        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select A Category");

        final CategoryListAdapter cla = new CategoryListAdapter(getMainActivity(), android.R.layout.select_dialog_singlechoice, SessionSingleton.getInstance().getCategoryList());

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(cla, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NewSubmissionFragment.this.selectedCategries.add(cla.getItem(which));
                Log.e(TAG, "onClick: added to selected: " + cla.getItem(which) + " size now at " + selectedCategries.size());
                dialog.dismiss();
            }
        });
        builderSingle.create();
        builderSingle.show();
    }

    private class CategoryListAdapter extends ArrayAdapter<Category> {


        public CategoryListAdapter(Context context, int resource) {
            super(context, resource);
        }

        public CategoryListAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public CategoryListAdapter(Context context, int resource, Category[] objects) {
            super(context, resource, objects);
        }

        public CategoryListAdapter(Context context, int resource, int textViewResourceId, Category[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public CategoryListAdapter(Context context, int resource, List<Category> objects) {
            super(context, resource, objects);
        }

        public CategoryListAdapter(Context context, int resource, int textViewResourceId, List<Category> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            // Replace text with my own
            view.setText(getItem(position).getTitle());
            return view;
        }


    }
}
