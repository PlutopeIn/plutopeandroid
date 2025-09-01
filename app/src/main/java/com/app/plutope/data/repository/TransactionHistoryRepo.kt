package com.app.plutope.data.repository

import com.app.plutope.R
import com.app.plutope.model.Tokens
import com.app.plutope.model.Transaction
import com.app.plutope.model.TransactionDetail
import com.app.plutope.model.TransactionHistoryModel
import com.app.plutope.model.TransactionLists
import com.app.plutope.model.Wallet.getString
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.ui.fragment.transactions.buy.buy_detail.TransactionMoralisResponse
import com.app.plutope.ui.fragment.transactions.buy.buy_detail.TransferHistoryModel
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.date_formate.getReadableDate
import com.app.plutope.utils.date_formate.toCal
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

                            model.priceToShow = buildString {
                                append("-")
                                append(
                                    String.format(
                                        getString(R.string._5f),
                                        weiToEther(valueResponse).toString().toDouble()
                                    )
                                )
                                append(" ")
                                append(tokens.chain?.symbol)
                            }

                        } else {
                            model.addressShow = model.from
                            model.priceToShow = "+" + String.format(
                                "%.5f",
                                weiToEther(valueResponse).toString().toDouble()
                            ) + " " + tokens.chain?.symbol
                        }
                        model.timeStamp = model.timeStamp.toLong().getReadableDate()
                            .toString()//getDateFromTimeStampShow(model.timeStamp.toLong())

                    }
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error(response.message())
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
            if (e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            } else {
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
                            loge("transList", "${tokens.t_address} == ${it.tokenContractAddress}")
                            tokens.t_address.lowercase() == it.tokenContractAddress.lowercase()
                        }

                        loge("transModel", "${transModel}")
                        if (transModel.isNotEmpty()) {
                            if (it.value.size > 1) {
                                transModel[0].isSwap = true
                                transListFilter.add(transModel[0])
                            } else {
                                if (transModel.isNotEmpty()) transListFilter.add(transModel[0])
                            }
                        }
                    }

                    loge("transListFilter", "${transListFilter.size}")

                    transListFilter.forEach { transaction ->
                        val valueResponse = transaction.amount.toBigDecimal().setScale(
                            6,
                            RoundingMode.DOWN
                        )


                        if (transaction.from.lowercase() == tokens.chain?.walletAddress.toString()
                                .lowercase()
                        ) {
                            transaction.addressShow =
                                if (transaction.isSwap) transaction.from else transaction.to
                            transaction.priceToShow =
                                if (transaction.transactionSymbol.lowercase() == "btc") {
                                    val symbol =
                                        if (valueResponse.toString().startsWith("-")) "" else "+"
                                    symbol + valueResponse + " " + tokens.t_symbol
                                } else {
                                    "-" + valueResponse + " " + tokens.t_symbol
                                }
                        } else {
                            transaction.addressShow =
                                if (transaction.isSwap) transaction.to else transaction.from
                            transaction.priceToShow =
                                if (transaction.transactionSymbol.lowercase() == "btc") {
                                    val symbol =
                                        if (valueResponse.toString().startsWith("-")) "" else "+"
                                    symbol + valueResponse + " " + tokens.t_symbol

                                } else {
                                    "+" + valueResponse + " " + tokens.t_symbol
                                }

                        }


                        transaction.transactionTimeInMillis = transaction.transactionTime
                        transaction.transactionTime =
                            transaction.transactionTime.toLong().getReadableDate()
                                .toString()//getDateFromTimeStampShow(transaction.transactionTime.toLong())
                    }

                    // transListFilter.contains(TransactionLists)

                    loge("transListFilter2", "${transListFilter.size}")

                    list[0].transactionLists = transListFilter.distinctBy { it.txId }
                        .filter { it.amount != "0" || it.txFee != "" }


                    NetworkState.Success("", list[0])
                } else {
                    NetworkState.Error(response.message())
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


    suspend fun executeMoralisTransactionList(
        url: String,
        tokens: Tokens
    ): NetworkState<TransactionMoralisResponse?> {
        return try {
            val response = apiHelper.executeMoralisTransactionList(url = url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {

                    //result.transaction?.filter { it.value != "0" }.forEach {  }

                    // var list1 = result.transaction?.filter {it.value != "0" }

                    val tempList =
                        mutableListOf<com.app.plutope.ui.fragment.transactions.buy.buy_detail.Transaction>()


                    result.transaction/*?.filter { it.value != "0" }*/?.forEach { model ->
                        model.transactionTime =
                            model.blockTimestamp?.toCal("yyyy-MM-dd'T'HH:mm:ss.SSSX")?.timeInMillis!!.getReadableDate()
                        model.addressShow = if (model.fromAddress.toString()
                                .lowercase() != tokens.chain?.walletAddress.toString().lowercase()
                        ) model.fromAddress else model.toAddress

                        val valueResponse =
                            if (tokens.t_address == "") weiToEther(model.value!!.toBigInteger()).toBigDecimal()
                                .setScale(
                                    6,
                                    RoundingMode.DOWN
                                ) else model.valueDecimal?.toBigDecimal()
                                ?.setScale(
                                    6,
                                    RoundingMode.DOWN
                                )

                        if (model.fromAddress?.lowercase() == tokens.chain?.walletAddress.toString()
                                .lowercase()
                        ) {
                            model.priceToShow = "-" + valueResponse?.stripTrailingZeros()
                                ?.toPlainString() + " " + tokens.t_symbol
                        } else {
                            model.priceToShow = "+" + valueResponse?.stripTrailingZeros()
                                ?.toPlainString() + " " + tokens.t_symbol
                        }

                        tempList.add(model)

                        // model.priceToShow = valueResponse.toString()
                    }


                    val result2 = TransactionMoralisResponse(
                        cursor = result.cursor,
                        limit = result.limit,
                        page = result.page?.plus(1),
                        pageSize = result.pageSize,
                        transaction = tempList
                    )

                    NetworkState.Success("", result2)
                } else {
                    NetworkState.Error(response.message())
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


    suspend fun executeTransactionHistoryList(
        url: String,
        tokens: Tokens,
        page: Int?
    ): NetworkState<TransferHistoryModel?> {
        return try {
            val response = apiHelper.executeTransactionHistoryList(url = url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result != null) {

                    //result.transaction?.filter { it.value != "0" }.forEach {  }

                    // var list1 = result.transaction?.filter {it.value != "0" }
                    var decimal = tokens.t_decimal
                    loge("CallDecimal", "${decimal}")
                    val tempList =
                        mutableListOf<TransferHistoryModel.Transactions>()


                    result.transactions/*?.filter { it.value != "0" }*/?.forEach { model ->
                        model.transactionTime =
                            model.timestamp?.toCal("yyyy-MM-dd'T'HH:mm:ss.SSSX")?.timeInMillis!!.getReadableDate()

                        model.transactionTitle = model.type
                        //if (model.transcationType == "smatContract") "Smart Contract" else "Transfer"
                        model.addressShow = if (model.fromAddress.toString()
                                .lowercase() != tokens.chain?.walletAddress.toString().lowercase()
                        ) model.fromAddress else model.toAddress

                        val valueResponse =
                            if (tokens.t_address == "") weiToEther(model.value!!.toBigInteger()).toBigDecimal()
                                .setScale(
                                    6,
                                    RoundingMode.DOWN
                                ) else model.value?.toBigDecimal()?.setScale(
                                6,
                                RoundingMode.DOWN
                            )




                        if (model.fromAddress?.lowercase() == tokens.chain?.walletAddress.toString()
                                .lowercase() && model.toAddress?.lowercase() != tokens.chain?.walletAddress.toString()
                                .lowercase()
                        ) {
                            if (tokens.t_address == "") {
                                model.priceToShow = "-" + valueResponse?.stripTrailingZeros()
                                    ?.toPlainString() + " " + tokens.t_symbol
                            } else {
                                model.priceToShow = model.formattedValue + " " + tokens.t_symbol
                            }

                        } else {
                            if (tokens.t_address == "") {
                                model.priceToShow = "+" + valueResponse?.stripTrailingZeros()
                                    ?.toPlainString() + " " + tokens.t_symbol
                            } else {
                                model.priceToShow = model.formattedValue + " " + tokens.t_symbol
                            }
                        }

                        model.isTransactionFailed = model.receiptStatus == "0"

                        tempList.add(model)

                        // model.priceToShow = valueResponse.toString()
                    }


                    val result2 = TransferHistoryModel(
                        cursor = result.cursor,
                        totalPage = 10,
                        page = page?.plus(1),
                        transactions = tempList
                    )

                    NetworkState.Success("", result2)
                } else {
                    NetworkState.Error(response.message())
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