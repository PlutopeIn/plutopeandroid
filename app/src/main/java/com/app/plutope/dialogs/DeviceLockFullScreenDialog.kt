package com.app.plutope.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.WindowManager
import com.app.plutope.R
import com.app.plutope.custom_views.CustomKeyboardView
import com.app.plutope.databinding.DialogDeviceLockScreenBinding
import com.app.plutope.passcode.pass2.PassCodeView
import com.app.plutope.utils.constant.isFullScreenLockDialogOpen
import com.app.plutope.utils.extras.PreferenceHelper


class DeviceLockFullScreenDialog private constructor() {

    private var input: String = ""
    lateinit var binding: DialogDeviceLockScreenBinding
    private val shakeDelay: Long = 300
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        var singleInstance: DeviceLockFullScreenDialog? = null

        fun getInstance(): DeviceLockFullScreenDialog {
            if (singleInstance == null) {
                singleInstance = DeviceLockFullScreenDialog()
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


        binding = DialogDeviceLockScreenBinding.inflate(LayoutInflater.from(context))

        fullScreenSuccessDialog!!.setContentView(binding.root)

        val layoutParams = fullScreenSuccessDialog?.window!!.attributes
        fullScreenSuccessDialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        fullScreenSuccessDialog?.window?.setBackgroundDrawableResource(android.R.color.white)
        fullScreenSuccessDialog?.window!!.attributes = layoutParams
        fullScreenSuccessDialog?.setCancelable(false)


        binding.keyboardView.listner = object : CustomKeyboardView.NotifyKeyListener {
            override fun getValue(value: String) {
                appendInputText(value)
                binding.passwordView.setPassCode(input)
            }

            override fun removeValue() {
                removeInputText()
                binding.passwordView.setPassCode(input)
            }

            override fun removeAllValue() {
                input = ""
                binding.passwordView.setPassCode(input)

            }
        }

        binding.passwordView.setOnTextChangeListener(object : PassCodeView.TextChangeListener {
            override fun onTextChanged(text: String?) {
                if (text?.length == 6) {
                    if (text != PreferenceHelper.getInstance().appPassword) {
                        binding.passwordView.setFilledDrawable(R.drawable.ic_asterisk_error)
                        handler.postDelayed({
                            input = ""
                            binding.passwordView.startShakeAnimation()
                            binding.passwordView.reset()
                        }, shakeDelay)

                    } else {
                        isFullScreenLockDialogOpen = false
                        input = ""
                        listener.onSubmitClicked("")
                        fullScreenSuccessDialog?.dismiss()
                    }

                }
            }
        })




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