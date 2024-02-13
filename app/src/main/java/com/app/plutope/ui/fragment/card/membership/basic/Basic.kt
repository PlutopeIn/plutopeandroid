package com.app.plutope.ui.fragment.card.membership.basic

import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentBasicBinding
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.setCardProgress
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Basic : BaseFragment<FragmentBasicBinding, BasicViewModel>() {
    private val basicViewModel: BasicViewModel by viewModels()
    override fun getViewModel(): BasicViewModel {
        return basicViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.basicViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_basic
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setCardProgress(viewDataBinding!!.root, 2, (requireActivity() as BaseActivity))
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewDataBinding!!.txtBasic.isSelected = true

        viewDataBinding!!.txtBasic.setOnClickListener {
            viewDataBinding!!.txtBasic.isSelected = true
            viewDataBinding!!.txtProfessional.isSelected = false
            viewDataBinding!!.txtBusiness.isSelected = false
            viewDataBinding!!.txtPlatinumElite.isSelected = false

            cardBasic()


        }

        viewDataBinding!!.txtProfessional.setOnClickListener {
            viewDataBinding!!.txtBasic.isSelected = false
            viewDataBinding!!.txtProfessional.isSelected = true
            viewDataBinding!!.txtBusiness.isSelected = false
            viewDataBinding!!.txtPlatinumElite.isSelected = false

            cardProfessional()

        }

        viewDataBinding!!.txtBusiness.setOnClickListener {
            viewDataBinding!!.txtBasic.isSelected = false
            viewDataBinding!!.txtProfessional.isSelected = false
            viewDataBinding!!.txtBusiness.isSelected = true
            viewDataBinding!!.txtPlatinumElite.isSelected = false

            viewDataBinding!!.layoutShadow.background =
                ResourcesCompat.getDrawable(resources, R.drawable.img_card_professional, null)

        }
        viewDataBinding!!.txtPlatinumElite.setOnClickListener {
            viewDataBinding!!.txtBasic.isSelected = false
            viewDataBinding!!.txtProfessional.isSelected = false
            viewDataBinding!!.txtBusiness.isSelected = false
            viewDataBinding!!.txtPlatinumElite.isSelected = true

            viewDataBinding!!.layoutShadow.background =
                ResourcesCompat.getDrawable(resources, R.drawable.img_card_professional, null)

        }


        viewDataBinding!!.btnGetCard.setOnClickListener {
            findNavController().safeNavigate(BasicDirections.actionBasicToMemberShip2Fragment())
        }

    }


    private fun cardBasic() {
        viewDataBinding!!.txtTitleMembership.text = getString(R.string.basic)
        viewDataBinding!!.layoutShadow.background =
            ResourcesCompat.getDrawable(resources, R.drawable.img_card_basic, null)
        viewDataBinding!!.txtMembershipPrice.text = "$9"
        viewDataBinding!!.txtDailyCardTopUpPayment.text = "Up to \$15,000 per day"
        viewDataBinding!!.btnGetCard.text = getString(R.string.get_basic_card)
    }

    private fun cardProfessional() {
        viewDataBinding!!.txtTitleMembership.text = getString(R.string.professional)
        viewDataBinding!!.layoutShadow.background =
            ResourcesCompat.getDrawable(resources, R.drawable.img_card_professional, null)
        viewDataBinding!!.txtMembershipPrice.text = "$10"
        viewDataBinding!!.txtDailyCardTopUpPayment.text = "Up to \$15,000 per day"
        viewDataBinding!!.btnGetCard.text = getString(R.string.get_professional_card)
    }

    override fun setupObserver() {

    }


}