package com.app.plutope.ui.fragment.ens

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.ENSRepo
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddENSViewModel @Inject constructor(private val ensRepo: ENSRepo) :
    BaseViewModel<CommonNavigator>() {

    //get Ens list
    private val _getEnsListResponse =
        MutableStateFlow<NetworkState<ENSListModel?>>(NetworkState.Empty())
    val ensListResponse: StateFlow<NetworkState<ENSListModel?>>
        get() = _getEnsListResponse

    fun getEnsListCall(domainSearchModel: DomainSearchModel) {
        viewModelScope.launch {
            _getEnsListResponse.emit(NetworkState.Loading())
            _getEnsListResponse.collectStateFlow(ensRepo.getDomainSearchList(domainSearchModel))
        }
    }


}