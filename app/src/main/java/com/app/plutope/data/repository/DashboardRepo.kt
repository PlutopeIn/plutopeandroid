package com.app.plutope.data.repository

import com.app.plutope.model.CryptoData
import com.app.plutope.model.Info
import com.app.plutope.model.Quote
import com.app.plutope.model.USD
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import org.json.JSONObject
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
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }

    }

    suspend fun getAssets(
        symbol: String
    ): NetworkState<Info?> {
        return try {
            val response = apiHelper.getAssets("7d00fe84-d1ea-47da-b2f6-53287366a15c",symbol)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    val info: Info
                    val mutableMap: MutableMap<String, CryptoData> = mutableMapOf()
                    val jsonObj = JSONObject(result?.string())
                    val dataList = jsonObj.getJSONObject("data")

                    val key = dataList.keys()
                    key.forEach { it->
                        val obj = dataList.getJSONObject(it)
                        val quote = obj.getJSONObject("quote")
                        val quoteKeys = quote.keys()
                        var keyQuoteObj :USD? =null
                        quoteKeys.forEach {
                             val currencyObj= quote.getJSONObject(it)
                             keyQuoteObj=  USD(currencyObj.getString("price"),currencyObj.getString("percent_change_24h"))
                        }
                        val cryptoObj = CryptoData(obj.getString("id") ,obj.getString("name"),obj.getString("symbol"),"", quote = Quote(keyQuoteObj) )
                        mutableMap[it] = cryptoObj

                    }
                    info = Info(mutableMap)
                    NetworkState.Success("", info)
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