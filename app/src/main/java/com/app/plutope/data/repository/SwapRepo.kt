package com.app.plutope.data.repository

import com.app.plutope.model.AvailablePairsResponseModel
import com.app.plutope.model.ExchangeRequestModel
import com.app.plutope.model.ExchangeResponseModel
import com.app.plutope.model.ExchangeStatusResponse
import com.app.plutope.model.OkxApproveResponse
import com.app.plutope.model.OkxSwapResponse
import com.app.plutope.model.RangSwapQuoteModel
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.RangoSwapResponseModel
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import org.json.JSONObject
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class SwapRepo @Inject constructor(private val apiHelper: ApiHelper) {

    //execute exchange
    suspend fun executeExchange(
        url: String,
        body: ExchangeRequestModel
    ): NetworkState<ExchangeResponseModel?> {
        return try {
            val response = apiHelper.executeExchange(url,body)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.id != null) {
                    NetworkState.Success("", result)
                } else {
                    val jsonString=response.errorBody()?.string()
                    try {
                        val jsonObject = JSONObject(jsonString)
                        val errorMessage = jsonObject.getString("message")
                        return NetworkState.Error(errorMessage)
                    } catch (e: Exception) {
                        // Handle any parsing or JSON-related exceptions here
                        e.printStackTrace()
                        return NetworkState.Error(response.errorBody().toString())
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }

        }

    }


    suspend fun executeExchangeStatus(url: String):NetworkState<ExchangeStatusResponse?>{
        return try {
            val response = apiHelper.executeExchangeStatus(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.id != null) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error(response.errorBody().toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            if(e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }
    }


    suspend fun executeSwapUsingOkx(url: String,headerOkxSignKey: String,
                                    headerTimeStamp: String) : NetworkState<OkxSwapResponse?>{
        return try {
            val response = apiHelper.executeSwapUsingOkx(url,headerOkxSignKey,
                headerTimeStamp)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.data1 != null) {
                    NetworkState.Success(result.code, result)
                } else {
                    NetworkState.Error(response.message())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }
    }

    suspend fun executeSwapAvailablePairs(url: String):NetworkState<MutableList<AvailablePairsResponseModel>?>{
        return try {
            val response = apiHelper.executeAvailablePairs(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.isNotEmpty() != null) {
                    NetworkState.Success("", result)
                } else {
                    val jsonObject = JSONObject(response.errorBody()?.string())

                    NetworkState.Error(jsonObject.getString("error"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }
    }

    suspend fun executeApproveUsingOkx(url: String,headerOkxSignKey: String,
                                       headerTimeStamp: String) : NetworkState<OkxApproveResponse?>{
        return try {
            val response = apiHelper.executeApproveUsingOkx(url,headerOkxSignKey,headerTimeStamp)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.data1 != null) {
                    NetworkState.Success(result.code, result)
                } else {
                    NetworkState.Error(response.message())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }
        }
    }


    suspend fun executeRangoExchangeQuote(
        fromBlockchain: String,
        fromTokenSymbol: String,
        fromTokenAddress: String,
        toBlockchain: String,
        toTokenSymbol: String,
        toTokenAddress: String,
        walletAddress: String,
        price: String,
        fromWalletAddress: String,
        toWalletAddress: String,
    ): NetworkState<RangSwapQuoteModel?> {
        return try {
            val response = apiHelper.rangSwapQuoteCall(
                fromBlockchain,
                fromTokenSymbol,
                fromTokenAddress,
                toBlockchain,
                toTokenSymbol,
                toTokenAddress,
                walletAddress,
                price,
                fromWalletAddress,
                toWalletAddress,
            )
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.route != null) {
                    NetworkState.Success("", result)
                } else {


                    NetworkState.Error("null")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }

        }

    }


    suspend fun rangSwapSubmitCall(
        fromBlockchain: String,
        fromTokenSymbol: String,
        fromTokenAddress: String,
        toBlockchain: String,
        toTokenSymbol: String,
        toTokenAddress: String,
        walletAddress: String,
        price: String,
        fromWalletAddress: String,
        toWalletAddress: String,
    ): NetworkState<RangoSwapResponseModel?> {
        return try {
            val response = apiHelper.rangSwapSubmitCall(
                fromBlockchain,
                fromTokenSymbol,
                fromTokenAddress,
                toBlockchain,
                toTokenSymbol,
                toTokenAddress,
                walletAddress,
                price, fromWalletAddress, toWalletAddress
            )
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.route != null) {
                    NetworkState.Success("", result)
                } else {


                    NetworkState.Error("null")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }

        }

    }


}