package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.data.APIRequests
import com.example.newsapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArticlesAdapter(private val articles: List<APIRequests.Article>, private val listener: OnArticleClickListener) : RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder>() {
    interface OnArticleClickListener {
        fun onArticleClick(position: Int)
    }

    class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sourceName: TextView = view.findViewById(R.id.sourceName)
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)
        val urlToImage: ImageView = view.findViewById(R.id.urlToImage)
        val publishedAt: TextView = view.findViewById(R.id.publishedAt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position] // Gets the position of the article in the list

        holder.sourceName.text = article.sourceName

        if (article.sourceName == "null") { // If the source name is null, try author instead
            holder.sourceName.text = article.author
        }
        else {
            holder.sourceName.text = article.sourceName
        }

        holder.title.text = article.title // Source Name and Title of the Article

        if (article.description != "null") { // Article description (can be null, so checks for that)
            holder.description.text = article.description
        }
        else {
            holder.description.text = ""
        }

        holder.publishedAt.text = formatDateTime(article.publishDate) // Date when published

        if (article.urlToImage != "null") { // Handles how the image appears or does not if the article has one
            holder.urlToImage.visibility = View.VISIBLE
            holder.urlToImage.layoutParams.height = LayoutParams.WRAP_CONTENT
            Glide.with(holder.itemView.context)
                .load(article.urlToImage)
                .into(holder.urlToImage)
        }
        else { // If there is no image, we hide the section so that there is no empty space (vertically)
            holder.urlToImage.visibility = View.GONE
            holder.urlToImage.layoutParams.height = 0
        }

        holder.itemView.setOnClickListener {
            listener.onArticleClick(position) // Handles when the user clicks on the article
                                              // See overridden function in NewsActivity.kt
        }
    }

    override fun getItemCount() = articles.size

}

fun formatDateTime(dateTimeString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss", Locale.getDefault())
    val date = inputFormat.parse(dateTimeString)
    return outputFormat.format(date ?: Date())
}