package com.app.plutope.ui.fragment.dashboard.nfts

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.NFTRepo
import com.app.plutope.model.Info
import com.app.plutope.model.NFTListModel
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NFTsViewModel @Inject constructor(val nftRepo: NFTRepo) : BaseViewModel<CommonNavigator>() {

    //Get Nft list
    private val _tagGetNftList =
        MutableStateFlow<NetworkState<NFTListModel?>>(NetworkState.Empty())

    val getNFTListResponse: StateFlow<NetworkState<NFTListModel?>>
        get() = _tagGetNftList

    fun executeGetNFTList(url: String) {
        viewModelScope.launch {
            _tagGetNftList.emit(NetworkState.Loading())
            _tagGetNftList.collectStateFlow(nftRepo.getNFTListing(url))
        }
    }

}