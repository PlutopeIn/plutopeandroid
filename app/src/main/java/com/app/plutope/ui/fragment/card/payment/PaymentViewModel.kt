package com.app.plutope.ui.fragment.card.payment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor() : BaseViewModel<CommonNavigator>() {

    private var _paymentTransactionList = MutableLiveData<List<PaymentTransactionModel>>()

    val paymentTransactionList: LiveData<List<PaymentTransactionModel>>
        get() = _paymentTransactionList

    fun setPaymentTransactionList(list: MutableList<PaymentTransactionModel>) {
        _paymentTransactionList.value = list
    }


}