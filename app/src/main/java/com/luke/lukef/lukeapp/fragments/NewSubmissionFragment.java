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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.tools.SubmissionFetchService;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.Submission;
import com.luke.lukef.lukeapp.popups.CategoriesPopup;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;

/**
 * Handles the new submission screen, includes sending the submissions, dispatching the camera intent
 * and listening to the category pop up clicks.
 */
public class NewSubmissionFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, Dialog.OnCancelListener {
    private final static String TAG = NewSubmissionFragment.class.toString();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String photoPath;
    private ArrayList<Category> confirmedCategories;
    private ArrayList<Category> tempCategories;
    private Location location;
    private File photoFile;
    private Bitmap currentPhoto;

    private Button categorySelectButton;
    private View fragmentView;
    private EditText submissionDescription;
    private EditText submissionTitle;
    private ImageView photoThumbnail;
    private ImageView mapThumbnail;
    private LinearLayout categoriesLinearLayout;
    private ImageButton makeSubmissionButton;
    private ImageButton backButton;
    private CategoriesPopup popMaker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.fragment_new_submission, container, false);
        // find the views and add listeners
        findViews();
        setupClickListeners();
        fetchBundleFromArguments();

        ArrayList<String> selectedCategories = new ArrayList<>();
        this.categorySelectButton.setOnClickListener(this);
        this.confirmedCategories = new ArrayList<>();
        this.tempCategories = new ArrayList<>();

        ViewTreeObserver vto = this.mapThumbnail.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final LukeNetUtils lukeNetUtils = new LukeNetUtils(getMainActivity());
                getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mapThumbnail.setImageBitmap(lukeNetUtils.getMapThumbnail(location, mapThumbnail.getWidth(), mapThumbnail.getHeight()));
                        } catch (ExecutionException | InterruptedException e) {
                            Log.e(TAG, "run: ERROR SETTING IMAGE", e);
                        }
                    }
                });
                photoThumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.e(TAG, "onGlobalLayout: photothumnailImageview dimensions:" + photoThumbnail.getWidth() + " x " + photoThumbnail.getHeight());
            }
        });
        return fragmentView;
    }

    /**
     * Finds the fragmen't views
     */
    private void findViews() {
        this.makeSubmissionButton = (ImageButton) this.fragmentView.findViewById(R.id.button_tick_submit);
        this.backButton = (ImageButton) this.fragmentView.findViewById(R.id.button_back);
        this.categoriesLinearLayout = (LinearLayout) this.fragmentView.findViewById(R.id.categoriesLinearLayout);
        this.submissionDescription = (EditText) this.fragmentView.findViewById(R.id.newSubmissionEditTextDescrption);
        this.submissionTitle = (EditText) this.fragmentView.findViewById(R.id.newSubmissionEditTextTitle);
        this.submissionDescription.setImeOptions(EditorInfo.IME_ACTION_DONE);
        submissionTitle.setImeOptions(EditorInfo.IME_ACTION_DONE);
        this.categorySelectButton = (Button) fragmentView.findViewById(R.id.categorySelectButtonNewSubmission);
        this.photoThumbnail = (ImageView) this.fragmentView.findViewById(R.id.photoThumbnail);
        this.mapThumbnail = (ImageView) this.fragmentView.findViewById(R.id.newSubmissionMapThumbnail);
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

    /**
     * Sets up the listeners
     */
    private void setupClickListeners() {
        this.categoriesLinearLayout.setOnClickListener(this);
        this.makeSubmissionButton.setOnClickListener(this);
        this.photoThumbnail.setOnClickListener(this);
        this.backButton.setOnClickListener(this);

    }

    /**
     * Fetches the location from the bundle
     */
    private void fetchBundleFromArguments() {
        Bundle b = getArguments();  // getMainActivity().getIntent().getExtras();
        if (b != null) {
            this.location = new Location("jes");
            this.location.setLatitude(b.getDouble("latitude"));
            this.location.setLongitude(b.getDouble("longitude"));
            this.location.setAltitude(b.getDouble("altitude"));
            Log.e(TAG, "onCreateView: bundle received: " + location.toString());
        }
    }

    /**
     * Activates camera intent
     */
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

    /**
     * Creates the image file from the taken image
     */
    private void createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
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
            options.inSampleSize = 2;
            Bitmap imageBitmap = BitmapFactory.decodeFile(this.photoPath, options);
            if (imageBitmap != null) {
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
            } else {
                getMainActivity().makeToast("Error taking picture");
            }
        }
    }

    /**
     * Makes the submission and pushes it to the server
     */
    private void makeSubmission() {
        if (checkFieldsValidity()) {
            Submission newSub = new Submission(getMainActivity(), this.confirmedCategories, this.submissionTitle.getText().toString(), this.submissionDescription.getText().toString(), this.location);
            newSub.setFile(this.photoFile);
            if (this.currentPhoto != null) {
                newSub.setImage(this.currentPhoto);
            }

            if (newSub.submitToServer()) {
                // fetch the submissions again
                getMainActivity().startService(new Intent(getMainActivity(), SubmissionFetchService.class));
                Log.e(TAG, "makeSubmission: Submission sent succesfully");
                getMainActivity().fragmentSwitcher(Constants.fragmentTypes.FRAGMENT_MAP, null);
                getMainActivity().makeToast("Success!");
            } else {
                getMainActivity().makeToast("Error Submitting");
            }
        } else {
            getMainActivity().makeToast("Fields are not valid!");
            Log.e(TAG, "makeSubmission: FIELDS NOT VALID\nFIELDS NOT VALID");
        }
    }

    /**
     * Checks that all the parameters for the submission are valid
     *
     * @return <b>true</b> if the parameters are valid, <b>false</b> if not
     */
    private boolean checkFieldsValidity() {
        if (!TextUtils.isEmpty(this.submissionDescription.getText().toString())) {
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
        this.popMaker = new CategoriesPopup(getMainActivity(), this, this, this.confirmedCategories, this);
        this.popMaker.setupCategoriesPopup();
    }

    // listens to the checkbox being clicked and adds/removes the selected categories
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
     * Handles updating the categories on the submission screen, based on user selection
     */
    private void updateCategoryThumbnails() {
        this.categoriesLinearLayout.removeAllViews();

        for (Category c : this.confirmedCategories) {
            ImageView categoryImg = new ImageView(this.getMainActivity());
            categoryImg.setImageBitmap(c.getImage());
            LinearLayout.LayoutParams make = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(this.categoriesLinearLayout.getHeight(), this.categoriesLinearLayout.getHeight()));
            categoryImg.setLayoutParams(make);
            this.categoriesLinearLayout.addView(categoryImg);
        }
    }
}
