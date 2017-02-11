/*
        BalticApp, for studying and tracking the condition of the Baltic sea
        and Gulf of Finland throug user submissions.
        Copyright (C) 2016  Daniel Zakharin, LuKe

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <http://www.gnu.org/licenses/> or
        the beginning of MainActivity.java file.

*/

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
