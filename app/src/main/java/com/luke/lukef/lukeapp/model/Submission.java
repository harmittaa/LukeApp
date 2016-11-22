package com.luke.lukef.lukeapp.model;

/**
 * Created by tehetenamasresha on 01/11/2016.
 */

import android.graphics.Bitmap;
import android.location.Location;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.Date;

/**
 * handles submissions made by users
 */
public class Submission {
    private Bitmap image;
    private Location location;
    private String title;
    private String[] category;
    private Date date;
    private String description;

    //all values present
    public Submission(String title, String[] category, Date date, String description, Bitmap image, Location location) {
        this.image = image;
        this.location = location;
        this.title = title;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    //only mandatory values
    public Submission(String[] category, Date date, String description) {
        this.location = location;
        this.category = category;
        this.date = date;
        this.description = description;
    }


    //making a new submission
    public boolean add() {
        return true;
    }

    public boolean submitToServer(){
        return false;
    }

    //editing an existing submission
    public boolean update() {
        return true;
    }

    //deleting a submission
    public boolean delete() {
        return true;
    }

    //share submissions on other medias
    public boolean share() {
        return true;
    }

    //add a review flag
    public boolean flag() {
        return true;
    }

    public Bitmap getImage() {
        return image;
    }

    public Location getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public String[] getCategory() {
        return category;
    }

    public Date getDate() {
        return this.date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

