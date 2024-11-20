# Personal Project: Just The News
## Kotlin News Application
By [Sam Clark](https://github.com/SamC95)

Just The News is a region-based news application built using Kotlin, Firebase, and NewsAPI.

The API Key for [NewsAPI](https://newsapi.org/) is not included in the GitHub repository. 

If attempting to use the application, please get your own free key on the [website](https://newsapi.org/) and create a file "apikeys.properties" and store it with the name "NEWS_API_KEY"

## Contents
* [Project Aims](https://github.com/SamC95/Kotlin_NewsApp#project-aims)
* [Approach](https://github.com/SamC95/Kotlin_NewsApp#approach)
* [Technologies](https://github.com/SamC95/Kotlin_NewsApp#technologies)
* [Project Planning](https://github.com/SamC95/Kotlin_NewsApp#project-planning)
* [Implementation](https://github.com/SamC95/Kotlin_NewsApp#implementation)
* [Key Learnings](https://github.com/SamC95/Kotlin_NewsApp#key-learnings)
* [Achievements](https://github.com/SamC95/Kotlin_NewsApp#achievements)
* [Challenges](https://github.com/SamC95/Kotlin_NewsApp#challenges)
* [Conclusions](https://github.com/SamC95/Kotlin_NewsApp#conclusions)
* [Credits](https://github.com/SamC95/Kotlin_NewsApp#credits)

## Project Aims
* Develop an Android-based news application using Kotlin
* Design an application with a greater focus on UI/UX design than prior Kotlin projects
* Implement an account system using Firebase and Firestore
* Utilise [NewsAPI](https://newsapi.org/) to populate the application with data
* Include functionality to retrieve regional news based on the user's location with permission
* Allow the user to define the region that their account is set to manually, with appropriate news retrieved
* Implement the functionality for the user to log in using their Google account
* User should be able to search for news based on their input
* Functionality for the user to save articles on their account
* The user should have the ability to delete their account along with any saved data related to it permanently
* Application should behave as expected when changing between vertical and horizontal orientation and data should be correctly retained

## Approach

### Planning & Analysis

Prior to beginning development of the project, I defined the key aims that I was looking to achieve with the application which have been mentioned in the prior section. 
The core fundamental design goals of the application were to design a Kotlin Android app with an account system that was based around Firebase integration with a greater focus on the UI/UX design.

After I had decided on these core goals, I proceeded to determine the theme of the application and decided upon a news application using the NewsAPI service for retrieving the data.
Once this aspect had been determined, I performed some research to decide on how I wanted to approach the project.

This involved: 

* Research into the NewsAPI service and the type of functionality that it offered, as well as what limitations there may be to consider with it
* It was also necessary to research similar applications to determine a general idea for the UI/UX of the application, as such I particularly focused on the [Sky News](https://play.google.com/store/apps/details?id=com.bskyb.skynews.android&hl=en_GB&gl=US) and [BBC News](https://play.google.com/store/apps/details?id=bbc.mobile.news.uk&hl=en_GB) applications as a reference point.
* I also needed to look into the functionality of Firebase to determine how I would approach the account system as it was my first time using it

## Technologies

![Static Badge](https://img.shields.io/badge/kotlin-Kotlin?style=for-the-badge&logo=Kotlin&logoColor=white&color=%237F52FF)  
![Static Badge](https://img.shields.io/badge/firebase-Firebase?style=for-the-badge&logo=Firebase&logoColor=white&color=%23DD2C00)  
![Static Badge](https://img.shields.io/badge/NewsAPI-NewsAPI?style=for-the-badge&logoColor=white&color=%232D72D9)  
![Static Badge](https://img.shields.io/badge/android%20studio-Android%20Studio?style=for-the-badge&logo=Android%20Studio&logoColor=white&color=%233DDC84)  

## Project Planning

### Application Navigation Diagram

This diagram was created to determine the overall flow of the application in terms of how the user navigates between activities.

<img src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/85dfb506-0f33-48cf-a81a-c168f1e3cf96" width="800" />

### UI/UX Storyboards

Some storyboards of the intended user interface for the application were created, representing some of the core activities in the application.

#### Login & Create Account

During development, I eventually opted to not include Apple login but this was initially considered during the planning phase.

<p float="left">
  <img src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/56ed6097-6da4-4ee3-b07f-b5eb910e94ee" width="300" />
  <img src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/6d9d99e4-d6f2-4ea5-9057-71707b811bd5" width="300" />
</p>

#### News Feed & Side Navigation Bar

<img src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/32c4d1bf-423c-4761-9a27-40928af6d4db" width="800" />

#### News Article

<img src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/8f02c950-fbf4-480c-989b-2e25f3d95ffc" width="600" />

## Implementation

### Handling Location Permissions

During the course of developing the application, there were some opportunities to utilise functionality that I had not used before in a Kotlin application. One example of this was requesting the user's location and using that location as a basis for the news that is being retrieved.

Below shows an example snippet of how this was implemented in the appliaction. The countryCodeChecker function is used to determine if the current location is supported by the NewsAPI service.

```kotlin
private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission( // Re-check location permission
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) // Get address based on lat/lon of location

                        if (!addresses.isNullOrEmpty()) {
                            val country = addresses[0].countryName // Get the country name string from what geocoder provided

                            // Converts from country name to country code for supported regions, if region is unsupported then defaults to US
                            // For example, "United Kingdom" would return "gb" or China would return "cn"
                            countryCode = countryCodeChecker.checkCountryCode(country)

                            loadTopStories() // Load top stories based on the region retrieved

                        } else {
                            Log.d(TAG, "No address found for the provided coordinates.")

                            loadTopStories() // Load with default (US) data if no region found
                        }
                    } else {
                        Log.d(TAG, "Location is null")

                        loadTopStories() // Load with default (US) data if no region found
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting location: ${e.message}", e)

                    loadTopStories() // Load with default (US) data if no region found
                }
        }
    }
```

### Application Screenshots

Below shows screenshots of the final implementation of the application, including several of the activities in the application and some examples of mentioned functionality. Any user details in the applications were used for testing and are not associated with any actual person.

Application was developed using a Pixel 7 Pro API 34 Emulator

<p float="left">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/a855d247-f50b-416a-91ba-06973532b83a">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/34a35e44-ffe9-4846-a33c-14f0f88da512">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/d13022ee-516d-4a76-aa9f-84b95a744441">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/e4d42255-4feb-45eb-8c05-c46e5eaf67f0">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/d880797b-f45c-4c06-b05e-ffd4e1d0c57a">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/95850b36-3e1d-4f69-9dfb-a0d0b95c811f">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/914617d5-881c-4396-a397-eead6865c0b4">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/3f31c379-79a7-4678-9c4b-ac7588fc377a">
  <img width="260" src="https://github.com/SamC95/Kotlin_NewsApp/assets/132593571/ec98481b-44ae-4e6f-91fc-556a0cd9c0cb">
</p>

## Key Learnings

* Gained much more understanding of how to implement effective UI/UX design in an Android application, this included the use of things such as DrawerLayout, RecyclerViews and properly populating the application with data using adapters.
* Learnt how to utilise Firebase and Firestore in a Kotlin application for authentication and the storing/retrieval of user data.
* Improved general knowledge of both the Kotlin language and utilising APIs in an application.
* Was able to implement functionality for Google Login and tie that to Firebase effectively.
* Utilised the functionality to use the user's location data as a basis for data retrieval, using Geocoder and requesting the permission for the user's coarse and fine location information. Whilst ensuring that the application had other methods for setting location, either a default location (US) if permission is not allowed or allowing the user to set their own region for their account.

## Achievements

* I feel I was able to effectively design an application with a good user interface that was more involved than my prior experience with Kotlin.
* Met my overall planning & design goals in terms of features & functionality and ensured that my final application met the overall design of the storyboards.
* Maintained a consistent design across the entire application that makes it easy to use and navigate.
* Utilising the location functionality and allowing the user to set their own region and reflecting that appropriately with the news retrieved.
* I was able to use Firebase and Firestore in an effective manner and the application works smoothly in authenticating the user, storing their details and retrieving those details when needed.

## Challenges

There were some key challenges with the application, particularly in relation to ensuring that the UI/UX design met my intended goals based on the project storyboards. Getting the DrawerLayout with the Side Navigation Bar to work took some time to figure out but I was satisfied with the end result.

There are some aspects of the application that could be improved, particularly when it comes to cases where the news article may not have an associated image, or sometimes there can be unexpected behaviour when it comes to data being displayed in certain languages (Japanese, Korean & Chinese for example) where the news article activity can have some unintended formatting.

## Conclusions

I believe I was able to effectively meet my design goals with the application, implementing a much more robust android application that includes a consistent design across the entire application with several features that were an interesting challenge to implement due to the use of new functionality that I have not interacted with before (Location permissions, Google/Apple Login, RecyclerViews/Adapters & DrawerLayouts).

Changing the news retrieved based on the location was a particularly interesting feature to implement that allowed me to experiment with several approaches, as such the use of the NewsAPI was very beneficial in allowing me to come up with functionality that I may have not initially thought of when I was laying out the goals for the application; but after looking at the functionality that the API provided it became a key aspect that I wanted to include.

There are definitely some aspects that I could improve; such as some of the minor formatting issues of news articles in certain languages. Overall however, I feel the application allowed me to challenge myself to implement new types of functionality and create an application that was more involved than prior projects.

## Credits

Credit to [NewsAPI](https://newsapi.org) for the use of the news data in the application.

Credit for the Icons used in the application belongs to [Icons8](https://icons8.com/icons)
