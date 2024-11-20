package com.example.newsapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.newsapp.R
import com.example.newsapp.adapters.CountryAdapter
import com.example.newsapp.data.CountryRepository
import com.example.newsapp.data.UserDataManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class CountrySelectorActivity : ComponentActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navHeaderText: TextView
    private lateinit var navView: NavigationView
    private lateinit var navButton: ImageButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userDataManager = UserDataManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countries)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        NavigationMenuListener.setListener(navView, this) // Sets up navigation menu buttons functionality

        val headerView = navView.getHeaderView(0)
        navHeaderText = headerView.findViewById(R.id.navHeaderText)
        navButton = findViewById(R.id.nav_button)

        // Sets up the user greeting on the side bar for this activity
        userDataManager.setupGreeting(this, navHeaderText)

        navButton.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        val listView: ListView = findViewById(R.id.list_view)

        val countries = CountryRepository.getCountries()

        val countryAdapter = CountryAdapter(this, countries)
        listView.adapter = countryAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedCountry = countries[position]

            updateUserRegion(selectedCountry.countryName)
        }
    }

    // Updates the user's set region in firestore
    private fun updateUserRegion(newRegion: String) {
        val userId = auth.currentUser?.uid // Get the current logged in user's uid number

        val updatedRegion =
            if (newRegion == "Use location or default") {
                null // Sets the region to default state if the user selects to use location or default
            } else {
                newRegion // If user selects a country from the list then set the region to that
            }

        if (userId != null) {
            val userDocRef = db.collection("users").document(userId) // Gets the document based on the user's uid

            userDocRef.update("region", updatedRegion) // Updates the region based on the user's choice
                .addOnSuccessListener {
                    Toast.makeText(this, "Region successfully updated", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@CountrySelectorActivity, ProfileActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update region", Toast.LENGTH_SHORT).show()
                }
        }
    }
}