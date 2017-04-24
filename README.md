### Build Status
[![Build Status](https://travis-ci.org/harmittaa/LukeApp.svg?branch=master)](https://travis-ci.org/harmittaa/LukeApp)


# Baltic App

The Baltic App is a collaboration between LUKE, Metropolia and Bonus. The objective of this project was to create an application for monitoring the condition of the Baltic sea through user created reports, for the benefit of researchers and the general public. The application can be used to submit reports on the condition of the environment as well as viewing reports made by other users, to determine the condition of a location. The application consists of android application as well as an admin panel and server.

# Contributors
Android Development & Design:

  Matti-Mäkikihniä
  
  Daniel Zakharin
  
  
  
Design:

  Janne Pelkonen
  
  Samppa Sassi
  
# License 
GPLv3

### Key Functionalities
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

### Techical
  - Server used to store most of data
  - SQLite database for storing bulky data used during app lifetime
    - Pins displayed on the map are retrieved intelligently from the SQLite database, so that unused data does not get loaded into memeory
  
### Apis
  - [Google Maps](https://developers.google.com/maps/android/) used as map Api
  - [Auth0](https://auth0.com/) used for authentication through social media or email
  - [Custom Server](http://www.balticapp.fi/lukeA/) for serverside operations
  - [Admin panel](https://github.com/BangNguyen1992/Luke-Admin-v2)
  
### Screenshots
<img src="https://i.imgur.com/QagnraC.jpg" width="250" height ="500"><img src="https://i.imgur.com/t8JLPJY.jpg" width="250" height ="500"> <img src="https://i.imgur.com/AEI7BvD.jpg" width="250" height ="500"> 
