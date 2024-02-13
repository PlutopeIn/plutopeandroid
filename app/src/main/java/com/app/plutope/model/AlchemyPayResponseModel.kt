package com.app.plutope.model

import com.google.gson.annotations.SerializedName

data class AlchemyPayResponseModel(
    @SerializedName("data") val resultList: AlchemyPayResponse,
    val success: Boolean
)

data class AlchemyPayResponse(
    val crypto: String,
    val cryptoPrice: String,
    val cryptoQuantity: String,
    val fiat: String,
    val networkFee: String,
    val payWayCode: String,
    val rampFee: String
)