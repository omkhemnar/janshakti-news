package com.example.newsapp.activities

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.ArticlesAdapter
import com.example.newsapp.data.APIRequests
import com.example.newsapp.data.UserDataManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class SavedArticlesActivity : ComponentActivity(), ArticlesAdapter.OnArticleClickListener {
    private lateinit var noSavedArticles: TextView

    private lateinit var auth: FirebaseAuth

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navHeaderText: TextView
    private lateinit var navButton: ImageButton

    private val userDataManager = UserDataManager()

    private val db = Firebase.firestore

    private lateinit var articlesAdapter: ArticlesAdapter
    private val articlesList = mutableListOf<APIRequests.Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_articles)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navButton = findViewById(R.id.nav_button)
        noSavedArticles = findViewById(R.id.noResults)

        val headerView = navView.getHeaderView(0)
        navHeaderText = headerView.findViewById(R.id.navHeaderText)

        NavigationMenuListener.setListener(navView, this) // Sets up navigation menu buttons functionality

        // Sets up firebase authentication
        auth = Firebase.auth

        val recyclerView: RecyclerView = findViewById(R.id.newsArticlesView)

        // Initialize RecyclerView and Adapter
        articlesAdapter = ArticlesAdapter(articlesList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = articlesAdapter

        // Loads the articles the user has saved from firestore
        loadSavedArticles()

        // Sets up the user greeting on the side bar for this activity
        userDataManager.setupGreeting(this, navHeaderText)

        navButton.setOnClickListener {
            drawerLayout.openDrawer(navView)
        } // Opens side navigation menu

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadSavedArticles() {
        val userId = auth.currentUser?.uid ?: return // Gets user id, if null then return

        val userDocRef = db.collection("userSavedArticles").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) { // If the user has not saved articles before
                    noSavedArticles.text = getString(R.string.you_haven_t_saved_any_articles_yet)
                    return@addOnSuccessListener
                }

                // If the saved articles is currently empty
                val articles = document.get("articles") as? List<Map<String, Any>> ?: run {
                    noSavedArticles.text = getString(R.string.no_saved_articles)
                    return@addOnSuccessListener
                }

                articlesList.clear() // Clears any existing articles list

                for (articleMap in articles) {
                    val article = mapToArticle(articleMap)
                    if (article != null) {
                        articlesList.add(article)
                    }
                }
                articlesAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error loading articles: $error")
            }
    }

    // Maps the data retrieved from firestore into the Article Data Class from APIRequests
    private fun mapToArticle(articleMap: Map<String, Any>): APIRequests.Article? {
        return try {
            APIRequests.Article(
                sourceId = articleMap["sourceId"] as? String?,
                sourceName = articleMap["sourceName"] as? String ?: "",
                author = articleMap["author"] as? String?,
                title = articleMap["title"] as? String ?: "",
                description = articleMap["description"] as? String ?: "",
                url = articleMap["url"] as? String ?: "",
                urlToImage = articleMap["urlToImage"] as? String?,
                publishDate = articleMap["publishDate"] as? String ?: "",
                articleContent = articleMap["articleContent"] as? String?
            )
        }
        catch (error: Exception) {
            Log.e(TAG, "Error mapping article: $error")
            null
        }
    }

    override fun onArticleClick(position: Int) {
        val clickedArticle = articlesList[position]

        val articleIntent = Intent(this, ArticleActivity::class.java)
        articleIntent.putExtra("articleData", clickedArticle)
        startActivity(articleIntent)
    }
}