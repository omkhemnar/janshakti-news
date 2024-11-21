package com.example.newsapp.activities

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.newsapp.data.APIRequests
import com.example.newsapp.adapters.ArticlesAdapter
import com.example.newsapp.BuildConfig
import com.example.newsapp.data.CountryCodeChecker
import com.example.newsapp.data.UserDataManager
import com.example.newsapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.util.Locale

class NewsActivity : ComponentActivity(), ArticlesAdapter.OnArticleClickListener {
    private lateinit var noResultsText: TextView
    private lateinit var navHeaderText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navButton: ImageButton
    private lateinit var youtubeWebView: WebView
    private val apiRequests = APIRequests()
    private val countryCodeChecker = CountryCodeChecker()
    private val userDataManager = UserDataManager()
    private val apiKey = BuildConfig.NEWS_API_KEY
    private var countryCode = "us" // Default case
    private var regionResolved = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLastLocation()
        } else {
            Log.d(TAG, "Location permission denied")
            loadTopStories()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newsfeed)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        NavigationMenuListener.setListener(navView, this)

        val headerView = navView.getHeaderView(0)
        navHeaderText = headerView.findViewById(R.id.navHeaderText)

        navButton = findViewById(R.id.nav_button)
        noResultsText = findViewById(R.id.noResults)

        auth = Firebase.auth
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        youtubeWebView = findViewById(R.id.youtubeWebView)
        val webSettings = youtubeWebView.settings
        webSettings.javaScriptEnabled = true
        youtubeWebView.webViewClient = WebViewClient()
        youtubeWebView.loadUrl("https://www.youtube.com/@JanshaktiMarathiNews")

        if (savedInstanceState != null) {
            val savedCountry = savedInstanceState.getString("countryCode")
            if (savedCountry != null) {
                countryCode = savedCountry
            }
        }

        if (checkPermissions()) {
            loadTopStories()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        userDataManager.setupGreeting(this, navHeaderText)

        navButton.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }
    }

    private fun loadTopStories() {
        userDataManager.checkUserSetRegion { newCountry ->
            newCountry?.let {
                countryCode = it
            }
            regionResolved = true
            loadStoriesIfReady(apiKey)
        }
    }

    private fun loadStoriesIfReady(apiKey: String) {
        if (!regionResolved) {
            return
        }
        apiRequests.topStoriesRequest(countryCode, apiKey) { response, errorMessage ->
            runOnUiThread {
                if (errorMessage != null) {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                } else {
                    val articlesAdapter = ArticlesAdapter(response, this)
                    noResultsText.text = if (response.isEmpty()) getString(R.string.no_results_found) else ""
                }
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val country = addresses[0].countryName
                            countryCode = countryCodeChecker.checkCountryCode(country)
                            loadTopStories()
                        } else {
                            loadTopStories()
                        }
                    } else {
                        loadTopStories()
                    }
                }
                .addOnFailureListener { e -> loadTopStories() }
        }
    }

    private fun checkPermissions(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onArticleClick(position: Int) {
        TODO("Not yet implemented")
    }
}
