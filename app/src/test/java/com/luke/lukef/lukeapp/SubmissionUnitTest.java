package com.luke.lukef.lukeapp;

/**
 * Created by Bang Nguyen on 11/3/2016.
 */


import com.luke.lukef.lukeapp.Submission;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import static org.junit.Assert.*;

public class SubmissionUnitTest {
    // checks if the constructor that get all the parameter
    @Test
    public void constructor_title() {
        String title = "Test submission";
        Date thisDate = new Date();
        Submission testSubmission = new Submission(title, "beach", "none", thisDate);
        assertEquals(testSubmission.getTitle(), title);
    }

    @Test
    public void constructor_category() {
        String category = "Test submission";
        Date thisDate = new Date();
        Submission testSubmission = new Submission("Test submission", category, "none", thisDate);
        assertEquals(testSubmission.getCategory(), category);
    }

    @Test
    public void constructor_feedback() {
        String feedback = "Test feedback";
        Date thisDate = new Date();
        Submission testSubmission = new Submission("Test submission", "beach", feedback, thisDate);
        assertEquals(testSubmission.getFeedback(), feedback);
    }

    @Test
    public void constructor_date() {
        Date thisDate = new Date();
        Submission testSubmission = new Submission("Test submission", "beach", "test feedback", thisDate);
        assertEquals(testSubmission.getDate(), thisDate);
    }

}
