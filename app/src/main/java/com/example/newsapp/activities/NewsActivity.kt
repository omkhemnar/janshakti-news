package com.example.newsapp.activities

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.data.APIRequests
import com.example.newsapp.adapters.ArticlesAdapter
import com.example.newsapp.BuildConfig
import com.example.newsapp.data.CountryCodeChecker
import com.example.newsapp.adapters.NewsTypeAdapter
import com.example.newsapp.data.UserDataManager
import com.example.newsapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import java.util.Locale

class NewsActivity : ComponentActivity(), NewsTypeAdapter.OnItemClickListener, ArticlesAdapter.OnArticleClickListener {
    private lateinit var noResultsText: TextView
    private lateinit var newsSearchBar: EditText
    private lateinit var navHeaderText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navButton: ImageButton
    private lateinit var youtubeWebView: WebView
    private val dataSet = listOf(
        "Top Stories", "Business", "General", "Health", "Entertainment",
        "Science", "Sport", "Technology"
    )
    private val apiRequests = APIRequests()
    private val countryCodeChecker = CountryCodeChecker()
    private val userDataManager = UserDataManager()
    private val apiKey = BuildConfig.NEWS_API_KEY
    private var countryCode = "us" // Default case
    private var isTopStoriesSelected: Boolean = true
    private var selectedCategory: String? = null
    private var articles: List<APIRequests.Article> = emptyList()
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

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        val newsTypeAdapter = NewsTypeAdapter(dataSet, this)
        recyclerView.adapter = newsTypeAdapter

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        NavigationMenuListener.setListener(navView, this)

        val headerView = navView.getHeaderView(0)
        navHeaderText = headerView.findViewById(R.id.navHeaderText)

        navButton = findViewById(R.id.nav_button)
        noResultsText = findViewById(R.id.noResults)
        newsSearchBar = findViewById(R.id.newsSearchBar)

        auth = Firebase.auth
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        youtubeWebView = findViewById(R.id.youtubeWebView)
        val webSettings = youtubeWebView.settings
        webSettings.javaScriptEnabled = true
        youtubeWebView.webViewClient = WebViewClient()
        youtubeWebView.loadUrl("https://www.youtube.com/@JanshaktiMarathiNews")

        if (savedInstanceState != null) {
            val newsSearch = savedInstanceState.getString("newsSearchInput")
            val savedCountry = savedInstanceState.getString("countryCode")
            isTopStoriesSelected = savedInstanceState.getBoolean("isTopStoriesSelected")
            selectedCategory = savedInstanceState.getString("selectedCategory")
            newsSearchBar.setText(newsSearch)
            if (savedCountry != null) {
                countryCode = savedCountry
            }
        }

        if (checkPermissions()) {
            if (isTopStoriesSelected) {
                loadTopStories()
            } else {
                selectedCategory?.let { category ->
                    onItemClicked(0, category)
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        userDataManager.setupGreeting(this, navHeaderText)

        navButton.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        newsSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val userInput = newsSearchBar.text.toString()
                if (userInput != "") {
                    performSearch(apiKey, userInput)
                }
                true
            } else {
                false
            }
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
                    articles = response.toList()
                    val articlesAdapter = ArticlesAdapter(response, this)
                    val newsRecyclerView: RecyclerView = findViewById(R.id.newsArticlesView)
                    newsRecyclerView.layoutManager = LinearLayoutManager(this)
                    newsRecyclerView.adapter = articlesAdapter
                    noResultsText.text = if (response.isEmpty()) getString(R.string.no_results_found) else ""
                }
            }
        }
    }

    private fun performSearch(apiKey: String, userInput: String) {
        apiRequests.searchRequests(apiKey, userInput) { response, errorMessage ->
            runOnUiThread {
                if (errorMessage != null) {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                } else {
                    articles = response.toList()
                    val articlesAdapter = ArticlesAdapter(response, this)
                    val newsRecyclerView: RecyclerView = findViewById(R.id.newsArticlesView)
                    newsRecyclerView.layoutManager = LinearLayoutManager(this)
                    newsRecyclerView.adapter = articlesAdapter
                    noResultsText.text = if (response.isEmpty()) getString(R.string.no_results_found) else ""
                }
            }
        }
    }

    override fun onItemClicked(position: Int, data: String) {
        if (data == "Top Stories") {
            loadTopStories()
            isTopStoriesSelected = true
            selectedCategory = null
        } else {
            isTopStoriesSelected = false
            selectedCategory = data
            apiRequests.categoryRequests(countryCode, data.lowercase(), apiKey) { response, errorMessage ->
                runOnUiThread {
                    if (errorMessage != null) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    } else {
                        articles = response.toList()
                        val articlesAdapter = ArticlesAdapter(response, this)
                        val newsRecyclerView: RecyclerView = findViewById(R.id.newsArticlesView)
                        newsRecyclerView.layoutManager = LinearLayoutManager(this)
                        newsRecyclerView.adapter = articlesAdapter
                        noResultsText.text = if (response.isEmpty()) getString(R.string.no_results_found) else ""
                    }
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
                .addOnFailureListener { e ->
                    loadTopStories()
                }
        }
    }

    private fun checkPermissions(): Boolean {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onArticleClick(position: Int) {
        TODO("Not yet implemented")
    }
}
