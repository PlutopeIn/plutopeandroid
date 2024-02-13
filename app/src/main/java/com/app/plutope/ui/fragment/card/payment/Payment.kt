package com.app.plutope.ui.fragment.card.payment

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentPaymentBinding
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.card.setCardProgress
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Payment : BaseFragment<FragmentPaymentBinding, PaymentViewModel>() {
    private val paymentViewModel: PaymentViewModel by viewModels()
    override fun getViewModel(): PaymentViewModel {
        return paymentViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.paymentViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_payment
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
        setCardProgress(viewDataBinding!!.root, 4, (requireActivity() as BaseActivity))
        viewDataBinding!!.paymentTransactionAdapter = PaymentTransactionAdapter(arrayListOf())

        val list = arrayListOf<PaymentTransactionModel>()
        list.add(
            PaymentTransactionModel(
                coinName = "BTC Rate",
                coinRate = "1 BTC",
                coinConvertedValue = "28.60 USD"
            )
        )
        list.add(
            PaymentTransactionModel(
                coinName = "USD Rate",
                coinRate = "1 BTC",
                coinConvertedValue = "28.60 USD"
            )
        )
        paymentViewModel.setPaymentTransactionList(list)


    }

    override fun setupObserver() {

    }


}