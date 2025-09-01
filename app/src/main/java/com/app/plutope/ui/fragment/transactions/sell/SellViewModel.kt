package com.app.plutope.ui.fragment.transactions.sell

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.ProviderDetailsRepo
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellViewModel @Inject constructor(private val providerDetailsRepo: ProviderDetailsRepo) :
    BaseViewModel<CommonNavigator>() {

    /**
     * Buy quote Single transaction
     * */

    private val _sellProviderListApi =
        MutableStateFlow<NetworkState<SellProviderModel?>>(NetworkState.Empty())

    val sellProviderResponse: StateFlow<NetworkState<SellProviderModel?>>
        get() = _sellProviderListApi

    fun getSellProviderList() {
        viewModelScope.launch {
            _sellProviderListApi.emit(NetworkState.Loading())
            _sellProviderListApi.collectStateFlow(
                providerDetailsRepo.getSellProviderList()
            )
        }
    }
}