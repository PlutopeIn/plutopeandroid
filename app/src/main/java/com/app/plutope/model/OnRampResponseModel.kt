package com.app.plutope.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OnRampResponseModel(
   @SerializedName("data") val response: OnRampResponse
)

@Keep
data class OnRampResponse(
    val clientFee: Int,
    val gasFee: Double,
    val gatewayFee: Int,
    val onrampFee: Double,
    val quantity: Double,
    val rate: Double
)