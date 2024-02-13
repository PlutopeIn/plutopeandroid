package com.app.plutope.browser.custom

interface URLLoadInterface {
    fun onWebpageLoaded(url: String?, title: String?)
    fun onWebpageLoadComplete()
}