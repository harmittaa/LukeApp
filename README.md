# Baltic App

The Baltic App is a collaboration between LUKE, Metropolia and Bonus. The objective of this project was to create an application for monitoring the condition of the Baltic sea through user created reports, for the benefit of researchers and the general public. The application can be used to submit reports on the condition of the environment as well as viewing reports made by other users, to determine the condition of a location. The application consists of android application as well as an admin panel and server.

## Key Functionalities
  - Loggin in with social media or email
  - User profile creation with profile image and username
    - Editing user profile after creation is possible
  - Map with pins representing user made reports
    - Pin color coding
    - Pin clustering
  - Opening pins to see what the report is about
  - Ability to filter user made submissions by date
  - Creating a submission
    - Adding an image
    - Adding Categories describing what the report is about
    - Title and description
  - Leaderboard with top users listed
  - User profile page for viewing other users profiles
    - Submissions
    - Score
    - Rank
    - Profile image

## Techical
  - Server used to store most of data
  - SQLite database for storing bulky data used during app lifetime
    - Pins displayed on the map are retrieved intelligently from the SQLite database, so that unused data does not get loaded into memeory
  
## Apis
  - [https://developers.google.com/maps/android/](Google Maps) used as map Api
  - [https://auth0.com/](Auth0) used for authentication through social media or email
  - [http://www.balticapp.fi/lukeA/](Custom Server) for serverside operations
  
## Screenshots
<center><img src="/screenshots/main.jpg" width="250"> <img src="/screenshots/list.jpg" width="250"> <img src="/screenshots/result.jpg" width="250"></center>
  
## Build Status
[![Build Status](https://travis-ci.org/harmittaa/LukeApp.svg?branch=master)](https://travis-ci.org/harmittaa/LukeApp)
