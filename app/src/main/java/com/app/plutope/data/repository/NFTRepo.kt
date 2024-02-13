package com.app.plutope.data.repository

import com.app.plutope.model.NFTListModel
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.Event
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import java.net.UnknownHostException
import javax.inject.Inject

class NFTRepo @Inject constructor(private val apiHelper: ApiHelper) {

    suspend fun getNFTListing(
        url:String
    ): NetworkState<NFTListModel?> {
        return try {
            val response = apiHelper.executeNftListApi(url)
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