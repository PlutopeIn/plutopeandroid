package com.app.plutope.ui.base

import android.app.Application
import android.content.Context
import com.app.plutope.utils.extras.PreferenceHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class   App : Application() {

    @Inject
    lateinit var sharedPreferences :PreferenceHelper

    override fun onCreate() {
        super.onCreate()

        context = applicationContext


//        changeLanguage()

    }

/*
    private fun changeLanguage() {
        val change: String

        val language = sharedPreferences.currentLanguage

        change = if (language == "Thai") {
            "th"
        } else if (language == "English") {
            "en"
        } else {
            ""
        }

        BaseActivity.dLocale = Locale(change)
    }
*/

    companion object {

        private var context: Context? = null
        fun getContext(): Context {
            return context!!
        }
    }


}