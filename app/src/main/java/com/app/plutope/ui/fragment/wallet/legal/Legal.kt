package com.app.plutope.ui.fragment.wallet.legal

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentLegalBinding
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.enableDisableButton
import com.app.plutope.utils.extras.buttonClickedWithEffect
import com.app.plutope.utils.loge
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
        return ""
    }

    override fun setupUI() {
        setupListener()
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }


        val screenWidthInPixels = resources.displayMetrics.widthPixels
        val screenHeightInPixels = resources.displayMetrics.heightPixels
        val screenDensity = resources.displayMetrics.density



        loge("viewport","width=>$screenWidthInPixels   :: height=> $screenHeightInPixels  :: density => $screenDensity")

       // val screenWidthInDp = convertPixelsToDp(this, screenWidthInPixels.toFloat())
       // val screenHeightInDp = convertPixelsToDp(this, screenHeightInPixels.toFloat())


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
                   // url = "https://plutope.io/terms-conditions.html",
                    url = "https://www.plutope.io/terms-and-conditions",
                    title = "Terms Of Service"
                )
            )

        }

        viewDataBinding?.checkbox?.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
               /* viewDataBinding!!.btnContinue.apply {
                    isEnabled = true
                    background =
                        ResourcesCompat.getDrawable(resources, R.drawable.button_gradient_26, null)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.bg_white, null))
                }*/

                viewDataBinding!!.btnContinue.enableDisableButton(true)

            } else {

                viewDataBinding!!.btnContinue.enableDisableButton(false)

              /*  viewDataBinding!!.btnContinue.apply {
                    isEnabled = false
                    background =
                        ResourcesCompat.getDrawable(resources, R.drawable.button_disable, null)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.bg_white, null))

                }*/

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