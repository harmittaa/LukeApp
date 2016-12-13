package com.luke.lukef.lukeapp;

import com.luke.lukef.lukeapp.model.UserFromServer;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Unit tests for {@link com.luke.lukef.lukeapp.model.UserFromServer}.
 */
@RunWith(JUnit4.class)
public class UserFromServerUnitTest {

    /**
     * Tests{@link UserFromServer} constructor.
     */
    @Test
    public void testConstructor() {
        UserFromServer c = new UserFromServer();
        Assert.assertNotNull(c);
    }

    /**
     * Tests{@link UserFromServer} getters and setters
     */
    @Test
    public void testGettersAndSetters() {
        String id = "id";
        String username = "username";
        double score = 10;
        String rankId = "rankId";
        String imageUrl = "url";
        UserFromServer c = new UserFromServer();

        c.setUsername(username);
        c.setId(id);
        c.setScore(score);
        c.setRankId(rankId);
        c.setImageUrl(imageUrl);

        Assert.assertSame(c.getId(), id);
        Assert.assertSame(c.getUsername(), username);
        Assert.assertEquals(c.getScore(), score);
        Assert.assertSame(c.getRankId(), rankId);
        Assert.assertSame(c.getImageUrl(), imageUrl);
    }


}
