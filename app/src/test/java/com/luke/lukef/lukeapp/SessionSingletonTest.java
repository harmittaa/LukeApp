package com.luke.lukef.lukeapp;

import com.luke.lukef.lukeapp.model.SessionSingleton;

import junit.framework.Assert;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SessionSingleton}.
 */

public class SessionSingletonTest {

    /**
     * Tests that the {@link SessionSingleton} is not null
     *
     * @throws Exception
     */
    @Test
    public void getInstance() throws Exception {
        Assert.assertTrue(SessionSingleton.getInstance() != null);
    }

    /**
     * Tests that the {@link SessionSingleton#categories} is not null
     *
     * @throws Exception
     */
    @Test
    public void testSubmissionSingletonCategories() throws Exception {
        Assert.assertNotNull(SessionSingleton.getInstance().getCategoryList());
    }


    /**
     * Tests the getters and setters of {@link SessionSingleton}:
     * <li>{@link SessionSingleton#userId}</li>
     * <li>{@link SessionSingleton#accessToken}</li>
     * <li>{@link SessionSingleton#username}</li>
     * <li>{@link SessionSingleton#auth0ClientID}</li>
     * <li>{@link SessionSingleton#auth0Domain}</li>
     * <li>{@link SessionSingleton#score}</li>
     * <li>{@link SessionSingleton#isUserLogged}</li>
     *
     * @throws Exception
     */
    @Test
    public void testGettersAndSetters() throws Exception {
        SessionSingleton s = SessionSingleton.getInstance();
        String userId = "id";
        String accessToken = "token";
        String username = "username";
        String auth0ClientID = "auth0clientid";
        String auth0Domain = "auth0Domain";
        int xp = 100;
        boolean isUserLogged = false;

        s.setUserId(userId);
        s.setAccessToken(accessToken);
        s.setUsername(username);
        s.setAuth0ClientID(auth0ClientID);
        s.setAuth0Domain(auth0Domain);
        s.setScore(xp);
        s.setUserLogged(isUserLogged);

        Assert.assertSame(s.getUserId(), userId);
        Assert.assertSame(s.getAccessToken(), accessToken);
        Assert.assertSame(s.getUsername(), username);
        Assert.assertSame(s.getAuth0ClientID(), auth0ClientID);
        Assert.assertSame(s.getAuth0Domain(), auth0Domain);
        Assert.assertNull(s.getIdToken());
        Assert.assertEquals(s.isUserLogged(), isUserLogged);
    }

}