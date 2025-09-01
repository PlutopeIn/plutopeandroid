package com.app.plutope.ui.fragment.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.DashboardRepo
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(private val dashboardRepo: DashboardRepo) :
    BaseViewModel<CommonNavigator>() {


    var getBalance = MutableLiveData<String>("0.0")

    //Get Assets
    private val _tagGetGenerateToken =
        MutableStateFlow<NetworkState<GenerateTokenModel?>>(NetworkState.Empty())

    val getGenerateTokenResponse: StateFlow<NetworkState<GenerateTokenModel?>>
        get() = _tagGetGenerateToken

    fun executeGetGenerateToken() {
        viewModelScope.launch {
            _tagGetGenerateToken.emit(NetworkState.Loading())
            _tagGetGenerateToken.collectStateFlow(dashboardRepo.getGenerateToken())
        }

    }


    private val _storedAssetsList = MutableLiveData(mutableListOf<Tokens>())

    val storedList: MutableLiveData<MutableList<Tokens>>
        get() = _storedAssetsList

    fun addAllAssetsList(tokenList: MutableList<Tokens>) {
        viewModelScope.launch {
            _storedAssetsList.postValue(tokenList)
        }
    }

    private val _tagIsBalanceHidden = MutableStateFlow(true)
    val observeIsBalanceHidden: StateFlow<Boolean> = _tagIsBalanceHidden.asStateFlow()
    fun updateIsBalanceHidden(hide: Boolean) {
        viewModelScope.launch {
            _tagIsBalanceHidden.emit(hide)
        }
    }


    private val _clickEvent = MutableLiveData<Boolean>()
    val clickEvent: LiveData<Boolean> get() = _clickEvent

    fun onClick() {
        _clickEvent.value = true
    }

    fun resetClickEvent() {
        _clickEvent.value = false
    }

}