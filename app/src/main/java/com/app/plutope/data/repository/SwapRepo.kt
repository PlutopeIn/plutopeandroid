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
import com.app.plutope.ui.fragment.transactions.swap.SwapQuoteRequestModel
import com.app.plutope.ui.fragment.transactions.swap.SwapQuoteResponseModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.ExodusSwapResponseModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.RangoSwapResponseModel
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.generateRequestBody
import com.app.plutope.utils.network.NetworkState
import org.json.JSONObject
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class SwapRepo @Inject constructor(private val apiHelper: ApiHelper) {

    //execute exchange
    suspend fun executeExchange(
        url: String,
        body: ExchangeRequestModel, lastEnteredAmount: String
    ): NetworkState<ExchangeResponseModel?> {
        return try {
            val response = apiHelper.executeExchange(url, body)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.id != null) {
                    val res = result
                    res.lastEnteredAmount = lastEnteredAmount
                    NetworkState.Success("", res)
                } else {

                    val jsonString = response.errorBody()?.string()
                    try {
                        val jsonObject = jsonString?.let { JSONObject(it) }
                        val errorMessage = jsonObject?.getString("message")
                        return NetworkState.Error(errorMessage ?: "Error occurred")
                    } catch (e: Exception) {
                        // Handle any parsing or JSON-related exceptions here
                        e.printStackTrace()
                        return NetworkState.Error(response.errorBody().toString())
                    }

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


    suspend fun executeExchangeStatus(url: String): NetworkState<ExchangeStatusResponse?> {
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

            if (e is NoConnectivityException || e is UnknownHostException || e is ConnectException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
                NetworkState.Error(e.message.toString())
            }
        }
    }


    suspend fun executeSwapUsingOkx(
        url: String, headerOkxSignKey: String,
        headerTimeStamp: String, lastEnteredAmount: String
    ): NetworkState<OkxSwapResponse?> {
        return try {
            val response = apiHelper.executeSwapUsingOkx(
                url, headerOkxSignKey,
                headerTimeStamp
            )
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.data1 != null) {
                    val res = result
                    res.lastEnteredAmount = lastEnteredAmount
                    NetworkState.Success(result.code, res)
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

    suspend fun executeSwapAvailablePairs(url: String): NetworkState<MutableList<AvailablePairsResponseModel>?> {
        return try {
            val response = apiHelper.executeAvailablePairs(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.isNotEmpty() != null) {
                    NetworkState.Success("", result)
                } else {
                    val jsonObject = response.errorBody()?.string()?.let { JSONObject(it) }

                    NetworkState.Error(jsonObject!!.getString("error"))
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

    suspend fun executeApproveUsingOkx(
        url: String, headerOkxSignKey: String,
        headerTimeStamp: String
    ): NetworkState<OkxApproveResponse?> {
        return try {
            val response = apiHelper.executeApproveUsingOkx(url, headerOkxSignKey, headerTimeStamp)
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
        lastEnteredAmount: String
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
                    val res = result
                    res.enteredAmount = lastEnteredAmount
                    NetworkState.Success(lastEnteredAmount, res)
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
        isFromButtonCliked: Boolean
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
                    val res = result
                    res.isFromButtonCliked = isFromButtonCliked
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


    suspend fun swapQuoteSingleCall(body: SwapQuoteRequestModel): NetworkState<SwapQuoteResponseModel?> {
        return try {
            val response = apiHelper.swapQuoteSingleCall(body)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success(result.status.toString(), result)
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

    suspend fun exodusSwapUpdateOrderCall(
        id: String,
        transactionId: String
    ): NetworkState<ExodusSwapResponseModel?> {
        return try {

            val params = mapOf(
                "id" to id,
                "transactionId" to transactionId,
            )

            val response = apiHelper.exodusSwapUpdateOrderCall(
                body = generateRequestBody(params)
            )


            // val response = apiHelper.exodusSwapUpdateOrderCall(body)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success("", result)
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


    suspend fun exodusTransactionStatusCall(id: String): NetworkState<ExodusSwapResponseModel?> {
        return try {
            val response =
                apiHelper.exodusTransactionStatusCall(BASE_URL_PLUTO_PE + "exodus-swap-single-orders/$id")
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {
                    NetworkState.Success("", result)
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

}