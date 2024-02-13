package com.app.plutope.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class UnlimitBestPriceModel(
    @SerializedName("amountOut")
    var amountOut: String?,
    @SerializedName("networkFee")
    var networkFee: String?,
    @SerializedName("processingFee")
    var processingFee: String?
) : Parcelable