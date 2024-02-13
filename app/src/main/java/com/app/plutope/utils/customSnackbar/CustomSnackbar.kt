package com.app.plutope.utils.customSnackbar

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.app.plutope.R
import com.google.android.material.snackbar.BaseTransientBottomBar



class CustomSnackbar(parent: ViewGroup, content: CustomSnackbarView) : BaseTransientBottomBar<CustomSnackbar>(parent, content, content) {

    init {
        getView().setBackgroundColor(ContextCompat.getColor(view.context, android.R.color.transparent))
        getView().setPadding(0, 0, 0, 150)

        val params = getView().layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.width = FrameLayout.LayoutParams.MATCH_PARENT
        getView().layoutParams = params
    }

    companion object {

        fun make(viewGroup: ViewGroup, message: String): CustomSnackbar {

            val customView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_custom_snackbar, viewGroup, false) as CustomSnackbarView

            customView.findViewById<TextView>(R.id.tvMessage).text = message

            return CustomSnackbar(viewGroup, customView)
        }

        fun showWithView(viewGroup: ViewGroup, message: String, layoutId: Int): CustomSnackbar {
            val customView = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.layout_custom_snackbar, viewGroup, false) as CustomSnackbarView

            customView.findViewById<TextView>(R.id.tvMessage).text = message

            return CustomSnackbar(viewGroup, customView)
        }
    }

}