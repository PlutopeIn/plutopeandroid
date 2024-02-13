package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AvailablePairsResponseModel(
    val flow: Flow,
    val fromCurrency: String,
    val fromNetwork: String,
    val toCurrency: String,
    val toNetwork: String
):Parcelable

@Keep
@Parcelize
data class Flow(
    @SerializedName("fixed-rate") val fixedRate: Boolean,
    val standard: Boolean
):Parcelable