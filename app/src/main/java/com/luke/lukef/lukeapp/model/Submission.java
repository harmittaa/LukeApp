package com.luke.lukef.lukeapp.model;

/**
 * Created by tehetenamasresha on 01/11/2016.
 */

import android.graphics.Bitmap;

import java.text.DateFormat;
import java.util.Date;

/**
 * handles submissions made by users
 */
public class Submission {
    private Bitmap image;
    private int location;
    private String title;
    private String category;
    private String feedback;
    private Date date;
    private String content;


    public Submission(String title, String category, String feedback, Date date, String content) {
        this.image = image;
        this.location = location;
        this.title = title;
        this.category = category;
        this.feedback = feedback;
        this.date = date;
        this.content = content;
    }
    //For testing CarView
    public Submission(){

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

    public String getContent() {
        return content;
    }

    public Bitmap getImage() {
        return image;
    }

    public int getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getFeedback() {
        return feedback;
    }

    public Date getDate() {
        return this.date;
    }

}

