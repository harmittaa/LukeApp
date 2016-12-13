package com.luke.lukef.lukeapp;


import com.luke.lukef.lukeapp.model.Category;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CategoryUnitTest {


    /**
     * Tests that when a {@link Category} object is created, it's instance variables are null.
     *
     * @throws Exception
     */
    @Test
    public void checkEmptyConstructor() throws Exception {
        Category c = new Category();
        Assert.assertNull(c.getId());
        Assert.assertNull(c.getDescription());
        Assert.assertNull(c.getImage());
        Assert.assertNull(c.getPositive());
        Assert.assertNull(c.getTitle());
    }

    /**
     * Tests {@link Category} variable {@link Category#id} getter and setter.
     */
    @Test
    public void testIdGetterAndSetter() {
        Category c = new Category();
        String id = "id";
        c.setId(id);
        Assert.assertEquals(c.getId(), id);
    }

    /**
     * Tests {@link Category} variable {@link Category#title} getter and setter.
     */
    @Test
    public void testTitleGetterAndSetter() {
        Category c = new Category();
        String title = "title";
        c.setTitle(title);
        Assert.assertEquals(c.getTitle(), title);
    }

    /**
     * Tests {@link Category} variable {@link Category#description} getter and setter.
     */
    @Test
    public void testDescriptionGetterAndSetter() {
        Category c = new Category();
        String description = "description";
        c.setDescription(description);
        Assert.assertSame(c.getDescription(), description);
    }


}
