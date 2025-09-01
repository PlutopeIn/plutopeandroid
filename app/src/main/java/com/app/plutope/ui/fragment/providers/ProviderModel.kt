package com.app.plutope.ui.fragment.providers

import android.os.Parcelable
import com.app.plutope.model.CoinCode
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProviderModel(
    var providerName: String = "",
    val coinCode: CoinCode,
    var minAmount: Int = 0,
    var currency: String = "",
    var bestPrice: String = "0.0",
    var symbol: String = "",
    var icon: Int = 0,
    var isSelected: Boolean = false,
    var isFromSell: Boolean = false,
    var currencyCode: String = "",
    var isBestPrise: Boolean = false,
    var percentageBestPrice: String = "0.0",
    var swapperFees: String = "0.0",
    var providerIcon:String="",
    var webUrl:String? ="",
    var name:String="",
    var exodusId:String?="",
    var exodusTransactionId:String?=""



) : Parcelable