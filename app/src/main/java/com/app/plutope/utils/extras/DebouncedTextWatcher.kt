package com.app.plutope.utils.extras

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher

class DebouncedTextWatcher(
    private val debounceMs: Long = 1000, // Adjust the debounce time as needed
    private val onTextChanged: (CharSequence?) -> Unit
) : TextWatcher {

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // No action needed
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Cancel any previous runnables in the handler
        handler.removeCallbacks(runnable!!)

        // Schedule a new runnable after the debounce time
        runnable = Runnable {
            onTextChanged.invoke(s)
        }
        handler.postDelayed(runnable!!, debounceMs)
    }

    override fun afterTextChanged(s: Editable?) {
        // No action needed
    }
}