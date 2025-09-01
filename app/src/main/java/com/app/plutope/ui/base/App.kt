package com.app.plutope.ui.base

import android.app.Application
import android.content.Context
import com.app.plutope.R
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.loge
import dagger.hilt.android.HiltAndroidApp
import zendesk.android.Zendesk
import zendesk.messaging.android.DefaultMessagingFactory
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var sharedPreferences :PreferenceHelper


    override fun onCreate() {
        super.onCreate()
        context = applicationContext

/*
        startKoin {
            androidLogger()
            androidContext(androidContext = this@App)
        }
*/

      //  appsFlyerInitialization()
        zenDeskInitialization()





    }

    private fun zenDeskInitialization() {
        Zendesk.initialize(this, this.getString(R.string.channel_key), successCallback = { zendesk ->
            loge(LOG_TAG, getString(R.string.msg_init_success))
        }, failureCallback = { error ->
            loge(LOG_TAG, "${getString(R.string.msg_init_error)}: $error")
        }, messagingFactory = DefaultMessagingFactory())
    }

   /* private fun appsFlyerInitialization() {
        val appsFlyer: AppsFlyerLib = AppsFlyerLib.getInstance()
        if (BuildConfig.DEBUG){
            appsFlyer.setDebugLog(true)
        }else{
            appsFlyer.setDebugLog(false)
        }

        appsFlyer.init(afDevKey,null,this)
        appsFlyer.start(this, null, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                logd(LOG_TAG, "Launch sent successfully")
            }
            override fun onError(errorCode: Int, errorDesc: String) {
                logd(LOG_TAG, "Launch failed to be sent:\n" +
                        "Error code: " + errorCode + "\n"
                        + "Error description: " + errorDesc)
            }
        })

        val cUid = appsFlyer.getAppsFlyerUID(this)
        loge("CUID","cUid =>  $cUid")
    }
*/

    companion object {

        private var context: Context? = null

        const val LOG_TAG: String = "PlutoPeApp"
        const val DL_ATTRS: String = "dl_attrs"

        fun getContext(): Context {
            return context!!
        }
    }


}