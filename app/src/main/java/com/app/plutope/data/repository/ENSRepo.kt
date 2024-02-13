package com.app.plutope.data.repository

import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.ui.fragment.ens.DomainSearchModel
import com.app.plutope.ui.fragment.ens.ENSListModel
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseBadRequest
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import java.net.UnknownHostException
import javax.inject.Inject

class ENSRepo @Inject constructor(private val apiHelper: ApiHelper) {

    suspend fun getDomainSearchList(
        domainSearchModel: DomainSearchModel
    ): NetworkState<ENSListModel?> {
        return try {
            val response = apiHelper.domainCheck(domainSearchModel)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else if (response.code() == responseBadRequest) {
                NetworkState.Error("Domain ${domainSearchModel.domainName} is not available")
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("Error")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }

}