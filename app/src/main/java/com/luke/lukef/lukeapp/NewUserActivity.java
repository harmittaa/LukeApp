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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.luke.lukef.lukeapp.interfaces.Auth0Responder;
import com.luke.lukef.lukeapp.tools.LukeNetUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static com.luke.lukef.lukeapp.WelcomeActivity.REQUEST_IMAGE_CAPTURE;

public class NewUserActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener,Auth0Responder {

    private EditText username;
    private ImageButton confirmButton;
    private String usernameString;
    ImageView userImageViewCamera;
    ImageView userImageViewAuth0;
    ImageView userImageViewDefault;
    private static final String TAG = "NewUserActivity";
    private String photoPath;
    private File photofile;
    private Bitmap currentPhoto;
    LukeNetUtils lukeNetUtils;
    RadioGroup radioGroupPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lukeNetUtils = new LukeNetUtils(this);
        setContentView(R.layout.activity_new_user);
        userImageViewCamera = (ImageView) findViewById(R.id.newUserCameraImageView);
        userImageViewAuth0 = (ImageView)findViewById(R.id.newUserSocialMediaImageView);
        userImageViewDefault = (ImageView)findViewById(R.id.newUserDefaultImageView);
        confirmButton = (ImageButton) findViewById(R.id.newUserConfirmButton);
        confirmButton.setOnClickListener(this);
        username = (EditText) findViewById(R.id.newUserName);
        username.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (attemptSetUsername(username.getText().toString())) {
                    // TODO: 05/12/2016 change activity
                    startActivity(new Intent(NewUserActivity.this,MainActivity.class));
                } else {
                    // TODO: 05/12/2016 display error
                }
                return false;
            }
        });
        radioGroupPicture = (RadioGroup)findViewById(R.id.radioGroupPicture);
        radioGroupPicture.setOnCheckedChangeListener(this);
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
                if(attemptSetUsername(username.getText().toString()))
                break;
        }
    }

    private boolean attemptSetUsername(String uname) {
        if (checkUsernameValid(uname)) {
            try {
                if (lukeNetUtils.checkUsernameAvailable(uname)) {
                    if (lukeNetUtils.setUsername(uname)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } catch (ExecutionException e) {
                Log.e(TAG, "onEditorAction: ", e);
                return false;
            } catch (InterruptedException e) {
                Log.e(TAG, "onEditorAction: ", e);
                return false;
            } catch (IOException e) {
                Log.e(TAG, "onEditorAction: ", e);
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean checkUsernameValid(String uname) {
        if (!TextUtils.isEmpty(uname)) {
            if (uname.length() > 3 && uname.length() < 10) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


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

    private void createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
        this.photoPath = image.getAbsolutePath();
        this.photofile = image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap imageBitmap = BitmapFactory.decodeFile(this.photoPath.toString(), options);
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
            userImageViewCamera.setImageBitmap(imageBitmap);
            this.currentPhoto = imageBitmap;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.radioButtonDefault:
                break;
            case R.id.radioButtonSocial:
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
            }
        });
    }
}
