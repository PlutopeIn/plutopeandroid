package com.app.plutope.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class MiddleEllipsisEditText : AppCompatEditText {

    private var maxVisibleChars: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setMaxVisibleChars(maxVisibleChars: Int) {
        this.maxVisibleChars = maxVisibleChars
    }

    override fun setText(text: CharSequence?, type: BufferType) {
        val ellipsizedText = text?.let {
            if (it.length > maxVisibleChars) {
                val middleStart = it.length / 2 - maxVisibleChars / 2
                val middleEnd = it.length / 2 + maxVisibleChars / 2

                val middleText = "..." // Customize the ellipsis as needed
                "${it.subSequence(0, middleStart)}$middleText${it.subSequence(middleEnd, it.length)}"
            } else {
                it.toString()
            }
        } ?: ""

        super.setText(ellipsizedText, type)
    }
}
