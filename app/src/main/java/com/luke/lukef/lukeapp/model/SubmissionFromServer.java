package com.luke.lukef.lukeapp.model;

import android.location.Location;

import java.util.List;

/**
 * Created by Daniel on 09/12/2016.
 */

public class SubmissionFromServer {
    /*id: String
    longitude: Number,
    latitude: Number,
    altitude: Number,
    image_url: String,
    title: String,
    description: String,
    date: String,
    categoryId: [
    String
    ],
    rating: Number,
    submitterId: String,
    approved: Boolean,
    flagged: Boolean,
    votes: [
    {
        userId: String,
                vote: Boolean
    }
    ]*/
    private String submissionId;
    private Location location;
    private String imageUrl;
    private String title;
    private String description;
    private String date;
    private List<String> submissionCategoryList;
    private String submitterId;

    public SubmissionFromServer(){

    }


    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getSubmissionCategoryList() {
        return submissionCategoryList;
    }

    public void setSubmissionCategoryList(List<String> submissionCategoryList) {
        this.submissionCategoryList = submissionCategoryList;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }
}
