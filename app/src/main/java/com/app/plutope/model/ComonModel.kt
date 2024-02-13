package com.app.plutope.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class PointModel(var id: Int, var pointText: String)

data class CoinListModel(
    var id: String,
    var coinName: String,
    var coinPrice: String,
    var coinType: String,
    var coinImage: String
)

@Parcelize
data class ButtonModel(
    @SerializedName("id")
    var id: Int,
    var buttonName: String = "", val image: Int,
    var isViewed: Boolean = false
) : Parcelable