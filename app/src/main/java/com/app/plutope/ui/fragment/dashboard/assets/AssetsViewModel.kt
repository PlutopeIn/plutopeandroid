package com.app.plutope.ui.fragment.dashboard.assets

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.DashboardRepo
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.ui.fragment.dashboard.GenerateTokenModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetsViewModel @Inject constructor(
    val dashboardRepo: DashboardRepo,
    private val tokenRepo: TokensRepo
) : BaseViewModel<CommonNavigator>() {


    //Get Assets
    private val _tagGetAssets =
        MutableStateFlow<NetworkState<GenerateTokenModel?>>(NetworkState.Empty())

    val getAssetsResponse: StateFlow<NetworkState<GenerateTokenModel?>>
        get() = _tagGetAssets

    fun executeGenerateToken() {
        viewModelScope.launch {
            _tagGetAssets.emit(NetworkState.Loading())
            _tagGetAssets.collectStateFlow(dashboardRepo.getGenerateToken())
        }
    }


}