package com.app.plutope.ui.fragment.recovery_wallet

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.os.postDelayed
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentRecoveryWalletBinding
import com.app.plutope.dialogs.ConfirmationAlertDialog
import com.app.plutope.dialogs.DeviceLockFullScreenDialog
import com.app.plutope.model.Wallet
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.phrase.recovery_phrase.VerifySecretPhraseViewModel
import com.app.plutope.utils.Securities
import com.app.plutope.utils.extras.BiometricResult
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.extras.isDeviceSecure
import com.app.plutope.utils.extras.lock_request_code
import com.app.plutope.utils.extras.openDeviceLock
import com.app.plutope.utils.extras.security_setting_request_code
import com.app.plutope.utils.extras.setBioMetric
import com.app.plutope.utils.extras.setSafeOnClickListener
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.showLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecoveryWalletFragment :
    BaseFragment<FragmentRecoveryWalletBinding, RecoveryWalletViewModel>() {
    private lateinit var walletList: MutableList<Wallets?>
    private val recoveryWalletViewModel: RecoveryWalletViewModel by viewModels()
    val args: RecoveryWalletFragmentArgs by navArgs()
    private val verifySecretPhraseViewModel: VerifySecretPhraseViewModel by viewModels()

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
            loge("Passcode", "here i am")
            openPasscodeScreen()
        }

    }

    override fun getViewModel(): RecoveryWalletViewModel {
        return recoveryWalletViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.recoverModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_recovery_wallet
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        // Customize soft keyboard behavior for this fragment
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        setDetails()
        setOnClickListner()
    }

    private fun setDetails() {
        viewDataBinding?.edtWalletName?.setText(args.walletModel.w_wallet_name)
        args.walletModel.apply {
            viewDataBinding?.txtDriveValue?.text =
                if (this.w_is_cloud_backup) "Active" else "Not Active"
            if (this.w_is_cloud_backup)
                viewDataBinding?.txtDriveValue?.setTextColor(
                    resources.getColor(
                        R.color.blue_00C6FB,
                        null
                    )
                )
            else
                viewDataBinding?.txtDriveValue?.setTextColor(
                    resources.getColor(
                        R.color.gray_767691,
                        null
                    )
                )

            viewDataBinding?.txtManualValue?.text =
                if (this.w_is_manual_backup) getString(R.string.active) else getString(R.string.not_active)
            if (this.w_is_manual_backup)
                viewDataBinding?.txtManualValue?.setTextColor(
                    resources.getColor(
                        R.color.header_blue,
                        null
                    )
                )
            else
                viewDataBinding?.txtManualValue?.setTextColor(
                    resources.getColor(
                        R.color.button_disable,
                        null
                    )
                )

        }

        viewDataBinding?.btnSave?.visibility = GONE

        verifySecretPhraseViewModel.getWalletsList()
    }

    private fun setOnClickListner() {
        val wordList = Securities.decrypt(args.walletModel.w_mnemonic).split(" ").toMutableList()
        viewDataBinding?.apply {

            cardDriveBackup.setSafeOnClickListener {
                if (!args.walletModel.w_is_cloud_backup) {
                    findNavController().navigate(
                        RecoveryWalletFragmentDirections.actionRecoveryWalletFragmentToNameYourBackup(
                            args.walletModel
                        )
                    )
                } else {
                    findNavController().navigate(
                        RecoveryWalletFragmentDirections.actionRecoveryWalletFragmentToYourRecoveryPhrase(
                            Securities.decrypt(args.walletModel.w_mnemonic).toString(),
                            wordList.toTypedArray(), args.walletModel, true
                        )
                    )
                }
            }
            cardManualBackup.setSafeOnClickListener {

                if (preferenceHelper.isAppLock) {
                    if (!preferenceHelper.isLockModePassword) {
                        setBioMetric(biometricListener)
                        DeviceLockFullScreenDialog.getInstance().show(requireContext(),
                            object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
                                override fun onSubmitClicked(selectedList: String) {
                                    openPasscodeScreen()
                                }
                            })

                    } else {
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

            edtWalletName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim()
                            .isNotEmpty() && args.walletModel.w_wallet_name != s.toString().trim()
                    ) {
                        viewDataBinding?.btnSave?.visibility = VISIBLE
                        viewDataBinding?.imgCancel?.visibility = VISIBLE
                    } else {
                        viewDataBinding?.btnSave?.visibility = GONE
                        viewDataBinding?.imgCancel?.visibility = GONE
                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            btnSave.setSafeOnClickListener {
                verifySecretPhraseViewModel.updateWalletBackup(
                    args.walletModel.w_is_cloud_backup,
                    args.walletModel.w_is_manual_backup,
                    args.walletModel.w_id,
                    edtWalletName.text.toString(),
                    args.walletModel.folderId,
                    args.walletModel.fileId
                )
            }

            btnDelete.setSafeOnClickListener {
                openConfirmationDialog()
            }
            imgCancel.setSafeOnClickListener {
                edtWalletName.setText("")
            }


        }
    }

    private fun openConfirmationDialog() {
        ConfirmationAlertDialog.getInstance().show(
            requireContext(),
            getString(R.string.are_you_sure_you_would_like_to_delete_this_wallet),
            getString(R.string.make_sure_you_have_backup_of_your_wallet),
            listener = object : ConfirmationAlertDialog.DialogOnClickBtnListner {
                override fun onDeleteClick() {
                    verifySecretPhraseViewModel.deleteWalletById(
                        args.walletModel.w_id,
                        walletList,
                        args.walletModel.w_isprimary
                    )
                }
            })
    }

    override fun setupObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.updateWalletBackupResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            hideLoader()
                            if (it.data != null && args.walletModel.w_isprimary == 1) {
                                Wallet
                                    .setWalletObjectFromInstance(it.data)
                                Wallet.refreshWallet()
                            }
                            viewDataBinding?.btnSave?.visibility = GONE

                            findNavController().popBackStack()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.walletsListResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data?.isNotEmpty() == true && it.data.size > 1) {
                                walletList = it.data as MutableList<Wallets?>
                                viewDataBinding?.btnDelete?.visibility = VISIBLE
                            } else {
                                viewDataBinding?.btnDelete?.visibility = GONE
                            }

                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {}

                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                verifySecretPhraseViewModel.walletsDeleteResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data != null) {
                                findNavController().navigateUp()
                            }
                            hideLoader()
                        }

                        is NetworkState.Loading -> {
                            requireContext().showLoader()
                        }

                        is NetworkState.Error -> {
                            hideLoader()
                        }

                        is NetworkState.SessionOut -> {}
                        else -> {
                            hideLoader()
                        }
                    }
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            lock_request_code -> if (resultCode == AppCompatActivity.RESULT_OK && isDeviceSecure()) {
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


        }
    }

    fun openPasscodeScreen() {
        val wordList =
            Securities.decrypt(args.walletModel.w_mnemonic).trim().split(" ").toMutableList()

        if (!args.walletModel.w_is_manual_backup) {
            findNavController().navigate(
                RecoveryWalletFragmentDirections.actionRecoveryWalletFragmentToBackUpWalletCheck(
                    args.walletModel
                )
            )
        } else {
            findNavController().navigate(
                RecoveryWalletFragmentDirections.actionRecoveryWalletFragmentToYourRecoveryPhrase(
                    Securities.decrypt(args.walletModel.w_mnemonic.toString()),
                    wordList.toTypedArray(), args.walletModel, false
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Customize soft keyboard behavior for this fragment
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

    }
}