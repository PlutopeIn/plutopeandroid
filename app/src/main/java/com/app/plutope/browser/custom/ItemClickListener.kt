package com.app.plutope.browser.custom

/**
 * Created by Pravin on 17/01/2024.
 * Ahmedabad in India
 */
interface ItemClickListener {
    fun onItemClick(url: String?)
    fun onItemLongClick(url: String?) {} //only override this if extra handling is needed
}
