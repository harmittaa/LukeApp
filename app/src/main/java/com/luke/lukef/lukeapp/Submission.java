package com.luke.lukef.lukeapp;

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
    private DateFormat date;





    public Submission(String title, String category, String feedback, DateFormat date){
        this.image = image;
        this.location = location;
        this.title = title;
        this.category = category;
        this.feedback = feedback;
        this.date = date;
    }
    //making a new submission
        public  Boolean add() {

            return  true;
        }
//editing an existing submission
        public  Boolean update() {
            return true;
        }
//deleting a submission
        public  Boolean delete() {
            return true;
        }
//share submissions on other medias
        public  Boolean share() {
            return true;
        }
//add a review flag
        public  Boolean flag() {
            return true;
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

    public DateFormat getDate() {
        return DateFormat.getDateTimeInstance();
    }

}

