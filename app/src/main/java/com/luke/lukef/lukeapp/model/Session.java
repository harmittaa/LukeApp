package com.luke.lukef.lukeapp.model;

import android.graphics.Bitmap;

public class Session {
    private static final String TAG ="Session";
    private int userId;
    private String accessToken;
    private String idToken;
    private String username;
    private int xp;
    private int level;
    private Bitmap userImage;

    // optimal case when all the parameters can be gotten from the server
    public Session(String username, int xp, int level, Bitmap userImage,int userId,String accessToken, String idToken){
        this.username = username;
        this.xp = xp;
        this.level = level;
        this.userImage = userImage;
        this.userId = userId;
        this.accessToken = accessToken;
        this.idToken = idToken;
    }

    // if user has not chosen an image then use this constructor
    public Session(String username, int xp, int level){
        this.username = username;
        this.xp = xp;
        this.level = level;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
