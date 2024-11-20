package com.example.newsapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.newsapp.R
import com.example.newsapp.data.UserData
import com.example.newsapp.data.UserDataManager

data class SectionDetails (
    val iconResId: Int,
    val sectionName: String,
    val userDetails: String,
)

class ListDetailsAdapter(context: Context, details: List<SectionDetails>)
    : ArrayAdapter<SectionDetails>(context, 0, details) {

        private val userDataManager = UserDataManager() // userDataManager to call getUserData

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val detail = getItem(position)!! // Gets the current detail item

        val iconView = view.findViewById<ImageView>(R.id.list_icon)
        val sectionNameView = view.findViewById<TextView>(R.id.section_name)
        val userDetailView = view.findViewById<TextView>(R.id.user_detail)

        // Sets icon and section name
        iconView.setImageResource(detail.iconResId)
        sectionNameView.text = detail.sectionName

        // Gets the required user data based on the section name
        userDataManager.getUserData { userData: UserData? ->
            userData?.let {
                val userDetail = when (detail.sectionName) {
                    "First Name" -> it.firstName
                    "Surname" -> it.surname
                    "Email Address" -> it.email
                    "Country" -> it.region ?: "Click here to set your country"
                    "Saved Articles" -> "Click here to view saved articles"
                    "Delete Account" -> "Click here to permanently delete your account and all associated data"
                    else -> ""
                }
                // Updates the view on the main thread
                view.post {
                    userDetailView.text = userDetail
                }
            }
        }
        return view
    }
}