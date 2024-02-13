package com.app.plutope.model

import androidx.annotation.Keep

@Keep
data class OnMetaBestPriceModel(
    val buyTokenAddress: String,
    val buyTokenSymbol: String,
    val chainId: Int,
    val fiatAmount: Double,
    val fiatCurrency: String
)

@Keep
data class OnMetaSellBestPriceModel(
    val chainId: Int,
    val fiatAmount: Int,
    val fiatCurrency: String,
    val sellTokenAddress: String,
    val sellTokenSymbol: String,
    val senderAddress: String
)