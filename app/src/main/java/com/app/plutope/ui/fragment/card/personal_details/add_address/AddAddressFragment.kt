package com.app.plutope.ui.fragment.card.personal_details.add_address

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentAddAddressBinding
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.setCardProgress
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddAddressFragment : BaseFragment<FragmentAddAddressBinding, AddAddressViewModel>() {
    private val addAddressViewModel: AddAddressViewModel by viewModels()

    override fun getViewModel(): AddAddressViewModel {
        return addAddressViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.addAddressViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_add_address
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setCardProgress(viewDataBinding!!.root, 1, (requireActivity() as BaseActivity))

        viewDataBinding!!.btnNext.setOnClickListener {
            findNavController().safeNavigate(AddAddressFragmentDirections.actionAddAddressFragmentToMoreDetails())

        }
    }

    override fun setupObserver() {}

}