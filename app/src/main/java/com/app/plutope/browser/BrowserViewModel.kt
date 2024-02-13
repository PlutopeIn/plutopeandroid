package com.app.plutope.browser

import android.content.Context
import android.preference.PreferenceManager
import com.app.plutope.browser.browserModel.DApp
import com.app.plutope.browser.utils.getDappsList
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrowserViewModel @Inject constructor() : BaseViewModel<CommonNavigator>() {


    fun getDAppsMasterList(context: Context?): MutableList<DApp>? {
        return getDappsList(context)
    }

    fun getHomePage(context: Context?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(C.DAPP_HOMEPAGE_KEY, null)
    }

}