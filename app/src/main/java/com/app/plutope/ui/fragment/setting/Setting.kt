package com.app.plutope.ui.fragment.setting

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.os.postDelayed
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentSettingBinding
import com.app.plutope.dialogs.DeviceLockFullScreenDialog
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.constant.ABOUT_US_URL
import com.app.plutope.utils.extras.BiometricResult
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.extras.isDeviceSecure
import com.app.plutope.utils.extras.lock_request_code
import com.app.plutope.utils.extras.openDeviceLock
import com.app.plutope.utils.extras.security_setting_request_code
import com.app.plutope.utils.extras.setBioMetric
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Setting : BaseFragment<FragmentSettingBinding, SettingViewModel>() {

    private val settingViewModel: SettingViewModel by viewModels()

    private var biometricListener = object : BiometricResult {
        override fun success() {

            PreferenceHelper.getInstance().isBiometricAllow = true
            openPasscodeScreen()
        }

        override fun failure(errorCode: Int, errorMessage: String) {
            when (errorCode) {

                BiometricPrompt.ERROR_LOCKOUT -> (requireActivity() as BaseActivity).continueWithoutBiometric(
                    "Maximum number of attempts exceeds! Try again later",
                    useDevicePassword = true
                )

                BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_CANCELED -> (requireActivity() as BaseActivity).continueWithoutBiometric(
                    "Unlock with Face ID/ Touch ID or password",
                    true
                )

                else -> (requireActivity() as BaseActivity).continueWithoutBiometric(errorMessage)
            }
        }

        override fun successCustomPasscode() {
            openPasscodeScreen()
        }

    }

    override fun getViewModel(): SettingViewModel {
        return settingViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.settingViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_setting
    }

    override fun setupToolbarText(): String {
        return getString(R.string.settings)
    }

    override fun setupUI() {
        setOnClickListner()
        viewDataBinding?.txtCurrencySelected?.text = preferenceHelper.getSelectedCurrency()?.code

        val popupMenu = PopupMenu(requireContext(), viewDataBinding!!.cardLanguage)
        popupMenu.menuInflater.inflate(R.menu.language_menu, popupMenu.menu)

        viewDataBinding!!.cardLanguage.setOnClickListener {
//            popupMenu.show()
            showSortPopup()
        }

        viewDataBinding!!.cardEns.setOnClickListener {
            findNavController().safeNavigate(SettingDirections.actionSettingToAddENS())
        }

        viewDataBinding!!.cardHelpCenter.setOnClickListener {

        }

        viewDataBinding!!.txtLanguageSelected.text = preferenceHelper.currentLanguage

        clickOnPopupMenu(popupMenu)

    }

    private fun clickOnPopupMenu(popupMenu: PopupMenu) {
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.english -> {
                    viewDataBinding!!.txtLanguageSelected.text = getString(R.string.english)
                    preferenceHelper.currentLanguage = "English"
                    (activity as BaseActivity).changeLanguage("en", true)
                }

                R.id.thai -> {
                    viewDataBinding!!.txtLanguageSelected.text = getString(R.string.thai)
                    preferenceHelper.currentLanguage = "Thai"
                    (activity as BaseActivity).changeLanguage("th", true)
                }

                R.id.hindi -> {
                    viewDataBinding!!.txtLanguageSelected.text = getString(R.string.hindi)
                    preferenceHelper.currentLanguage = "Hindi"
                    (activity as BaseActivity).changeLanguage("hi", true)
                }
                R.id.arabic -> {
                    viewDataBinding!!.txtLanguageSelected.text = getString(R.string.arabic)
                    preferenceHelper.currentLanguage = "Arabic"
                    (activity as BaseActivity).changeLanguage("ar", true)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun showSortPopup() {

        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.language_list, null)

        // Wrap the existing content with a LinearLayout to apply margins
        val popupLayout = LinearLayout(context)
        popupLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupLayout.orientation = LinearLayout.VERTICAL
        val horizontalMargin = resources.getDimensionPixelSize(R.dimen._20mdp)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(horizontalMargin, 0, horizontalMargin, 0)
        popupView.layoutParams = layoutParams
        popupLayout.addView(popupView)

        val popupWindow = PopupWindow(
            popupLayout,  // Use the LinearLayout as the root layout
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isFocusable = true
        val anchorView = viewDataBinding!!.cardLanguage
        popupWindow.showAsDropDown(anchorView)

        val backgroundDim = ColorDrawable(Color.BLACK)
        backgroundDim.alpha = 150
        popupWindow.setBackgroundDrawable(backgroundDim)
        popupWindow.isOutsideTouchable = true

        val listView = popupView.findViewById<ListView>(R.id.list_view)

        listView.adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_list_item_1,
            arrayListOf("English", "Thai", "Hindi"/*, "Arabic"*/)
        )

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, p2, _ ->

                when (p2) {
                    0 -> {
                        viewDataBinding!!.txtLanguageSelected.text = "English"
                        preferenceHelper.currentLanguage = "English"
                        (activity as BaseActivity).changeLanguage("en", true)
                    }

                    1 -> {
                        viewDataBinding!!.txtLanguageSelected.text = "Thai"
                        preferenceHelper.currentLanguage = "Thai"
                        (activity as BaseActivity).changeLanguage("th", true)
                    }

                    2 -> {
                        viewDataBinding!!.txtLanguageSelected.text = "Hindi"
                        preferenceHelper.currentLanguage = "Hindi"
                        (activity as BaseActivity).changeLanguage("hi", true)
                    }

                    /* 3 -> {
                         viewDataBinding!!.txtLanguageSelected.text = "Arabic"
                         preferenceHelper.currentLanguage = "Arabic"
                         (activity as BaseActivity).changeLanguage("ar", true)

                     }*/
                }
                popupWindow.dismiss()
            }
    }


    private fun setOnClickListner() {
        viewDataBinding!!.cardWallets.setOnClickListener {
            findNavController().safeNavigate(SettingDirections.actionSettingToWallets())
        }

        viewDataBinding!!.cardSecurity.setOnClickListener {
            if (preferenceHelper.isAppLock) {
                if (!preferenceHelper.isLockModePassword)
                    setBioMetric(biometricListener)
                else {
                    // openDeviceLock()

                    // requireContext().openCustomDeviceLock()

                    DeviceLockFullScreenDialog.getInstance().show(requireContext(),
                        object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
                            override fun onSubmitClicked(selectedList: String) {
                                openPasscodeScreen()
                            }
                        })
                }

            } else {
                openPasscodeScreen()
            }

        }

        viewDataBinding?.cardCurrency?.setOnClickListener {
            findNavController().safeNavigate(SettingDirections.actionSettingToCurrency(true))
        }

        viewDataBinding?.cardAboutPlutope?.setOnClickListener {
            val intent = CustomTabsIntent.Builder()
                .build()
            intent.launchUrl(requireContext(), Uri.parse(ABOUT_US_URL))

        }

        viewDataBinding?.cardContacts?.setOnClickListener {
            findNavController().safeNavigate(
                SettingDirections.actionSettingToContactListFragment(
                    false
                )
            )
        }

        viewDataBinding?.cardWalletConnect?.setOnClickListener {
            findNavController().safeNavigate(SettingDirections.actionSettingToWalletConnect())
        }

    }

    override fun setupObserver() {

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            lock_request_code -> if (resultCode == AppCompatActivity.RESULT_OK && isDeviceSecure()) {
                //If screen lock authentication is success update text
                // showToast("Success passcode")


                (requireActivity() as BaseActivity).bioMetricDialog?.dismiss()
                Handler(Looper.getMainLooper()).postDelayed(3000) {
                    (requireActivity() as BaseActivity).openDefaultPass = false
                }
                openPasscodeScreen()


            } else {
                //If screen lock authentication is failed update text
                //"unlock failed".showToast(this)
                if (!isDeviceSecure())
                    (requireActivity() as BaseActivity).continueWithoutBiometric(
                        "Can not use app without device credentials",
                        true
                    )
                else (requireActivity() as BaseActivity).continueWithoutBiometric(
                    "Failed to authenticate user.",
                    true
                )
            }

            security_setting_request_code -> {
                //When user is enabled Security settings then we don't get any kind of RESULT_OK
                //So we need to check whether device has enabled screen lock or not
                if (resultCode == AppCompatActivity.RESULT_OK && isDeviceSecure()) {
                    //If screen lock enabled show toast and start intent to authenticate user
                    openDeviceLock()
                } else {
                    //If screen lock is not enabled just update text
                    (requireActivity() as BaseActivity).continueWithoutBiometric(
                        "Can not use app without device credentials",
                        true
                    )
                }


            }

            else -> {

            }


        }
    }

    fun openPasscodeScreen() {
        findNavController().safeNavigate(SettingDirections.actionSettingToSecurity())
    }
}