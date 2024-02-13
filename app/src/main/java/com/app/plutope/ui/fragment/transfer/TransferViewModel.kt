package com.app.plutope.ui.fragment.transfer

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.TransactionHistoryRepo
import com.app.plutope.model.Tokens
import com.app.plutope.model.TransactionDetail
import com.app.plutope.model.TransactionHistoryResponse
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(val transactionHistoryRepo: TransactionHistoryRepo) : BaseViewModel<CommonNavigator>() {

    //transaction History detail
    private val _tagGetTransactionOkLinkHistoryDetail =
        MutableStateFlow<NetworkState<List<TransactionDetail>?>>(NetworkState.Empty())

    val transactionHistoryOkLinkResponse: StateFlow<NetworkState<List<TransactionDetail>?>>
        get() = _tagGetTransactionOkLinkHistoryDetail

    fun executeGetTransactionHistoryDetailOkLink(url: String) {
        viewModelScope.launch {
            _tagGetTransactionOkLinkHistoryDetail.emit(NetworkState.Loading())
            _tagGetTransactionOkLinkHistoryDetail.collectStateFlow(transactionHistoryRepo.getTransactionHistoryDetail(url))
        }
    }



}