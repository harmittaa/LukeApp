package com.luke.lukef.lukeapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.luke.lukef.lukeapp.interfaces.Auth0Responder;
import com.luke.lukef.lukeapp.model.SessionSingleton;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.luke.lukef.lukeapp.WelcomeActivity.REQUEST_IMAGE_CAPTURE;

public class NewUserActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, Auth0Responder {
    private EditText username;
    private ImageButton confirmButton;
    private ImageView userImageViewCamera;
    private ImageView userImageViewAuth0;
    private ImageView userImageViewDefault;
    private static final String TAG = "NewUserActivity";
    private String photoPath;
    private File photofile;
    private LukeNetUtils lukeNetUtils;
    private RadioGroup radioGroupPicture;
    private Bitmap selectedProfileImage;
    private Bitmap auth0ProfileImage;
    private Bitmap cameraProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.lukeNetUtils = new LukeNetUtils(this);
        setContentView(R.layout.activity_new_user);
        this.userImageViewCamera = (ImageView) findViewById(R.id.newUserCameraImageView);
        this.userImageViewAuth0 = (ImageView) findViewById(R.id.newUserSocialMediaImageView);
        this.userImageViewDefault = (ImageView) findViewById(R.id.newUserDefaultImageView);
        this.confirmButton = (ImageButton) findViewById(R.id.newUserConfirmButton);
        this.confirmButton.setOnClickListener(this);
        this.username = (EditText) findViewById(R.id.newUserName);
        this.username.setImeActionLabel("Custom text", KeyEvent.ACTION_DOWN);
        this.username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (attemptSetUsername(username.getText().toString())) {
                    // TODO: 05/12/2016 change activity
                    Log.e(TAG, "onEditorAction: SHOULD CHANGE ACTIVITY NOW");
                    startActivity(new Intent(NewUserActivity.this, MainActivity.class));
                } else {
                    Log.e(TAG, "onEditorAction: SOME ERROR HAPPENED?");
                    // TODO: 05/12/2016 display error
                }
                return false;
            }
        });
        this.radioGroupPicture = (RadioGroup) findViewById(R.id.radioGroupPicture);
        this.radioGroupPicture.setOnCheckedChangeListener(this);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    protected void onResume() {
        LukeNetUtils lukeNetUtils = new LukeNetUtils(this);
        lukeNetUtils.getUserImageFromAuth0(this);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.newUserConfirmButton:
                if (attemptSetUsername(this.username.getText().toString()))
                    break;
        }
    }

    private boolean attemptSetUsername(String uname) {
        if (checkUsernameValid(uname)) {
            Log.e(TAG, "onEditorAction: Validitiy checked");
            try {
                if (this.lukeNetUtils.checkUsernameAvailable(uname)) {
                    Log.e(TAG, "onEditorAction: username available");
                    if (this.lukeNetUtils.setUsername(uname)) {
                        if (this.selectedProfileImage != null) {
                            this.lukeNetUtils.updateUserImage(this.selectedProfileImage);
                            SessionSingleton.getInstance().setUserImage(this.selectedProfileImage);

                        }
                        Log.e(TAG, "onEditorAction: username set");
                        startActivity(new Intent(NewUserActivity.this, MainActivity.class));
                        return true;
                    } else {
                        Toast.makeText(this, "An Error Occured", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    Toast.makeText(this, "Username Taken", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (ExecutionException e) {
                Toast.makeText(this, "An Error Occured", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onEditorAction: ", e);
                return false;
            } catch (InterruptedException e) {
                Toast.makeText(this, "An Error Occured", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onEditorAction: ", e);
                return false;
            } catch (IOException e) {
                Toast.makeText(this, "An Error Occured", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onEditorAction: ", e);
                return false;
            }
        } else {
            Toast.makeText(this, "Invalid Username", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * Checks if the username is valid (not empty, length between 1 & 10 characters)
     * @param uname username to check
     * @return returns true if all checks have passed
     */
    private boolean checkUsernameValid(String uname) {
        Log.e(TAG, "checkUsernameValid: USERNAME IS  " + uname);
        if (!TextUtils.isEmpty(uname)) {
            Log.e(TAG, "checkUsernameValid: passed first");
            int length = uname.trim().length();
            return (length >= 1) && (length <= 10);
        }
        return false;
    }


    /**
     * Creates the intent to start camera
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go

            if (photofile != null) {
                this.photoPath = photofile.getAbsolutePath();
                // Continue only if the File was successfully created
                Uri photoURI = FileProvider.getUriForFile(this, "com.luke.lukef.lukeapp", photofile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                createImageFile();
                dispatchTakePictureIntent();
            }
        }
    }

    /**
     * Creates an image file from the photo that was taken
     */
    private void createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
        if (image != null) {
            this.photoPath = image.getAbsolutePath();
            this.photofile = image;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap imageBitmap = BitmapFactory.decodeFile(this.photoPath, options);
            try {
                Log.e(TAG, "onActivityResult: photo file before write" + this.photofile.length());
                FileOutputStream fo = new FileOutputStream(this.photofile);
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
                Log.e(TAG, "onActivityResult: photo file after write" + this.photofile.length());
            } catch (FileNotFoundException e) {
                Log.e(TAG, "onActivityResult: ", e);
            }

            imageBitmap = BitmapFactory.decodeFile(photoPath, options);

            if (imageBitmap != null)
                Log.e(TAG, "onActivityResult: photo exists, size : " + imageBitmap.getByteCount());
            Log.e(TAG, "run: CAMERA");
            this.userImageViewCamera.setImageBitmap(imageBitmap);
            this.cameraProfileImage = imageBitmap;
            this.selectedProfileImage = this.cameraProfileImage;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioButtonDefault:
                this.selectedProfileImage = null;
                break;
            case R.id.radioButtonSocial:
                this.selectedProfileImage = this.auth0ProfileImage;
                break;
            case R.id.radioButtonCamera:
                dispatchTakePictureIntent();
                break;
        }
    }

    @Override
    public void receiveBitmapFromAuth0(final Bitmap b) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NewUserActivity.this.userImageViewAuth0.setImageBitmap(b);
                Log.e(TAG, "run: AUTH0");
                NewUserActivity.this.auth0ProfileImage = b;
            }
        });
    }
}
