package com.luke.lukef.lukeapp.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.tools.CategoriesPopup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class NewSubmissionFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, Dialog.OnCancelListener {
    View fragmentView;
    EditText submissionTitle;
    EditText submissionDescription;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private HorizontalScrollView scrollView;
    ImageView photoThumbnail;
    ImageView mapThumbnail;
    private LinearLayout categoriesLinearLayout;
    Bitmap currentPhoto;
    Button categorySelectButton;
    private final static String TAG = NewSubmissionFragment.class.toString();
    ArrayList<String> selectedCategories;
    ArrayList<Category> confirmedCategories;
    ArrayList<Category> tempCategories;

    ImageButton submittt;
    ImageButton backButton;
    Location location;
    private File photoFile;
    private String photoPath;
    CategoriesPopup popMaker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_new_submission_2, container, false);

        submittt = (ImageButton) fragmentView.findViewById(R.id.button_tick_submit);
        backButton = (ImageButton)fragmentView.findViewById(R.id.button_back);
        backButton.setOnClickListener(this);
        this.categoriesLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.categoriesLinearLayout);
        submissionDescription = (EditText) fragmentView.findViewById(R.id.newSubmissionEditTextDescrption);
        submissionTitle = (EditText) fragmentView.findViewById(R.id.newSubmissionEditTextTitle);
        submissionDescription.setImeOptions(EditorInfo.IME_ACTION_DONE);
        submissionTitle.setImeOptions(EditorInfo.IME_ACTION_DONE);
        setupClickListeners();
        fetchBundleFromArguments();
        selectedCategories = new ArrayList<>();
        this.categorySelectButton = (Button) fragmentView.findViewById(R.id.categorySelectButtonNewSubmission);
        this.categorySelectButton.setOnClickListener(this);
        this.confirmedCategories = new ArrayList<>();
        this.tempCategories = new ArrayList<>();
        setupThumbnailMap();
        ViewTreeObserver vto = mapThumbnail.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getMapThumbnail(location, mapThumbnail.getWidth(), mapThumbnail.getHeight());
                photoThumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.e(TAG, "onGlobalLayout: photothumnailImageview dimensions:" + photoThumbnail.getWidth() + " x " + photoThumbnail.getHeight());
            }
        });
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createImageFile();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.categorySelectButtonNewSubmission:
                makeCategoryListPopup();
                break;
            case R.id.button_tick_submit:
                makeSubmission();
                break;
            case R.id.photoThumbnail:
                dispatchTakePictureIntent();
                break;
            case R.id.button_back:
                Log.e(TAG, "onClick: back now pls" );
                getMainActivity().onBackPressed();
                break;
            case R.id.categories_accept_button:
                updateCategoryThumbnails();
                this.popMaker.dismissCategoriesPopup();
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupClickListeners() {
        this.categoriesLinearLayout.setOnClickListener(this);
        submittt.setOnClickListener(this);
    }

    private void fetchBundleFromArguments() {
        Bundle b = getArguments();  // getMainActivity().getIntent().getExtras();
        if (b != null) {
            location = new Location("jes");
            location.setLatitude(b.getDouble("latitude"));
            location.setLongitude(b.getDouble("longitude"));
            location.setAltitude(b.getDouble("altitude"));
            Log.e(TAG, "onCreateView: bundle received: " + location.toString());
        }
    }


    private void setupThumbnailMap() {
        photoThumbnail = (ImageView) fragmentView.findViewById(R.id.photoThumbnail);
        mapThumbnail = (ImageView) fragmentView.findViewById(R.id.newSubmissionMapThumbnail);
        //getMapThumbnail(location,mapThumbnail.getWidth(),mapThumbnail.getHeight());
        photoThumbnail.setOnClickListener(this);
    }

    //activates luke_camera intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getMainActivity().getPackageManager()) != null) {
            // Create the File where the photo should go

            if (photoFile != null) {
                this.photoPath = photoFile.getAbsolutePath();
                // Continue only if the File was successfully created
                Uri photoURI = FileProvider.getUriForFile(getMainActivity(), "com.luke.lukef.lukeapp", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                createImageFile();
                dispatchTakePictureIntent();
            }
        }
    }

    private void createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getMainActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            Log.e(TAG, "createImageFile: ", e);
        }

        // Save a file: path for use with ACTION_VIEW intents
        this.photoPath = image.getAbsolutePath();
        this.photoFile = image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap imageBitmap = BitmapFactory.decodeFile(this.photoPath.toString(), options);
            try {
                Log.e(TAG, "onActivityResult: photo file before write" + this.photoFile.length());
                FileOutputStream fo = new FileOutputStream(this.photoFile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
                Log.e(TAG, "onActivityResult: photo file after write" + this.photoFile.length());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onActivityResult: ", e);
            }

            imageBitmap = BitmapFactory.decodeFile(photoPath, options);

            if (imageBitmap != null)
                Log.e(TAG, "onActivityResult: photo exists, size : " + imageBitmap.getByteCount());
            photoThumbnail.setImageBitmap(imageBitmap);
            this.currentPhoto = imageBitmap;
        }
    }


    private void getMapThumbnail(final Location center, final int width, final int height) {
        //https://maps.googleapis.com/maps/api/staticmap?center=29.390946,%2076.963502&zoom=10&size=600x300&maptype=normal
        final String urlString1 = "https://maps.googleapis.com/maps/api/staticmap?center=";
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString1 + center.getLatitude() + ",%20" + center.getLongitude() + "&zoom=18&size=" + width + "x" + height + "&maptype=normal");
                    Log.e(TAG, "run: MAPS COME FROM HERE " + url.toString());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    changeMapThumbnail(BitmapFactory.decodeStream(input));
                } catch (IOException e) {
                    // Log exception
                }
            }
        };

        Thread t = new Thread(r);
        t.start();
    }


    private void changeMapThumbnail(final Bitmap bm) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapThumbnail.setImageBitmap(bm);
            }
        });
    }


    private void makeSubmission() {
        if (checkFieldsValidity()) {
            // TODO: 22/11/2016 create submission object, make httprequest and send to server(put this request into submission?)
            Submission newSub = new Submission(getMainActivity(), this.confirmedCategories, new Date(), submissionDescription.getText().toString(), this.location);
            newSub.setFile(this.photoFile);
            if (currentPhoto != null) {
                newSub.setImage(this.currentPhoto);
            }
            if (newSub.submitToServer()) {
                Log.e(TAG, "makeSubmission: Submission sent succesfully");
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP, null);
                getMainActivity().makeToast("Success!");
            } else {
                getMainActivity().makeToast("Error Submitting");
            }
        } else {
            Log.e(TAG, "makeSubmission: FIELDS NOT VALID\nFIELDS NOT VALID");
        }
    }

    private boolean checkFieldsValidity() {
        // TODO: 22/11/2016 check if location != null , check if
        if (!TextUtils.isEmpty(submissionDescription.getText().toString())) {
            if (this.location != null) {
                if (this.confirmedCategories.size() > 0) {
                    return true;
                } else {
                    getMainActivity().makeToast("No categories selected.");
                    return false;
                }
            } else {
                getMainActivity().makeToast("No location found.");
                return false;
            }
        } else {
            getMainActivity().makeToast("Description is required.");
            return false;
        }
    }

    /**
     * Creates the categories pop up, takes a copy of the current confirmed categories in case
     * user cancels their action.
     */
    private void makeCategoryListPopup() {
        this.tempCategories.clear();
        this.tempCategories = new ArrayList<>(this.confirmedCategories);
        Log.e(TAG, "makeCategoryListPopup: confirmed size " + this.confirmedCategories.size());
        popMaker = new CategoriesPopup(getMainActivity(), this, this, this.confirmedCategories, this);
        popMaker.setupCategoriesPopup();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.popup_categories_checkbox);
        boolean checked = checkBox.isChecked();
        ((CheckBox) view.findViewById(R.id.popup_categories_checkbox)).setChecked(!checked);
        CategoriesPopup.ListViewAdapter listViewAdapter = (CategoriesPopup.ListViewAdapter) adapterView.getAdapter();

        if (!checked) {
            this.confirmedCategories.add(listViewAdapter.getItem(i));
        } else {
            this.confirmedCategories.remove(listViewAdapter.getItem(i));
        }

        Log.e(TAG, "onItemClick: confirmed size after change " + this.confirmedCategories.size());
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        Log.e(TAG, "onCancel: cancelled");
        Log.e(TAG, "makeCategoryListPopup: temp size " + this.tempCategories.size());
        this.confirmedCategories = new ArrayList<>(this.tempCategories);
    }



    /**
     *
     */
    private void updateCategoryThumbnails() {
        this.categoriesLinearLayout.removeAllViews();

        for (Category c : this.confirmedCategories) {
            ImageView categoryImg = new ImageView(this.getMainActivity());
            categoryImg.setImageBitmap(c.getImage());
            LinearLayout.LayoutParams make = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(this.categoriesLinearLayout.getHeight(), this.categoriesLinearLayout.getHeight()));
            categoryImg.setLayoutParams(make);

            //categoryImg.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            this.categoriesLinearLayout.addView(categoryImg);
            //this.categoriesLinearLayout.addView(button);
        }
    }
}
