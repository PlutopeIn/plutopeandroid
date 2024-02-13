package com.app.plutope.data.repository

import com.app.plutope.model.CoinDetailModel
import com.app.plutope.model.CoinGeckoMarketChartResponse
import com.app.plutope.model.CoinGeckoMarketsResponse
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import java.net.UnknownHostException
import javax.inject.Inject

class GraphDetailRepo @Inject constructor(private val apiHelper: ApiHelper) {
    suspend fun executeCoinGeckoMarketChart(
        url: String
    ): NetworkState<CoinGeckoMarketChartResponse?> {
        return try {
            val response = apiHelper.executeCoinGeckoMarketChartApi(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }

    }


    suspend fun executeCoinGeckoMarketsList(
        url: String
    ): NetworkState<MutableList<CoinGeckoMarketsResponse>?> {
        return try {
            val response = apiHelper.executeCoinGeckoMarketsApi(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error(response.message().toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }

    }

    suspend fun executeCoinGeckoCoinDetail(
        url: String
    ): NetworkState<CoinDetailModel?> {
        return try {
            val response = apiHelper.executeCoinDetailApi(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }

    }


}