package com.luke.lukef.lukeapp;

public class Constants {
    public enum fragmentTypes{
        FRAGMENT_CONFIRMATION,FRAGMENT_LEADERBOARD,FRAGMENT_MAP, FRAGMENT_NEW_SUBMISSION,FRAGMENT_POINT_OF_INTEREST,FRAGMENT_PROFILE
    }

    public enum bottomActionBarStates{
        MAP_CAMERA, BACK_ONLY, BACK_TICK, BACK_REPORT
    }

    public enum loginFragmentTypes{
        LOGIN_FRAGMENT_WELCOME, LOGIN_FRAGMENT_AUTH0,LOGIN_FRAGMENT_USERDATA
    }

    public enum markerCategories{
        POSITIVE, NEUTRAL, NEGATIVE
    }
}
