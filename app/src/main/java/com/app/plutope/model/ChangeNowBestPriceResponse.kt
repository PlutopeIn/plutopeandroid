package com.app.plutope.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeNowBestPriceResponse(
    val fromAmount: String,
    val fromCurrency: String,
    val toAmount: Double,
    val toCurrency: String,
    val type: String
):Parcelable