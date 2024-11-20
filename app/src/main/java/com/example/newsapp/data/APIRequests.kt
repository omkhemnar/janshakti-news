package com.example.newsapp.data

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class APIRequests {
    data class Article( // Handles the data class for an Article, along with the functionality required to make it Parcelable
        val sourceId: String?,
        val sourceName: String,
        val author: String?,
        val title: String,
        val description: String,
        val url: String,
        val urlToImage: String?,
        val publishDate: String,
        val articleContent: String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString().toString(),
            parcel.readString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString(),
            parcel.readString().toString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(sourceId)
            parcel.writeString(sourceName)
            parcel.writeString(author)
            parcel.writeString(title)
            parcel.writeString(description)
            parcel.writeString(url)
            parcel.writeString(urlToImage)
            parcel.writeString(publishDate)
            parcel.writeString(articleContent)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Article> {
            override fun createFromParcel(parcel: Parcel): Article {
                return Article(parcel)
            }

            override fun newArray(size: Int): Array<Article?> {
                return arrayOfNulls(size)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun topStoriesRequest(country: String, newsApiKey: String, callback: (MutableList<Article>, String?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) { // Retrieves the top stories for a given country
            try {
                val url =
                    URL("https://newsapi.org/v2/top-headlines?country=$country&apiKey=$newsApiKey&pageSize=100") // API URL

                with(url.openConnection() as HttpsURLConnection) {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/126.0")

                    val response = StringBuilder()
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line).append("\n")
                    } // Appends each line onto the response variable
                    reader.close()

                    val articles =
                        parseArticles(response.toString()) // Parses response into an Article Class object
                    callback(articles, null) // Returns the articles with no error
                }
            }
            catch (exception: FileNotFoundException) {
                Log.d(TAG, "API Rate Limit Reached: $exception")

                callback(mutableListOf(), "API error occurred") // If API limit has been reached, returns an empty list and an error string
            }
            catch (e: Exception) {
                Log.e(TAG, "Network Error Occurred: ${e.message}", e)
                callback(mutableListOf(), "Network Error occurred: ${e.message}")
            }
        }
    }

    // Function for retrieving search results from the API based on a specific user search input
    @OptIn(DelicateCoroutinesApi::class)
    fun searchRequests(newsApiKey: String, userInput: String, callback: (MutableList<Article>, String?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url =
                    URL("https://newsapi.org/v2/everything?q=$userInput&apiKey=$newsApiKey&pageSize=100") // API URL

                with(url.openConnection() as HttpsURLConnection) {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/126.0")

                    val response = StringBuilder()
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line).append("\n")
                    } // Appends each line onto the response variable
                    reader.close()

                    val articles =
                        parseArticles(response.toString()) // Parses response into an Article Class object
                    callback(articles, null) // Returns th articles with no error
                }
            }
            catch (exception: FileNotFoundException) {
                Log.d(TAG, "API Rate Limit Reached: $exception")

                callback(mutableListOf(), "API error occurred") // If API limit has been reached, return an empty list and error string
            }
            catch (e: Exception) {
                Log.e(TAG, "Network error Occurred: ${e.message}", e)
                callback(mutableListOf(), "Network error occurred: ${e.message}")
            }
        }
    }

    // Functions similarly to topStoriesRequest, but returns results for a specific category (i.e., Business, Technology, Sports)
    @OptIn(DelicateCoroutinesApi::class)
    fun categoryRequests(country: String, categoryName: String, newsApiKey: String, callback: (MutableList<Article>, String?) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url =
                    URL("https://newsapi.org/v2/top-headlines?country=$country&category=$categoryName&apiKey=$newsApiKey&pageSize=100")

                with(url.openConnection() as HttpsURLConnection) {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/126.0")

                    val response = StringBuilder()
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line).append("\n")
                    }
                    reader.close()

                    val articles = parseArticles(response.toString())
                    callback(articles, null)
                }
            }
            catch (exception: FileNotFoundException) {
                Log.d(TAG, "API Rate Limit Reached: $exception")

                callback(mutableListOf(), "API error occurred")
            }
            catch (e: Exception) {
                Log.e(TAG, "Network Error Occurred: ${e.message}", e)
                callback(mutableListOf(), "Network Error occurred: ${e.message}")
            }
        }
    }

    // -- NOT USED --
    fun storeArticles(articles: List<Article>) {
        val firestore = Firebase.firestore
        val articlesCollection = firestore.collection("newsArticles")

        for (article in articles) {
            val articleMap = hashMapOf(
                "sourceId" to article.sourceId,
                "sourceName" to article.sourceName,
                "author" to article.author,
                "title" to article.title,
                "description" to article.description,
                "url" to article.url,
                "urlToImage" to article.urlToImage,
                "publishedAt" to article.publishDate,
                "content" to article.articleContent
            )

            articlesCollection.add(articleMap)
                .addOnSuccessListener {
                    println("Article added with ID: ${it.id}")
                }
                .addOnFailureListener { error ->
                    println("Error adding article: $error")
                }
        }
    }

    // Parses the article data into fields on an Article object so that specific fields can be requested later
    private fun parseArticles(jsonResponse: String): MutableList<Article> {
        val articles = mutableListOf<Article>()
        val jsonObject = JSONObject(jsonResponse)
        val jsonArray = jsonObject.getJSONArray("articles")

        for (i in 0 until jsonArray.length()) {
            val articleObject = jsonArray.getJSONObject(i)

            val sourceId = articleObject.getJSONObject("source").getString("id")
            val sourceName = articleObject.getJSONObject("source").getString("name")
            val author = articleObject.optString("author")
            val title = articleObject.getString("title")
            val description = articleObject.getString("description")
            val url = articleObject.getString("url")
            val urlToImage = articleObject.optString("urlToImage")
            val publishDate = articleObject.getString("publishedAt")
            val content = articleObject.optString("content")


            // Filters out deleted articles so that they do not appear in the results
            if (title != "[Removed]") {
                articles.add(
                    Article(
                        sourceId, sourceName, author, title, description,
                        url, urlToImage, publishDate, content)
                )
            }
        }
        return articles
    }
}