package com.app.plutope.ui.fragment.currency

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.CurrencyRepo
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Info
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallets
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
class CurrencyViewModel @Inject constructor(private val currencyRepo: CurrencyRepo) : BaseViewModel<CommonNavigator>() {

    private val _tagGetCurrency =
        MutableStateFlow<NetworkState<MutableList<CurrencyModel>?>>(NetworkState.Empty())

    val getCurrencyResponse: StateFlow<NetworkState<MutableList<CurrencyModel>?>>
        get() = _tagGetCurrency

    @OptIn(DelicateCoroutinesApi::class)
    fun executeGetCurrency(url: String) {
        viewModelScope.launch {
            _tagGetCurrency.emit(NetworkState.Loading())
            _tagGetCurrency.collectStateFlow(currencyRepo.getCurrencyList(url))
        }

    }


    private val _insertCurrencyResponse =
        MutableStateFlow<NetworkState<CurrencyModel?>>(NetworkState.Empty())
    val currencyResponse: MutableStateFlow<NetworkState<CurrencyModel?>>
        get() = _insertCurrencyResponse

    fun insertCurrency(currencyList: MutableList<CurrencyModel>) {

        viewModelScope.launch {
            _insertCurrencyResponse.emit(NetworkState.Loading())
            _insertCurrencyResponse.collectStateFlow(currencyRepo.insertCurrencyList(currencyList.toMutableList()))
        }
    }

    fun getTableCurrencyList(): List<CurrencyModel> {
        return currencyRepo.getAllTableCurrencyList()
    }


    //get Currency From Table
    private val _getCurrencyListTableResponse =
        MutableStateFlow<NetworkState<List<CurrencyModel?>>>(NetworkState.Empty())
    val currencyListResponse: MutableStateFlow<NetworkState<List<CurrencyModel?>>>
        get() = _getCurrencyListTableResponse

    fun getCurrencyFromTable() {
        viewModelScope.launch {
            _getCurrencyListTableResponse.emit(NetworkState.Loading())
            currencyRepo.getAllCurrencyFromTable().collect { networkState ->
                _getCurrencyListTableResponse.value = networkState
            }
        }
    }

}