package com.app.plutope.model

import androidx.annotation.Keep

@Keep
data class OnRampBestPriceRequestModel(
    val chainId: String,
    val coinCode: String,
    val fiatAmount: String,
    val fiatType: String,
    val network: String,
    val type: Int
)

@Keep
data class OnRampSellBestPriceRequestModel(
    val chainId: String,
    val coinCode: String,
    val quantity: String,
    val fiatType: String,
    val network: String,
    val type: Int
)