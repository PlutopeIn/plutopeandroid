package com.app.plutope.ui.fragment.wallet.legal

import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentLegalBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Legal : BaseFragment<FragmentLegalBinding, LegalViewModel>() {

    private val legalViewModel: LegalViewModel by viewModels()
    override fun getViewModel(): LegalViewModel {
        return legalViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.legalViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_legal
    }

    override fun setupToolbarText(): String {
        return "Legal"
    }

    override fun setupUI() {
        setupListener()
    }

    private fun setupListener() {

        viewDataBinding!!.layoutPrivacyPolicy.setOnClickListener {
            findNavController().safeNavigate(
                LegalDirections.actionLegalToWebViewToolbar(
                    url = "https://www.plutope.io/privacy-policy",
                    title = "Privacy Policy"
                )
            )
        }

        viewDataBinding!!.layoutTermsOfService.setOnClickListener {
            findNavController().safeNavigate(
                LegalDirections.actionLegalToWebViewToolbar(
                    url = "https://plutope.io/terms-conditions.html",
                    title = "Terms Of Service"
                )
            )

        }

        viewDataBinding?.checkbox?.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                viewDataBinding!!.btnContinue.apply {
                    isEnabled = true
                    background =
                        ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                }
            } else {
                viewDataBinding!!.btnContinue.apply {
                    isEnabled = false
                    background =
                        ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.green_02303B, null))

                }

            }
        }

        viewDataBinding?.txtCheckBoxInfo?.setOnClickListener {
            viewDataBinding!!.checkbox.performClick()
        }

        viewDataBinding?.btnContinue?.buttonClickedWithEffect {
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {

                if (preferenceHelper.menomonicWallet != "") {
                    findNavController().safeNavigate(LegalDirections.actionLegalToPhraseBackupFragment())
                } else {
                    findNavController().safeNavigate(LegalDirections.actionLegalToPasscode(false))
                }
                // findNavController().safeNavigate(LegalDirections.actionLegalToPhraseBackupFragment())
            }
        }

    }

    override fun setupObserver() {

    }


}