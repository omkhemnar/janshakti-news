package com.example.newsapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.newsapp.R

data class CountryDetails ( // Defines what is expected from the CountryDetails object
    val iconResId: Int,
    val countryName: String
)

class CountryAdapter(context: Context, details: List<CountryDetails>)
    : ArrayAdapter<CountryDetails>(context, 0, details) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.country_list_item, parent, false)

        val detail = getItem(position)!!

        val iconView = view.findViewById<ImageView>(R.id.list_icon)
        val countryNameView = view.findViewById<TextView>(R.id.country_name)

        iconView.setImageResource(detail.iconResId)
        countryNameView.text = detail.countryName // Sets the icon and country name appropriately

        return view
    }
}