package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Submission;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
                Uri photoURI = FileProvider.getUriForFile(getMainActivity(),"com.luke.lukef.lukeapp",photoFile);
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

    private Bitmap getBitmapFromStorage(){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap b = BitmapFactory.decodeFile(this.mCurrentPhotoPath, options);
        return b;
    }

    private void makeSubmission() {

    }

    private void checkFieldsValidity(){
        // TODO: 22/11/2016 check if location != null , check if
    }

}
