package com.app.plutope.data.repository

import com.app.plutope.data.database.CurrencyDao
import com.app.plutope.model.CurrencyModel
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.generateRequestBody
import com.app.plutope.utils.network.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.net.UnknownHostException
import javax.inject.Inject

class CurrencyRepo @Inject constructor(
    private val apiHelper: ApiHelper,
    private val currencyDao: CurrencyDao
) {

    suspend fun getCurrencyList(
        url: String
    ): NetworkState<MutableList<CurrencyModel>?> {
        return try {
            val response = apiHelper.executeGetCurrencyApi(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.currency != null) {
                    val currencyList = mutableListOf<CurrencyModel>()
                    val currencyRespList = result.currency
                    repeat(result.currency.size) { it ->
                        currencyList.add(
                            CurrencyModel(
                                name = currencyRespList[it].name,
                                symbol = currencyRespList[it].symbol,
                                code = currencyRespList[it].code,
                                id = currencyRespList[it].id
                            )
                        )
                    }
                    NetworkState.Success("", currencyList)
                } else {
                    NetworkState.Error(result?.status?.error_message.toString())
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

    suspend fun executeUpdateCardCurrencyApi(
        currency: String
    ): NetworkState<MutableList<CurrencyModel>?> {
        return try {

            val params = mapOf(
                "primaryCurrency" to currency,
            )

            val response = apiHelper.executeUpdateCardCurrencyApi(
                body = generateRequestBody(params)
            )


            //  val response = apiHelper.executeUpdateCardCurrencyApi(currency)


            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", mutableListOf())
                } else {
                    NetworkState.Error(response.message())
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


    suspend fun insertCurrencyList(
        currencyList: MutableList<CurrencyModel>
    ): NetworkState<CurrencyModel?> {
        return try {
            //currencyList.filter { it?.code=="INR" }.forEach { it?.isSelected=true }
            currencyDao.insertAllCurrency(currencyList)
            val selectedCurrency = currencyDao.getSelectedCurrency().single()
            NetworkState.Success("", selectedCurrency)
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }
        }

    }


    fun getAllTableCurrencyList(): List<CurrencyModel> {
        return currencyDao.getAllCurrency()
    }

    suspend fun getAllCurrencyFromTable(): Flow<NetworkState<List<CurrencyModel?>>> {
        return flow {
            val localData: MutableList<CurrencyModel> = mutableListOf()
            localData.addAll(currencyDao.getAllCurrencyList().first())

            if (localData.isNotEmpty()) {
                emit(NetworkState.Success("", localData))
            } else {
                emit(NetworkState.Error("Failed to load"))
            }
        }
    }
}