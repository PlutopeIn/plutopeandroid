package com.app.plutope.ui.fragment.wallet.back_up_wallet

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentBackUpWalletCheckBinding
import com.app.plutope.model.Wallets
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.getMnemonics
import com.app.plutope.utils.getWordListFromWordCharArray
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BackUpWalletCheck :
    BaseFragment<FragmentBackUpWalletCheckBinding, BackUpWalletCheckViewModel>() {
    val args:BackUpWalletCheckArgs by navArgs()
    private val backUpWalletCheckViewModel: BackUpWalletCheckViewModel by viewModels()
    override fun getViewModel(): BackUpWalletCheckViewModel {
        return backUpWalletCheckViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.backUpWalletCheckViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_back_up_wallet_check
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setListener()

        val mnemonicsWords = getMnemonics()
        val wordList = getWordListFromWordCharArray(mnemonicsWords)

        val words = wordList.joinToString(separator = " ")

        viewDataBinding!!.btnContinue.buttonClickedWithEffect {

            (activity as BaseActivity).askNotificationPermission()

            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (args.walletModel.w_wallet_name != "") {
                    val wordList = args.walletModel.w_mnemonic?.split(" ")?.toMutableList()
                    findNavController().safeNavigate(
                        BackUpWalletCheckDirections.actionBackUpWalletCheckToYourRecoveryPhrase(
                            args.walletModel.w_mnemonic.toString(),
                            wordList?.toTypedArray()!!, args.walletModel, false
                        )
                    )
                }else{
                    findNavController().safeNavigate(
                        BackUpWalletCheckDirections.actionBackUpWalletCheckToYourRecoveryPhrase(
                            words,
                            wordList.toTypedArray(), Wallets(),false
                        )
                    )
                }
            }
        }

    }

    private fun setListener() {
        viewDataBinding?.imgBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding?.checkboxLoosFund?.setOnCheckedChangeListener { _, _ ->
            enableButton()
        }

        viewDataBinding?.checkboxExploreShare?.setOnCheckedChangeListener { _, _ ->
            enableButton()
        }

        viewDataBinding?.checkboxTrustWalletSupport?.setOnCheckedChangeListener { _, _ ->
            enableButton()
        }
    }

    private fun enableButton() {
        if (viewDataBinding!!.checkboxLoosFund.isChecked && viewDataBinding!!.checkboxExploreShare.isChecked && viewDataBinding!!.checkboxTrustWalletSupport.isChecked) {
           /* viewDataBinding!!.btnContinue.apply {
                isEnabled = true
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.bg_white, null))
            }*/

            viewDataBinding?.btnContinue?.enableDisableButton(true)

        } else {

            viewDataBinding?.btnContinue?.enableDisableButton(false)

           /* viewDataBinding!!.btnContinue.apply {
                isEnabled = false
                background =
                    ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
                setTextColor(ResourcesCompat.getColor(resources, R.color.green_02303B, null))

            }*/

        }

    }

    override fun setupObserver() {

    }


}