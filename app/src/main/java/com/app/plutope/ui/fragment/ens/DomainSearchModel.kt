package com.app.plutope.ui.fragment.ens


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DomainSearchModel(
    @SerializedName("currency")
    var currency: String?,
    @SerializedName("domainName")
    var domainName: String?,
    @SerializedName("owner")
    var owner: Owner?,
    @SerializedName("records")
    var records: Records?
) : Parcelable {
    @Parcelize
    data class Owner(
        @SerializedName("address")
        var address: String?
    ) : Parcelable

    @Parcelize
    data class Records(
        @SerializedName("crypto.ETH.address")
        var cryptoETHAddress: String?
    ) : Parcelable
}