package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class TransactionHistoryResponse(
    @SerializedName("data") val transaction: List<Transaction>
):Parcelable

@Keep
@Parcelize
data class Transaction(
    val chainFullName: String,
    val chainShortName: String,
    val limit: String,
    val page: String,
    val totalPage: String,
    var transactionLists: List<TransactionLists>
):Parcelable

@Keep
@Parcelize
data class TransactionLists(
    val amount: String="",
    val blockHash: String="",
    val challengeStatus: String="",
    val from: String="",
    val height: String="",
    val isFromContract: Boolean=false,
    val isToContract: Boolean=false,
    val l1OriginHash: String="",
    val methodId: String="",
    val state: String="",
    val to: String="",
    val tokenContractAddress: String="",
    val tokenId: String="",
    val transactionSymbol: String="",
    var transactionTime: String="",
    val txFee: String="",
    val txId: String="",
    var addressShow: String = "",
    var priceToShow: String? = "",
    var transactionType: String? = "",
    var transactionTimeInMillis: String? = "",
    var isAaTransaction: Boolean? = false,
    var isSwap: Boolean = false
):Parcelable