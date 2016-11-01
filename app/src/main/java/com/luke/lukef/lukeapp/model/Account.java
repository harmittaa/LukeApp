package com.luke.lukef.lukeapp.model;

import android.graphics.Bitmap;

public class Account {
    private static final String TAG ="Account";
    private String username;
    private int xp;
    private int level;
    private Bitmap userImage;

    // optimal case when all the parameters can be get from the server
    public Account(String username, int xp, int level, Bitmap userImage){
        this.username = username;
        this.xp = xp;
        this.level = level;
        this.userImage = userImage;
    }

    // if user has not chosen an image then use this constructor
    public Account(String username, int xp, int level){
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

    public void setUsername(String username) {
        this.username = username;
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
}
