package com.example.newsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R

class NewsTypeAdapter(private val dataSet: List<String>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<NewsTypeAdapter.NewsTypeHolder>() {

    interface OnItemClickListener {
        fun onItemClicked(position: Int, data: String)
    }

    // ViewHolder class that holds references to the views for each item
    class NewsTypeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTypeButton: TextView = itemView.findViewById(R.id.newsTypeButton)
    }

    // Create new views for the different news types
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsTypeHolder {
        // Create a new view, which defines the UI of the list item
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)

        return NewsTypeHolder(itemView)
    }

    // Replaces the view content with the list of strings from the data set
    override fun onBindViewHolder(holder: NewsTypeHolder, position: Int) {
        holder.newsTypeButton.text = dataSet[position]

        holder.newsTypeButton.setOnClickListener {
            itemClickListener.onItemClicked(position, dataSet[position])
        }
    }

    // Gets the size of the data set
    override fun getItemCount() = dataSet.size
}