package com.luke.lukef.lukeapp.model;

/**
 * Created by tehetenamasresha on 01/11/2016.
 */

import android.graphics.Bitmap;
import android.location.Location;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * handles submissions made by users
 */
public class Submission {
    private Bitmap image;
    private Location location;
    private String title;
    private List category;
    private Date date;
    private String description;
    private String id;

    //all values present
    public Submission(String id, String title, List category, Date date, String description, Bitmap image, Location location) {
        this.category = new ArrayList();
        this.id = id;
        this.image = image;
        this.location = location;
        this.title = title;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    //only mandatory values
    public Submission(List category, Date date, String description) {
        this.category = new ArrayList();
        this.location = location;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    //making a new submission
    public boolean add() {
        return true;
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

    public List getCategory() {
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