package com.app.plutope.model

import android.os.Parcelable
import com.app.plutope.utils.extras.PreferenceHelper
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import com.app.plutope.utils.coinTypeEnum.CoinType

data class Info (
    val data: MutableMap<String, CryptoData> = mutableMapOf()
)

@Parcelize
data class CryptoData(
    val id: String="",
    val name: String="",
    val symbol: String="",
    var logo: String="",
    val quote: Quote? = null,
    var coinType:CoinType?=null,
    var balance:String ="0"
):Parcelable




@Parcelize
data class Quote(
   // @SerializedName("INR")
    val USD : USD? = USD()
):Parcelable

@Parcelize
data class USD(
    val price : String ="",
    val percent_change_24h : String="",
):Parcelable