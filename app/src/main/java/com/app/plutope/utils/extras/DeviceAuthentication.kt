package com.app.plutope.utils.extras

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.plutope.custom_views.CustomAlertDialog
import com.app.plutope.dialogs.DeviceLockFullScreenDialog


const val rc_biometric_enroll = 1001
const val lock_request_code = 1002
const val security_setting_request_code = 1003

fun Fragment.openDeviceLock() {
    val keyguardManager =
        activity!!.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
    val i = keyguardManager!!.createConfirmDeviceCredentialIntent(
        "PlutoPe locked", "Use device password to unlock"
    )
    try {
        startActivityForResult(i, lock_request_code)

        /*
                DeviceLockFullScreenDialog.getInstance().show(requireContext(),
                    object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
                        override fun onSubmitClicked(selectedList: String) {
                            // openPasscodeScreen()

                           // listener.success()



                        }
                    })
        */

    } catch (e: Exception) {
        openDialog(
            "Please set device lock screen to set up secure login.", Intent(
                DevicePolicyManager.ACTION_SET_NEW_PASSWORD
            ), security_setting_request_code
        )
    }
}

private fun Fragment.openDialog(message: String, intent: Intent, reqCode: Int) {
    var dialog: CustomAlertDialog? = null
    if (dialog == null) dialog = CustomAlertDialog(requireContext())
    dialog.dismiss()
    dialog.setCancelable(false)
    dialog.message = message
    dialog.setPositiveButton("Ok") {
        dialog.dismiss()
        try {
            startActivityForResult(intent, reqCode)
        } catch (ex: java.lang.Exception) {

            /*"Unable to find any Security settings so you have to set screen lock manually".showToast(
                requireContext()
            )*/
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
    }
    dialog.show()
}

fun Fragment.isDeviceSecure(): Boolean {
    val keyguardManager =
        requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager?
    return keyguardManager!!.isKeyguardSecure
}

fun Fragment.getPromptInfo(): androidx.biometric.BiometricPrompt.PromptInfo {
    return androidx.biometric.BiometricPrompt.PromptInfo.Builder().apply {
        setTitle("Biometric login for PlutoPe")
//        setDescription("Authenticate using your finger")
        setConfirmationRequired(false)
        setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
        setNegativeButtonText("Cancel")
    }.build()
}

fun Fragment.setBioMetric(listener: BiometricResult) {
    val biometricManager = BiometricManager.from(requireActivity())
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            DeviceLockFullScreenDialog.getInstance().show(requireContext(),
                object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
                    override fun onSubmitClicked(selectedList: String) {
                        listener.successCustomPasscode()
                    }
                })
            instanceOfBiometricPrompt(listener).authenticate(getPromptInfo())
        }

        else -> {
            DeviceLockFullScreenDialog.getInstance().show(
                requireContext(),
                object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
                    override fun onSubmitClicked(selectedList: String) {
                        listener.successCustomPasscode()
                    }
                })
        }
    }
}

fun Fragment.instanceOfBiometricPrompt(listener: BiometricResult): androidx.biometric.BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(requireActivity())
    val callback = object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            listener.failure(errorCode, errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            //   "Authentication failed for an unknown reason".showToast(requireContext())
        }

        override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            PreferenceHelper.getInstance().isBiometricAllow = true
            DeviceLockFullScreenDialog.getInstance().dismiss()
            listener.success()
        }
    }
    return androidx.biometric.BiometricPrompt(this, callback)
}


interface BiometricResult {
    fun success()
    fun failure(errorCode: Int, errorMessage: String)

    fun successCustomPasscode()
}


