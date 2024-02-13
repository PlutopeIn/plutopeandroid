package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class OnMetaBestPriceResponseModel(
    @SerializedName("data") val result: ResultOnMeta,
):Parcelable

@Keep
@Parcelize
data class ResultOnMeta(
    val conversionRate: String,
    val estimateId: String,
    val gasPriceNativeToken: String,
    val gasPriceWei: String,
    val gasUseEstimate: String,
    val nativeTokenDecimals: Int,
    val quote: String,
    val receivedTokens: String,
    val source: String,
    val sellToken:String,
    val fiatAmount:String
):Parcelable



