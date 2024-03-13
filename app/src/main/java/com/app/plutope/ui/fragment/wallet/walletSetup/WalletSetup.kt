package com.app.plutope.ui.fragment.wallet.walletSetup

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentWalletSetupBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.constant.isImportWallet
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletSetup : BaseFragment<FragmentWalletSetupBinding, WalletSetupViewModel>() {
    private val walletSetupViewModel: WalletSetupViewModel by viewModels()
    override fun getViewModel(): WalletSetupViewModel {
        return walletSetupViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.walletSetupViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_wallet_setup
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setUpListener()
    }

    private fun setUpListener() {
        viewDataBinding?.btnCreateWallet?.buttonClickedWithEffect {

            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                isImportWallet = false
                if (preferenceHelper.menomonicWallet != "") {
                    if (!preferenceHelper.isAppLock) {
                        findNavController().safeNavigate(
                            WalletSetupDirections.actionWalletSetupToCreatePassword(
                                false
                            )
                        )
                    } else {
                        findNavController().safeNavigate(WalletSetupDirections.actionWalletSetupToPhraseBackupFragment())
                    }
                } else {
                    findNavController().safeNavigate(WalletSetupDirections.actionWalletSetupToLegal())
                }
            }
        }
        viewDataBinding?.btnReCreateRecovery?.buttonClickedWithEffect {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                isImportWallet = true
                if (preferenceHelper.menomonicWallet != "") {

                    if (!preferenceHelper.isAppLock) {
                        findNavController().safeNavigate(
                            WalletSetupDirections.actionWalletSetupToCreatePassword(
                                false
                            )
                        )
                    } else {
                        findNavController().safeNavigate(WalletSetupDirections.actionWalletSetupToRestoreWallet())
                    }
                } else {
                    findNavController().safeNavigate(WalletSetupDirections.actionWalletSetupToLegal())
                }

            }
        }
    }

    override fun setupObserver() {

    }


}