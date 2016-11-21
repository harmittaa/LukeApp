package com.luke.lukef.lukeapp.model;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by Daniel on 15/11/2016.
 */
public class SessionSingleton {
    private static SessionSingleton ourInstance = new SessionSingleton();

    public static SessionSingleton getInstance() {
        return ourInstance;
    }


    private static final String TAG ="Session";
    private String userId;
    private String accessToken;
    private String idToken;
    private String username;
    private int xp;
    private int level;
    private Bitmap userImage;
    private boolean isUserLogged = false;

    // optimal case when all the parameters can be gotten from the server
    public void setValues(String username, int xp, int level, Bitmap userImage, String userId, String accessToken, String idToken){
        this.username = username;
        this.xp = xp;
        this.level = level;
        this.userImage = userImage;
        this.userId = userId;
        this.accessToken = accessToken;
        this.idToken = idToken;
    }

    // calls DB to update the xp
    public boolean increaseXp(int xp) {
        return true;
    }

    // calls DB update the level
    public boolean levelUp() {
        return true;
    }

    // calls DB to delete the account
    public boolean deleteAccount() {
        return true;
    }

    public String getUsername() {
        return username;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) { this.level = level; }

    public Bitmap getUserImage() {
        return userImage;
    }

    public void setUserImage(Bitmap userImage) {
        this.userImage = userImage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;

        Log.e(TAG, "setidToken: idtoken token set to " + this.idToken );
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;

        Log.e(TAG, "setidToken: accestoken set to " + this.accessToken );
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isUserLogged() {
        return isUserLogged;
    }

    public void setUserLogged(boolean userLogged) {
        isUserLogged = userLogged;
    }
}
