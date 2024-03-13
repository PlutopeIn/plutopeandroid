package com.app.plutope.data.repository

import com.app.plutope.model.Tokens
import com.app.plutope.model.Transaction
import com.app.plutope.model.TransactionDetail
import com.app.plutope.model.TransactionHistoryModel
import com.app.plutope.model.TransactionLists
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.date_formate.getReadableDate
import com.app.plutope.utils.decryptTransferInput
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.weiToEther
import java.math.BigInteger
import java.math.RoundingMode
import java.net.UnknownHostException
import javax.inject.Inject

class TransactionHistoryRepo @Inject constructor(private val apiHelper: ApiHelper) {


    suspend fun getTransactionHistory(
        url: String,
        tokens: Tokens
    ): NetworkState<TransactionHistoryModel?> {
        return try {
            val response = apiHelper.executeTransactionHistory(url = url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.result != null) {

                    result.result.forEach { model ->
                        val valueResponse: BigInteger = if (tokens.t_address != "") {
                            decryptTransferInput(model.input)?.second ?: BigInteger.ZERO
                        } else {
                            model.value.toBigInteger()
                        }
                        if (model.from.lowercase() == tokens.chain?.walletAddress.toString()
                                .lowercase()
                        ) {
                            model.addressShow = model.to

                            model.priceToShow = "-" + String.format(
                                "%.5f",
                                weiToEther(valueResponse).toString().toDouble()
                            ) + " " + tokens.chain?.symbol

                        } else {
                            model.addressShow = model.from
                            model.priceToShow = "+" + String.format(
                                "%.5f",
                                weiToEther(valueResponse).toString().toDouble()
                            ) + " " + tokens.chain?.symbol
                        }
                        model.timeStamp = model.timeStamp.toLong().getReadableDate().toString()//getDateFromTimeStampShow(model.timeStamp.toLong())

                    }
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error(response.message())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }


    suspend fun getTransactionHistoryNew(
        url: String,
        tokens: Tokens
    ): NetworkState<Transaction?> {
        return try {
            val response = apiHelper.executeOkLinkTransactionList(url = url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.transaction != null) {
                    val list = result.transaction
                    val transList = mutableListOf<TransactionLists>()
                    val transListFilter = mutableListOf<TransactionLists>()
                    transList.addAll(list[0].transactionLists)
                    transList.groupBy { it.txId }.forEach {
                        val transModel = it.value.filter {

                            loge(
                                " MatchSymbole",
                                "${it.transactionSymbol.lowercase()} == ${tokens.t_symbol?.lowercase()}"
                            )
                            val formatedSymbole =
                                if (it.transactionSymbol.lowercase() == "usdc.e") "usdc" else it.transactionSymbol.lowercase()

                            //formatedSymbole == tokens.t_symbol?.lowercase()

                            tokens.t_address == it.tokenContractAddress
                        }


                        if(transModel.isNotEmpty()) {
                            if (it.value.size > 1) {
                                transModel[0].isSwap = true
                                transListFilter.add(transModel[0])
                            } else {
                                if (transModel.isNotEmpty()) transListFilter.add(transModel[0])
                            }
                        }
                    }



                    transListFilter.forEach { transaction ->
                        val valueResponse = transaction.amount.toBigDecimal().setScale(
                            4,
                            RoundingMode.DOWN
                        )

                        if (transaction.from.lowercase() == tokens.chain?.walletAddress.toString()
                                .lowercase()
                        ) {
                            transaction.addressShow =
                                if (transaction.isSwap) transaction.from else transaction.to
                            transaction.priceToShow = "-" + valueResponse + " " + tokens.t_symbol
                        } else {
                            transaction.addressShow =
                                if (transaction.isSwap) transaction.to else transaction.from
                            transaction.priceToShow = "+" + valueResponse + " " + tokens.t_symbol
                        }


                        /*
                                                if (transaction.from.lowercase() == "0x5B2f0cFfAEDFDF788298e2dd517796cE51a6D404".toString()
                                                        .lowercase()
                                                ) {
                                                    transaction.addressShow = if (transaction.isSwap) transaction.from else transaction.to
                                                    transaction.priceToShow = "-" + valueResponse + " " + tokens.t_symbol
                                                } else {
                                                    transaction.addressShow =
                                                        if (transaction.isSwap) transaction.to else transaction.from
                                                    transaction.priceToShow = "+" + valueResponse + " " + tokens.t_symbol
                                                }
                        */



                        transaction.transactionTimeInMillis = transaction.transactionTime
                        transaction.transactionTime =
                            transaction.transactionTime.toLong().getReadableDate()
                                .toString()//getDateFromTimeStampShow(transaction.transactionTime.toLong())
                    }

                    // transListFilter.contains(TransactionLists)

                    list[0].transactionLists = transListFilter.distinctBy { it.txId }
                        .filter { it.amount != "0" || it.txFee != "" }
                    NetworkState.Success("", list[0])
                } else {
                    NetworkState.Error(response.message())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(serverErrorMessage)
            }
        }
    }


    suspend fun getTransactionHistoryDetail(
        url: String
    ): NetworkState<List<TransactionDetail>?> {
        return try {
            val response = apiHelper.executeOkLinkTransactionDetail(url = url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.result != null) {
                    NetworkState.Success("", result.result)
                } else {
                    NetworkState.Error(response.message())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(serverErrorMessage)
            }
        }

    }

}