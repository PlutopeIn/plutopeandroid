package com.app.plutope.model

import com.google.gson.annotations.SerializedName

data class CoinMarketCurrencyResponse(
    @SerializedName("data") val currency: MutableList<CurrencyModel>,
    val status: Status
)

data class Status(
    val credit_count: Int,
    val elapsed: Int,
    val error_code: Int,
    val error_message: Any,
    val notice: Any,
    val timestamp: String
)