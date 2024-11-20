package com.example.newsapp.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.newsapp.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private lateinit var loginBtn: Button
    private lateinit var signUpBtn: Button
    private lateinit var icons8Text: TextView
    private lateinit var newsApiText: TextView

    private lateinit var auth: FirebaseAuth

    public override fun onStart() { // On App Start, if the user was logged in then skip login activities
        super.onStart()
        auth = Firebase.auth

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val automaticLoginIntent = Intent(this, NewsActivity::class.java)
            startActivity(automaticLoginIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginBtn = findViewById(R.id.loginBtn)
        signUpBtn = findViewById(R.id.signUpBtn)
        icons8Text = findViewById(R.id.icons8)
        newsApiText = findViewById(R.id.newsapi)

        // Links to web pages for credit
        icons8Text.movementMethod = LinkMovementMethod.getInstance()
        newsApiText.movementMethod = LinkMovementMethod.getInstance()

        // Navigates to Log in page
        loginBtn.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        // Navigates to Sign up page
        signUpBtn.setOnClickListener {
            val signUpIntent = Intent(this, AccCreationActivity::class.java)
            startActivity(signUpIntent)
        }
    }
}