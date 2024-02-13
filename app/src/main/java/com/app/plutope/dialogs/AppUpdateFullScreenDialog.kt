package com.app.plutope.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.databinding.DialogAppUpdateBinding
import com.app.plutope.utils.constant.isFullScreenLockDialogOpen


class AppUpdateFullScreenDialog private constructor() {

    private var input: String = ""
    lateinit var binding: DialogAppUpdateBinding
    private val shakeDelay: Long = 300
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        var singleInstance: AppUpdateFullScreenDialog? = null

        fun getInstance(): AppUpdateFullScreenDialog {
            if (singleInstance == null) {
                singleInstance = AppUpdateFullScreenDialog()
            }
            return singleInstance!!
        }
    }

    private var fullScreenSuccessDialog: Dialog? = null

    fun show(context: Context, listener: DialogOnClickBtnListner) {
        isFullScreenLockDialogOpen = true
        if (fullScreenSuccessDialog == null) {
            fullScreenSuccessDialog = Dialog(context, R.style.full_screen)
        }


        R.layout.dialog_app_update

        binding = DialogAppUpdateBinding.inflate(LayoutInflater.from(context))

        fullScreenSuccessDialog!!.setContentView(binding.root)

        val layoutParams = fullScreenSuccessDialog?.window!!.attributes
        fullScreenSuccessDialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        fullScreenSuccessDialog?.window?.setBackgroundDrawableResource(android.R.color.white)
        fullScreenSuccessDialog?.window!!.attributes = layoutParams
        fullScreenSuccessDialog?.setCancelable(false)

        binding.btnUpdate.setOnClickListener {

            listener.onSubmitClicked("")
            fullScreenSuccessDialog?.dismiss()
        }

        if (!fullScreenSuccessDialog!!.isShowing) {

            try {
                fullScreenSuccessDialog?.show()
            } catch (e: WindowManager.BadTokenException) {
                e.printStackTrace()
            }
        } else {
            // fullScreenSuccessDialog?.dismiss()
        }

        binding.executePendingBindings()
    }

    fun dismiss() {
        if (fullScreenSuccessDialog!!.isShowing) {
            fullScreenSuccessDialog?.dismiss()
        }
    }

    fun appendInputText(text: String) {
        if (text.length + input.length > 6) {
            return
        }
        input += text
    }

    /**
     * remove last characters from input values and run not input animation
     */
    fun removeInputText() {
        if (input.isEmpty()) {
            return
        }

        input = input.dropLast(1)
    }


    interface DialogOnClickBtnListner {
        fun onSubmitClicked(selectedList: String)
    }

}