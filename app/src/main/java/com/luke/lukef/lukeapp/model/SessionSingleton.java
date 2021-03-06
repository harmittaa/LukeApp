/*
        BalticApp, for studying and tracking the condition of the Baltic sea
        and Gulf of Finland throug user submissions.
        Copyright (C) 2016  Daniel Zakharin, LuKe

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/> or
        the beginning of MainActivity.java file.

*/

package com.luke.lukef.lukeapp.model;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import com.luke.lukef.lukeapp.WelcomeActivity;

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
    private int score;
    private boolean isUserLogged = false;
    private ArrayList<Category> categories;
    private ArrayList<Rank> ranks;
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

    public void logOut(Activity activity) {
        this.setAuth0Domain(null);
        this.setIdToken(null);
        this.setUserLogged(false);
        this.setAccessToken(null);
        this.setUserId(null);
        this.setUsername(null);
        this.setUserImage(null);
        activity.startActivity(new Intent(activity, WelcomeActivity.class));
        activity.finish();
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public ArrayList<Rank> getRanks() {
        return ranks;
    }

    public void setRanks(ArrayList<Rank> ranks) {
        this.ranks = ranks;
    }

    public Rank getRankById(String id) {
        for (Rank c : getRanks()) {
            if (c.getId().equals(id)) {
                return c;
            }
        }return null;
    }
}
