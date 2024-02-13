package com.app.plutope.ui.fragment.card.membership_2

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentMemberShip2Binding
import com.app.plutope.dialogs.DialogCountryList
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.setCardProgress
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.constant.typeCurrencyList
import com.app.plutope.utils.loadBannerImage
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MemberShip2Fragment : BaseFragment<FragmentMemberShip2Binding, MemberShip2ViewModel>(),
    CommonNavigator {

    private val memberShipViewModel: MemberShip2ViewModel by viewModels()

    override fun getViewModel(): MemberShip2ViewModel {
        memberShipViewModel.setNavigator(this)
        return memberShipViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.memberShipViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_member_ship2
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        setCardProgress(viewDataBinding!!.root, 2, (requireActivity() as BaseActivity))
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewDataBinding!!.btnContinue.setOnClickListener {
            findNavController().safeNavigate(MemberShip2FragmentDirections.actionMemberShip2FragmentToCardDesign())
        }

        viewDataBinding!!.layoutCardCurrency.setOnClickListener {
            DialogCountryList.getInstance()
                ?.show(requireContext(), arrayListOf(), typeCurrencyList) {
                    viewDataBinding!!.txtCurrency.text = it.currencyCode
                    loadBannerImage(viewDataBinding?.imgCountryFlag!!, it.image)
                }
        }


    }

    override fun setupObserver() {

    }
}