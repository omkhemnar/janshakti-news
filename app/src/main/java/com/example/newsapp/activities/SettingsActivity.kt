package com.example.newsapp.activities

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.newsapp.R
import com.example.newsapp.adapters.ListDetailsAdapter
import com.example.newsapp.adapters.SectionDetails
import com.example.newsapp.data.UserDataManager
import com.google.android.material.navigation.NavigationView

class SettingsActivity : ComponentActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navHeaderText: TextView
    private lateinit var navView: NavigationView
    private lateinit var navButton: ImageButton

    private val userDataManager = UserDataManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

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

        val deleteIcon = R.drawable.icons8_user_shield_24

        val details = listOf(
            SectionDetails(deleteIcon, "Delete Account", "")
        )

        val adapter = ListDetailsAdapter(this, details)

        val listView: ListView = findViewById(R.id.list_view)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    showConfirmationDialog()
                }
            }
        }
    }

    // Handles the dialog window for when the user attempts to delete their account
    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this) // Opens a warning alertdialog

        builder.setTitle("Deletion Confirmation")
        builder.setMessage("Are you sure you want to delete your account? This is non-reversible.")

        builder.setPositiveButton("Delete") { _, _ -> // If this is chosen, deletion process begins
            userDataManager.deleteAccount() { success, message ->
                if (success) {
                    Toast.makeText(this, "Account successfully deleted", Toast.LENGTH_LONG).show()

                    val logoutIntent = Intent(this, MainActivity::class.java)
                    startActivity(logoutIntent)
                    finish() // Returns to app start activity upon successful deletion
                }
                else { // If something went wrong in the process, display a message to the user for clarity
                    Toast.makeText(this, "Sorry, something went wrong!", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "An error occurred deleting the account: $message")
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss() // On Cancel press, do nothing and just close the dialog box
        }

        builder.show() // Allows the dialog window to appear
    }
}