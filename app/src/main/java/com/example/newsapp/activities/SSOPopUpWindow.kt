package com.example.newsapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.PopupWindow
import com.example.newsapp.R

class SSOPopUpWindow(private val context: Context) {
    private var backgroundDim: View? = null
    private var popupWindow: PopupWindow? = null

    @SuppressLint("ClickableViewAccessibility")
    fun showPopUpWindow(view: View) {
        // Inflate the popup_layout.xml
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val parentView = view.rootView as ViewGroup
        val popupView = inflater.inflate(R.layout.popup_layout, parentView, false)
        val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)

        // Create the popup window
        val width = ViewGroup.LayoutParams.WRAP_CONTENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupWindow.setOnDismissListener {
            dismissPopUpWindow()
        }

        // Show the popup window
        popupWindow.showAtLocation(view, android.view.Gravity.CENTER, 0, 0)

        popupView.startAnimation(fadeIn)

        // Creates a dimming effect when the pop up is visible
        backgroundDim = dimBackground(parentView)

        backgroundDim?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dismissPopUpWindow()
                return@setOnTouchListener true
            }
            return@setOnTouchListener false
        }

        // Handles close button behaviour
        popupView.setOnTouchListener { _, _ ->
            return@setOnTouchListener true
        }

        val closeButton = popupView.findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            popupWindow.dismiss()
        }
    }

    // Manages the background dimming effect
    private fun dimBackground(parentView: ViewGroup): View {
        val backgroundDimView = View(context)
        backgroundDimView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        backgroundDimView.setBackgroundColor(Color.parseColor("#80000000"))
        backgroundDimView.isClickable = true
        backgroundDimView.isFocusable = true
        parentView.addView(backgroundDimView)

        return backgroundDimView
    }

    // Handles the dismissal of the pop up window if anywhere on the dimming effect is touched
    private fun dismissPopUpWindow() {
        backgroundDim?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }
        popupWindow?.dismiss()
    }
}
