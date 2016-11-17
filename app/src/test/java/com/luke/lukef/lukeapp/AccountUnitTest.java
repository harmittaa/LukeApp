package com.luke.lukef.lukeapp;

import com.luke.lukef.lukeapp.model.Session;

import org.junit.Test;

import static org.junit.Assert.*;

public class AccountUnitTest {

    // checks if the constructor that doesn't get image as parameter sets the image to null
    @Test
    public void constructor_noImage() {
        Session testAccount = new Session("testUsername", 0, 1);
        assertNull(testAccount.getUserImage());
    }

    @Test
    public void constructor_levelSet(){
        int level = 8;
        Session testAccount = new Session("testUsername", 0, level);
        assertEquals(testAccount.getLevel(), level);
    }

    @Test
    public void constructor_usernameSet(){
        String username = "username";
        Session testAccount = new Session(username, 0, 1);
        assertEquals(testAccount.getUsername(), username);
    }

    @Test
    public void constructor_xpSet(){
        int xp = 120;
        Session testAccount = new Session("testUsername", xp, 1);
        assertEquals(testAccount.getXp(), xp);
    }
}
