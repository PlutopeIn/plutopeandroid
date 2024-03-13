package com.app.plutope.utils.dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.airbnb.lottie.LottieAnimationView
import com.app.plutope.R
import com.app.plutope.utils.loge
import java.util.Timer
import java.util.TimerTask

class LoadingDialog  {

    companion object {
        private var singleInstance: LoadingDialog? = null
        fun getInstance(): LoadingDialog? {
            if (singleInstance == null) {
                singleInstance = LoadingDialog()
            }
            return singleInstance
        }

        fun getClearInstence() {
            singleInstance = null
        }

    }
    private var loadingDialog: Dialog? = null
    fun show(context: Context) {

        loge("LoadingDialog", "Status $loadingDialog")
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            return
        }

        loadingDialog = Dialog(context, R.style.Theme_PlutoPeApp_ErrorDialog)
        loadingDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val inflater =
            LayoutInflater.from(context).inflate(R.layout.layout_loader_animation, null, false)
        loadingDialog!!.setContentView(inflater)

        val layoutParams = loadingDialog?.window!!.attributes
        loadingDialog?.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        loadingDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        loadingDialog?.window!!.attributes = layoutParams
//        loadingDialog?.window!!.attributes.dimAmount = 0.0f
        loadingDialog?.setCancelable(false)
        val lottieAnimationView = loadingDialog!!.findViewById<LottieAnimationView>(R.id.animation_loader)

        if (!loadingDialog!!.isShowing && loadingDialog != null) {
            lottieAnimationView.playAnimation()
            loadingDialog?.show()

        } else {

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    lottieAnimationView.cancelAnimation()
                    loadingDialog?.dismiss()
                }
            }, 1500)
        }
    }

    fun dismiss() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog?.dismiss()
/*
            Timer().schedule(object : TimerTask() {
                override fun run() {

                }
            }, 1500)
*/

            // loadingDialog?.dismiss()
        }
    }

}