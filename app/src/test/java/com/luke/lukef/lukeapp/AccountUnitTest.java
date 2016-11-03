package com.luke.lukef.lukeapp;

import android.graphics.Bitmap;

import com.luke.lukef.lukeapp.model.Account;

import org.junit.Test;

import static org.junit.Assert.*;

public class AccountUnitTest {

    // checks if the constructor that doesn't get image as parameter sets the image to null
    @Test
    public void constructor_noImage() {
        Account testAccount = new Account("testUsername", 0, 1);
        assertNull(testAccount.getUserImage());
    }

    @Test
    public void constructor_levelSet(){
        int level = 8;
        Account testAccount = new Account("testUsername", 0, level);
        assertEquals(testAccount.getLevel(), level);
    }

    @Test
    public void constructor_usernameSet(){
        String username = "username";
        Account testAccount = new Account(username, 0, 1);
        assertEquals(testAccount.getUsername(), username);
    }

    @Test
    public void constructor_xpSet(){
        int xp = 120;
        Account testAccount = new Account("testUsername", xp, 1);
        assertEquals(testAccount.getXp(), xp);
    }
}