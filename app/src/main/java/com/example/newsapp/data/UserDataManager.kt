package com.example.newsapp.data

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.widget.TextView
import com.example.newsapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar

data class UserData(
    val firstName: String?,
    val surname: String?,
    val email: String?,
    val region: String?
)

class UserDataManager {
    private val db = FirebaseFirestore.getInstance() // Sets up the firestore database
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid // Gets the currently logged in user's id

    private val countryCodeChecker = CountryCodeChecker()

    private val calendar: Calendar = Calendar.getInstance()
    private val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // Gets the current hour based on the device clock

    private val auth = FirebaseAuth.getInstance()

    private val navHeaderGreeting = when(currentHour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    } // Depending on the current time, alters the string to display an appropriate message for that time

     fun getUserData(completion: (UserData?) -> Unit) {
        currentUserId?.let { uid ->
            val docRef = db.collection("users").document(uid) // Get appropriate document from firebase based on uid

            docRef.get()
                .addOnSuccessListener {
                    if (it != null) { // If field is found via that uid, get the data
                        val firstName = it.getField<String>("firstName")
                        val surname = it.getField<String>("surname")
                        val emailAddress = it.getField<String>("email")
                        val region = it.getField<String>("region")

                        val userData = UserData(firstName, surname, emailAddress, region) // Store data into UserData object

                        completion(userData) // Returns userData
                    }
                    else {
                        completion(null) // Returns nothing if the document was not found
                    }
                }.addOnFailureListener { exception ->
                    Log.e("UserDataManager", "Error getting user data: $exception")
                    completion(null) // Handles error
                }
        }
    }

    // Handles setting up the user greeting on the side nav menu
    // For example, if the device time is 3PM then it should return "Good afternoon, <firstName>!"
    fun setupGreeting(context: Context, textView: TextView) {
        getUserData { userData ->
            if (userData?.firstName != null) { // If user data is retrieved
                val firstName = userData.firstName
                // Update text with appropriate name
                textView.text =
                    context.getString(R.string.navHeaderGreeting, navHeaderGreeting, firstName)
            }
            else {
                textView.text = context.getString(R.string.navHeaderGreetingNoName, navHeaderGreeting)
            }
        }
    }

    // Checks if the user region has been set by the user in the profile section
     fun checkUserSetRegion(callback: (String?) -> Unit) {
         currentUserId?.let { uid ->
             val docRef = db.collection("users").document(uid)

             docRef.get()
                 .addOnSuccessListener { documentSnapshot ->
                     if (documentSnapshot != null) {
                         val region = documentSnapshot.getField<String>("region")

                         if (region != null) {
                             val countryCode = countryCodeChecker.checkCountryCode(region)
                             callback(countryCode)
                         }
                         else {
                             callback(null)
                         }
                     }
                 }
                 .addOnFailureListener {
                     Log.e(TAG, "Error fetching user document")
                     callback(null)
                 }
         } ?: callback(null)
    }

    suspend fun saveGoogleUserToFirestore(user: FirebaseUser) {
        // Checks if a document exists in users for this user's uid
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
        val userDoc = userDocRef.get().await()

        if (userDoc.exists()) { // If the user already exists in firestore then don't save the data
            Log.d(TAG, "User already stored")
            return
        }

        // Splits user first name and surname into separate variables
        val (firstName, surname) = splitFullName(user.displayName ?: "")

        val userDetails = hashMapOf( // Hashmap of the user details to be added to firestore
            "firstName" to firstName,
            "surname" to surname,
            "email" to user.email,
            "region" to null
        )

        try {
            withContext(Dispatchers.IO) {
                FirebaseFirestore.getInstance().collection("users")
                    .document(user.uid)
                    .set(userDetails) // Adds user details to firestore document with name of user uid
            }.await()

            Log.d(TAG, "User details saved")
        }
        catch (error: Exception) {
            Log.e(TAG, "Error adding document to Firestore: $error")
        }
    }

    // Splits the user's first name and last name into separate variables
    private fun splitFullName(fullName: String): Pair<String, String> {
        val parts = fullName.split(" ")

        val firstName = parts.firstOrNull() ?: ""
        val surname = parts.drop(1).joinToString(" ")

        return Pair(firstName, surname)
    }

    // Function for deleting the user's account on firebase when requested by the user
     fun deleteAccount(callback: (Boolean, String?) -> Unit) {
        val user: FirebaseUser? = auth.currentUser

        user?.let {
            deleteUserData(user.uid) { deletionStatus, deletionMessage ->
                if (deletionStatus) { // If user data was successfully deleted
                    user.delete() // then delete user account entirely
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                callback(true, null)
                            }
                            else {
                                task.exception?.let { exception ->
                                    callback(false, exception.message)
                                }
                            }
                        }
                }
                else {
                    callback(false, deletionMessage)
                }
            }
        } ?: run {
            callback(false, "No user currently signed in")
        }
    }

    // Deletes the user's data from firestore if they choose to delete their account
    private fun deleteUserData(uid: String, callback: (Boolean, String?) -> Unit) {
        val userDocRef = db.collection("users").document(uid)
        val userSavedArticlesRef = db.collection("userSavedArticles").document(uid)
        // Gets the users data from firestore based on the user's uid

        val batch = db.batch()
        batch.delete(userDocRef)
        batch.delete(userSavedArticlesRef) // Deletes the user data and their saved articles

        batch.commit().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, null)
            }
            else {
                task.exception?.let { exception ->
                    callback(false, exception.message)
                }
            }
        }
    }
}
