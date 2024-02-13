package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Keep
enum class TransactionStatus{
    IN,OUT
}
class TransactionModel(val transactionId:String, val status: TransactionStatus, val amount:String, val price_currency:BigDecimal, val address:String, val fee:String, val dateTime:String, var isCheckable:Boolean=false)

@Keep
@Parcelize
data class TransactionHistoryModel(
    val message: String="",
    val result: MutableList<Result> = mutableListOf(),
    val status: String=""
):Parcelable

@Keep
@Parcelize
data class Result(
    val blockHash: String="",
    val blockNumber: String="",
    val confirmations: String="",
    val contractAddress: String="",
    val cumulativeGasUsed: String="",
    val from: String="",
    val functionName: String="",
    val gas: String="",
    val gasPrice: String="",
    val gasUsed: String="",
    val hash: String="",
    val input: String="",
    val isError: String="",
    val methodId: String="",
    val nonce: String="",
    var timeStamp: String="",
    val to: String="",
    val transactionIndex: String="",
    val txreceipt_status: String="",
    var value: String="0.0",
    var addressShow:String?="",
    var priceToShow:String?="",
) : Parcelable