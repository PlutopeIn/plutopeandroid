package com.app.plutope.data.repository

import com.app.plutope.model.Info
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.ui.fragment.dashboard.GenerateTokenModel
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import java.net.UnknownHostException
import javax.inject.Inject

class DashboardRepo @Inject constructor(private val apiHelper: ApiHelper) {

    suspend fun getCoinsAssets(
        symbol: String
    ): NetworkState<Info?> {
        return try {
            val response = apiHelper.getCoinsAssets(symbol)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.data != null) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }
        }

    }

    suspend fun getGenerateToken(): NetworkState<GenerateTokenModel?> {
        return try {
            val url = BASE_URL_PLUTO_PE + "get-generate-token"
            val response = apiHelper.getGenerateToken(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }
        }

    }


}