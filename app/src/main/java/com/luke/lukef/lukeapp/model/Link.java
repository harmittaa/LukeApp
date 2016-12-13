package com.luke.lukef.lukeapp.model;

import java.util.List;

/**
 * Created by Daniel on 13/12/2016.
 */

public class Link {
    /*id	String
    Id of the Link

    link	String
    Third party link to specific site, or survey

    description	String
    Description of the link

    title	String
    Title of the link

    active	Boolean
    Indicates if the link is good to present

    done*/
    private String link;
    private String description;
    private String title;
    private boolean isActive;
    private List<String> done;
    private String id;

    public List<String> getDone() {
        return done;
    }

    public void setDone(List<String> done) {
        this.done = done;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
