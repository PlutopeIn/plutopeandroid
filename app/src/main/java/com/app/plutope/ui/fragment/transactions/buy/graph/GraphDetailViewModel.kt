package com.app.plutope.ui.fragment.transactions.buy.graph

import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.GraphDetailRepo
import com.app.plutope.model.CoinDetailModel
import com.app.plutope.model.CoinGeckoMarketChartResponse
import com.app.plutope.model.CoinGeckoMarketsResponse
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GraphDetailViewModel @Inject constructor(private val graphGraphDetailRepo: GraphDetailRepo) : BaseViewModel<CommonNavigator>() {
    //Get market chart
    private val _tagGetGraphMarket =
        MutableStateFlow<NetworkState<CoinGeckoMarketChartResponse?>>(NetworkState.Empty())

    val getGetGraphMarketResponse: StateFlow<NetworkState<CoinGeckoMarketChartResponse?>>
        get() = _tagGetGraphMarket

    fun executeGetGraphMarketResponse(url:String) {
        viewModelScope.launch {
            _tagGetGraphMarket.emit(NetworkState.Loading())
            _tagGetGraphMarket.collectStateFlow(graphGraphDetailRepo.executeCoinGeckoMarketChart(url))
        }
    }


    //Get markets
    private val _tagGetMarket =
        MutableStateFlow<NetworkState<MutableList<CoinGeckoMarketsResponse>?>>(NetworkState.Empty())

    val getGetMarketResponse: StateFlow<NetworkState<MutableList<CoinGeckoMarketsResponse>?>>
        get() = _tagGetMarket

    fun executeGetMarketResponse(url:String) {
        viewModelScope.launch {
            _tagGetMarket.emit(NetworkState.Loading())
            _tagGetMarket.collectStateFlow(graphGraphDetailRepo.executeCoinGeckoMarketsList(url))
        }
    }

    //Get coinDetail
    private val _tagGetCoinDetail =
        MutableStateFlow<NetworkState<CoinDetailModel?>>(NetworkState.Empty())

    val getGetCoinDetailResponse: StateFlow<NetworkState<CoinDetailModel?>>
        get() = _tagGetCoinDetail

    fun executeGetCoinDetailResponse(url:String) {
        viewModelScope.launch {
            _tagGetCoinDetail.emit(NetworkState.Loading())
            _tagGetCoinDetail.collectStateFlow(graphGraphDetailRepo.executeCoinGeckoCoinDetail(url))
        }
    }
}