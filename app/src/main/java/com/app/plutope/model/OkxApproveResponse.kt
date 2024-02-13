package com.app.plutope.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class OkxApproveResponse(
    val code: String,
    @SerializedName("data") val data1: List<DataApprove>,
    val msg: String
)

@Keep
data class DataApprove(
    @SerializedName("data") val dataApprove: String,
    val dexContractAddress: String,
    val gasLimit: String,
    val gasPrice: String
)