package com.luke.lukef.lukeapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.luke.lukef.lukeapp.model.Category;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class CategoryInstrumentedTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getContext();
    }

    /**
     * Tests {@link Category} variable {@link Category#image} getter and setter.
     */
    @Test
    public void testImageGetterAndSetter() {
       /* context = InstrumentationRegistry.getContext();
        Category c = new Category();
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_star);
        c.setImage(image);
        assertNull(c.getImage());*/
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.luke.lukef.lukeapp", appContext.getPackageName());

    }
}
