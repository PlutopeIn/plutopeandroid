package com.app.plutope.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class CountryListModel(
    @SerializedName("code")
    var code: String? = "",
    @SerializedName("image")
    var image: String? = "",
    @SerializedName("name")
    var countryName: String? = "",
    @SerializedName("unicode")
    var unicode: String? = "",
    var currencyCode: String? = "No universal currency",
    var currencyName: String? = "No universal currency",
    var currencySymbol: String? = "",
    var isSelected: Boolean? = false
) : Parcelable



