package com.luke.lukef.lukeapp.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
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

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_new_submission, container, false);
        cameraButton = (Button) fragmentView.findViewById(R.id.activateCameraButton);
        categoryButton = (Button) fragmentView.findViewById(R.id.buttonCategory);
        setupButtons();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);
        this.setBottomButtonListeners();
        setupThumbnailMap();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmation_button:
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_CONFIRMATION);
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

    private void setupThumbnailMap(){
        photoThumbnail = (ImageView)fragmentView.findViewById(R.id.photoThumbnail);
        thumbnailMap = (MapView)fragmentView.findViewById(R.id.thumbnailmap);
        thumbnailMap.setTileSource(TileSourceFactory.MAPNIK);
        thumbnailMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }

    //activates camera intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getMainActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photoThumbnail.setImageBitmap(imageBitmap);
        }
    }

}
