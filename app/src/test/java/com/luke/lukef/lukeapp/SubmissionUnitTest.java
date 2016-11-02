package com.luke.lukef.lukeapp;

/**
 * Created by Bang Nguyen on 11/3/2016.
 */


import com.luke.lukef.lukeapp.Submission;

import org.junit.Test;

import java.text.DateFormat;


import static org.junit.Assert.*;

public class SubmissionUnitTest {
    // checks if the constructor that get all the parameter
    @Test
    public void constructor_title() {
        String title = "Test submission";
        Submission testSubmission = new Submission(title, "beach", "none", DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.AM_PM_FIELD) );
        assertEquals(testSubmission.getTitle(),title );
    }

    @Test
    public void constructor_category() {
        String category = "Test submission";
        Submission testSubmission = new Submission("Test submission", category, "none", DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.AM_PM_FIELD) );
        assertEquals(testSubmission.getCategory(),category );
    }

    @Test
    public void constructor_feedback() {
        String feedback = "Test feedback";
        Submission testSubmission = new Submission("Test submission", "beach", feedback, DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.AM_PM_FIELD) );
        assertEquals(testSubmission.getFeedback(),feedback );
    }

    @Test
    public void constructor_date() {
        DateFormat date = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.AM_PM_FIELD);
        Submission testSubmission = new Submission("Test submission", "beach", "test feedback", date);
        assertEquals(testSubmission.getDate(),date );
    }

}
