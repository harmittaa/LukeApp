package com.luke.lukef.lukeapp.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.luke.lukef.lukeapp.Constants;
import com.luke.lukef.lukeapp.MainActivity;
import com.luke.lukef.lukeapp.R;
import com.luke.lukef.lukeapp.model.Category;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.model.Submission;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class NewSubmissionFragment extends Fragment implements View.OnClickListener {
    View fragmentView;
    Button categoryButton;
    EditText submissionTitle;
    EditText submissionDescription;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView photoThumbnail;
    ImageView mapThumbnail;
    Bitmap currentPhoto;
    private final static String TAG = NewSubmissionFragment.class.toString();
    private String mCurrentPhotoPath;
    ArrayList<String> selectedCategries;
    Button submittt;
    Location location;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_new_submission, container, false);
        categoryButton = (Button) fragmentView.findViewById(R.id.buttonCategory);
        submittt = (Button) fragmentView.findViewById(R.id.button_submit_test);
        submissionDescription = (EditText) fragmentView.findViewById(R.id.newSubmissionEditTextDescrption);
        submissionTitle = (EditText) fragmentView.findViewById(R.id.newSubmissionEditTextTitle);
        setupClickListeners();
        getMainActivity().setBottomBarButtons(Constants.bottomActionBarStates.BACK_TICK);
        this.setBottomButtonListeners();
        fetchBundleFromArguments();
        selectedCategries = new ArrayList<>();
        setupThumbnailMap();
        ViewTreeObserver vto = mapThumbnail.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getMapThumbnail(location,mapThumbnail.getWidth(),mapThumbnail.getHeight());
                photoThumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonCategory:
                makeCategoryListPopup();
                break;
            case R.id.button_submit_test:
                makeSubmission();
                break;
            case R.id.photoThumbnail:
                dispatchTakePictureIntent();
                break;
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private void setupClickListeners() {
        categoryButton.setOnClickListener(this);
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
        mapThumbnail = (ImageView)fragmentView.findViewById(R.id.newSubmissionMapThumbnail);
        //getMapThumbnail(location,mapThumbnail.getWidth(),mapThumbnail.getHeight());
        photoThumbnail.setOnClickListener(this);
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
                Log.e(TAG, "dispatchTakePictureIntent: uri file path" + photoURI.toString());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void compressImage() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Bitmap b = getBitmapFromStorage();
        Log.e(TAG, "compressImage: Byte count of UNcompressed image" + b.getByteCount());
        //b = MediaStore.Images.Media.getBitmap(getMainActivity().getContentResolver(), this.imagePath);
        Bitmap out = Bitmap.createScaledBitmap(b, 720, 560, false);
        Log.e(TAG, "compressImage: Byte count of compressed image" + out.getByteCount());
        File file = new File(this.mCurrentPhotoPath.toString());
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.JPEG, 0, fOut);
            Log.e(TAG, "compressImage: Byte count of compressformat image" + BitmapFactory.decodeFile(this.mCurrentPhotoPath).getByteCount());
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception e) {
            Log.e(TAG, "compressImage: ", e);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.e(TAG, "onActivityResult: original image size" + getBitmapFromStorage().getByteCount());
            compressImage();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap imageBitmap = BitmapFactory.decodeFile(this.mCurrentPhotoPath.toString(), options);
            photoThumbnail.setImageBitmap(imageBitmap);
            this.currentPhoto = imageBitmap;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "lukeImage";//"JPEG_" + timeStamp + "_";
        File storageDir = getMainActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.e(TAG, "createImageFile: FILEPATH : "+ mCurrentPhotoPath );
        return image;
    }

    private void getMapThumbnail(final Location center, final int width, final int height){
        //https://maps.googleapis.com/maps/api/staticmap?center=29.390946,%2076.963502&zoom=10&size=600x300&maptype=normal
        final String urlString1 = "https://maps.googleapis.com/maps/api/staticmap?center=";
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString1 + center.getLatitude() + ",%20" + center.getLongitude() + "&zoom=18&size="+width+"x"+height+"&maptype=normal");
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


    private void changeMapThumbnail(final Bitmap bm){
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapThumbnail.setImageBitmap(bm);
            }
        });
    }

    private Bitmap getBitmapFromStorage() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        final Bitmap b = BitmapFactory.decodeFile(this.mCurrentPhotoPath, options);
        return b;
    }

    private void makeSubmission() {
        if (checkFieldsValidity()) {
            // TODO: 22/11/2016 create submission object, make httprequest and send to server(put this request into submission?)
            Submission newSub = new Submission(getMainActivity(), this.selectedCategries, new Date(), submissionDescription.getText().toString(), this.location);
            newSub.setFilePath(this.mCurrentPhotoPath);
            if(currentPhoto != null){
                newSub.setImage(this.currentPhoto);
            }
            if (newSub.submitToServer()) {
                Log.e(TAG, "makeSubmission: Submission sent succesfully");
            } else {
                Log.e(TAG, "makeSubmission: Error submitting");
            }
        } else {
            Log.e(TAG, "makeSubmission: FIELDS NOT VALID\nFIELDS NOT VALID");
        }
    }

    private boolean checkFieldsValidity() {
        // TODO: 22/11/2016 check if location != null , check if
        if (!TextUtils.isEmpty(submissionDescription.getText().toString())) {
            if (location != null) {
                if (selectedCategries.size() > 0) {
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
                //NewSubmissionFragment.this.selectedCategries.add(cla.getItem(which));
                Category c = SessionSingleton.getInstance().getCategoryList().get(which);
                NewSubmissionFragment.this.selectedCategries.add(c.getId());
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
