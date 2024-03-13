package com.app.plutope.ui.fragment.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.DashboardRepo
import com.app.plutope.model.Info
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private val dashboardRepo: DashboardRepo) :
    BaseViewModel<CommonNavigator>() {


    var getBalance = MutableLiveData<String>("0.0")

    //Get Assets
    private val _tagGetAssets =
        MutableStateFlow<NetworkState<Info?>>(NetworkState.Empty())

    val getAssetsResponse: StateFlow<NetworkState<Info?>>
        get() = _tagGetAssets

    @OptIn(DelicateCoroutinesApi::class)
    fun executeGetAssets(symbol: String) {
        viewModelScope.launch {
            _tagGetAssets.emit(NetworkState.Loading())
            _tagGetAssets.collectStateFlow(dashboardRepo.getAssets(symbol))
        }

    }
}