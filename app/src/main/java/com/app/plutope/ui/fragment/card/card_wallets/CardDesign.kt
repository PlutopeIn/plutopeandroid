package com.app.plutope.ui.fragment.card.card_wallets

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentCardDesignBinding
import com.app.plutope.model.CurrencyModel
import com.app.plutope.ui.adapter.CurrencyListAdapter
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.setCardProgress
import com.app.plutope.utils.safeNavigate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardDesign : BaseFragment<FragmentCardDesignBinding, CardDesignViewModel>(),
    CurrencyListAdapter.OnActionClickListener {

    private val cardDesignViewModel: CardDesignViewModel by viewModels()
    override fun getViewModel(): CardDesignViewModel {
        return cardDesignViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.cardDesignViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_card_design
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        setCardProgress(viewDataBinding!!.root, 3, (requireActivity() as BaseActivity))
        viewDataBinding?.apply {
            currencyListAdapter = CurrencyListAdapter(arrayListOf(), this@CardDesign)
            cryptoListAdapter = CurrencyListAdapter(arrayListOf(), this@CardDesign)
            btnContinue.setOnClickListener {
                findNavController().safeNavigate(CardDesignDirections.actionCardDesignToPayment())
            }

        }

        val list = mutableListOf<CurrencyModel>()
        repeat(3) {
            list.add(CurrencyModel("US Dollar", "IN"))
        }
        cardDesignViewModel.setCurrencyList(list)

        val cryptoList = mutableListOf<CurrencyModel>()
        cryptoList.add(CurrencyModel("Bitcoin BTC", "IN"))
        cardDesignViewModel.setCryptoListList(cryptoList)


    }

    override fun setupObserver() {

    }

    override fun onEditCLick(model: CurrencyModel) {
        //this is implemented only for demo
    }

    override fun onDeleteClick(model: CurrencyModel) {
        //this is implemented only for demo
    }

}