package com.app.plutope.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExchangeRequestModel(
    val fromCurrency: String,
    val toCurrency: String,
    val fromNetwork: String,
    val toNetwork: String,
    val fromAmount: String,
    val address: String
) : Parcelable