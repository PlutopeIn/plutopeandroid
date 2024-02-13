package com.app.plutope.ui.fragment.card.personal_details.more_details

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentMoreDetailsBinding
import com.app.plutope.dialogs.DialogCountryList
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.setCardProgress
import com.app.plutope.utils.attachPasswordToggle
import com.app.plutope.utils.constant.typeCountryCodeList
import com.app.plutope.utils.loadBannerImage
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoreDetails : BaseFragment<FragmentMoreDetailsBinding, MoreDetailsViewModel>() {

    private val moreDetailsViewModel: MoreDetailsViewModel by viewModels()
    override fun getViewModel(): MoreDetailsViewModel {
        return moreDetailsViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.moreDetailsViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_more_details
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setCardProgress(viewDataBinding!!.root, 1, (requireActivity() as BaseActivity))

        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewDataBinding!!.edtPassword.attachPasswordToggle(viewDataBinding!!.imgHideShow)
        viewDataBinding!!.edtConfirmPassword.attachPasswordToggle(viewDataBinding!!.imgConfirmPasswordHideShow)
        viewDataBinding?.txtCheckBoxInfo?.setOnClickListener {
            viewDataBinding!!.checkbox.performClick()
        }

        viewDataBinding!!.btnContinue.setOnClickListener {
            findNavController().safeNavigate(MoreDetailsDirections.actionMoreDetailsToBasic())
        }

        viewDataBinding!!.layoutCountryCode.setOnClickListener {
            DialogCountryList.getInstance()
                ?.show(requireContext(), arrayListOf(), typeCountryCodeList) {
                    viewDataBinding!!.txtCountryCode.text = it.code
                    loadBannerImage(viewDataBinding?.imgCountryFlag!!, it.image)
                }
        }


    }

    override fun setupObserver() {

    }


}