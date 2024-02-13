package com.app.plutope.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExchangeResponseModel(
    val flow: String,
    val fromAmount: Double,
    val fromCurrency: String,
    val fromNetwork: String,
    val id: String,
    val payinAddress: String,
    val payoutAddress: String,
    val toAmount: Double,
    val toCurrency: String,
    val toNetwork: String,
    val type: String
):Parcelable


enum class SwapToken{
    BNB,USDT,FROMTOKEN,TOTOKEN
}
