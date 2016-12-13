package com.luke.lukef.lukeapp.model;

import android.graphics.Bitmap;

/**
 * Model class for the category
 */
public class Category {
    private String id;
    private String title;
    private String description;
    private Bitmap image;
    private Boolean isPositive;

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
        return this.image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Boolean getPositive() {
        return isPositive;
    }

    @Override
    public String toString() {
        return id;
    }

    public void setPositive(Boolean positive) {
        isPositive = positive;
    }
}
