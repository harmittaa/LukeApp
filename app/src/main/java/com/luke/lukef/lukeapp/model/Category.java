package com.luke.lukef.lukeapp.model;

import android.graphics.Bitmap;

/**
 * Created by Daniel on 22/11/2016.
 */

public class Category {
    private String id;
    private String title;
    private String description;
    private Bitmap image;
    private Boolean isPositive;

    public Category(){
    }
    public Category(String id, String title, Boolean isPositive){
        this.id = id;
        this.title = title;
        this.isPositive = isPositive;
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Boolean getPositive() {
        return isPositive;
    }

    public void setPositive(Boolean positive) {
        isPositive = positive;
    }

    @Override
    public String toString() {
        return " id: " + id + " title: " + title + " description: " + description + " image_url: " + image + " isPositive: " + isPositive;
    }
}
