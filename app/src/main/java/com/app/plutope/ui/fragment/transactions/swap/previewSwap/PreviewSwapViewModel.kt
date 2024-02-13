package com.app.plutope.ui.fragment.transactions.swap.previewSwap

import androidx.lifecycle.MutableLiveData
import com.app.plutope.data.repository.TransactionHistoryRepo
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreviewSwapViewModel @Inject constructor(val transactionHistoryRepo: TransactionHistoryRepo) : BaseViewModel<CommonNavigator>() {


    val gasFee = MutableLiveData(0.toBigInteger())



}