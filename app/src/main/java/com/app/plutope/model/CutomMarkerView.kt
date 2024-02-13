package com.app.plutope.model

import android.content.Context
import android.widget.TextView
import com.app.plutope.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class CustomMarkerView(context: Context, layoutResource: Int ) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(entry: Entry?, highlight: Highlight?) {
        entry?.let {
            tvContent.text = entry.y.toString()
        }
        super.refreshContent(entry, highlight)
    }

    // Set the content of the marker based on the provided Entry object
    fun setEntry(entry: Entry) {
        tvContent.text = "$${entry.y}"
    }

    override fun getOffset(): MPPointF {

        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }


}
