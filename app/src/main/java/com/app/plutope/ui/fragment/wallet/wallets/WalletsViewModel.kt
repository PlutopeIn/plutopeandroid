package com.app.plutope.ui.fragment.wallet.wallets

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.WalletRepo
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import com.bumptech.glide.load.engine.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletsViewModel @Inject constructor(val walletRepo: WalletRepo) :
    BaseViewModel<CommonNavigator>() {
    private val _getWalletsResponse =
        MutableStateFlow<NetworkState<List<com.app.plutope.model.Wallets?>>>(NetworkState.Empty())
    val walletsListResponse: MutableStateFlow<NetworkState<List<com.app.plutope.model.Wallets?>>>
        get() = _getWalletsResponse

    fun getWalletsList() {
        viewModelScope.launch {
            _getWalletsResponse.emit(NetworkState.Loading())
            walletRepo.getAllWalletList().collect { networkState ->
                _getWalletsResponse.value = networkState
            }
        }
    }
}