package com.luke.lukef.lukeapp.model;

/**
 * Created by Daniel on 15/12/2016.
 */

public class Rank {
    private String id;
    private String title;
    private String imageUrl;
    private String description;
    private int scoreRequirement;

    public int getScoreRequirement() {
        return scoreRequirement;
    }

    public void setScoreRequirement(int scoreRequirement) {
        this.scoreRequirement = scoreRequirement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
