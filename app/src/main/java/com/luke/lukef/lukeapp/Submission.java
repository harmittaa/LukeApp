package com.luke.lukef.lukeapp;

/**
 * Created by tehetenamasresha on 01/11/2016.
 */

import android.graphics.Bitmap;

/**
 * handles submissions made by users
 */
public class Submission {
    Bitmap image;
    int location;
    String title;
    String catagory;
    String feedback;


    public Submission(Bitmap image, int location, String title, String catagory, String feedback){
        this.image = image;
        this.location = location;
        this.title = title;
        this.catagory = catagory;
        this.feedback = feedback;
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
}

