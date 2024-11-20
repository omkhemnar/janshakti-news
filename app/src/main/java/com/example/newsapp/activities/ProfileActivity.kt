package com.example.newsapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.newsapp.R
import com.example.newsapp.adapters.ListDetailsAdapter
import com.example.newsapp.adapters.SectionDetails
import com.example.newsapp.data.UserDataManager
import com.google.android.material.navigation.NavigationView

class ProfileActivity : ComponentActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navHeaderText: TextView
    private lateinit var navView: NavigationView
    private lateinit var navButton: ImageButton

    private val userDataManager = UserDataManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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

        val userIcon = R.drawable.user_24
        val emailIcon = R.drawable.icons8_email_24
        val regionIcon = R.drawable.icons8_region_24
        val bookmarkIcon = R.drawable.icons8_bookmark_24

        val details = listOf(
            SectionDetails(userIcon, "First Name", ""),
            SectionDetails(userIcon, "Surname", ""),
            SectionDetails(emailIcon, "Email Address", ""),
            SectionDetails(regionIcon, "Country", ""),
            SectionDetails(bookmarkIcon, "Saved Articles", "")
        )

        val adapter = ListDetailsAdapter(this, details)

        val listView: ListView = findViewById(R.id.list_view)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                3 -> { // Opens activity for choosing a country
                    startActivity(Intent(this@ProfileActivity, CountrySelectorActivity::class.java))
                    finish()
                }
                4 -> { // Opens activity for viewing saved articles
                    startActivity(Intent(this@ProfileActivity, SavedArticlesActivity::class.java))
                }
            }
        }
    }
}