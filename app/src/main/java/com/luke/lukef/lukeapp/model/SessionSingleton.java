package com.luke.lukef.lukeapp.model;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;

/**
 * Represents the current session or user that's logged in
 */
public class SessionSingleton {
    private static SessionSingleton ourInstance = new SessionSingleton();
    private static final String TAG = "SessionSingleton";
    private String userId;
    private String accessToken;
    private String idToken;
    private String username;
    private String auth0ClientID;
    private String auth0Domain;
    private int xp;
    private boolean isUserLogged = false;
    private ArrayList<Category> categories;
    private Bitmap userImage;

    private SessionSingleton() {
        this.categories = new ArrayList<>();
    }

    public static SessionSingleton getInstance() {
        return ourInstance;
    }

    public ArrayList<Category> getCategoryList() {
        return this.categories;
    }

    public String getUsername() {
        return username;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

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

       // Log.e(TAG, "setidToken: idtoken token set to " + this.idToken);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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

    public String getAuth0ClientID() {
        return auth0ClientID;
    }

    public void setAuth0ClientID(String auth0ClientID) {
        this.auth0ClientID = auth0ClientID;
    }

    public String getAuth0Domain() {
        return auth0Domain;
    }

    public void setAuth0Domain(String auth0Domain) {
        this.auth0Domain = auth0Domain;
    }
}
