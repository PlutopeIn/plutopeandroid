package com.app.plutope.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MeldResponseModel(
    val error: String,
    val message: String,
   @SerializedName("quotes") val quotes: List<MeldResult>
):Parcelable

@Parcelize
data class MeldResult(
    val countryCode: String,
    val customerScore: Double,
    val destinationAmount: Double,
    val destinationAmountWithoutFees: String,
    val destinationCurrencyCode: String,
    val exchangeRate: Double,
    val fiatAmountWithoutFees: Double,
    val networkFee: String,
    val paymentMethodType: String,
    val serviceProvider: String,
    val sourceAmount: Double,
    val sourceAmountWithoutFees: Double,
    val sourceCurrencyCode: String,
    val totalFee: Double,
    val transactionFee: String,
    val transactionType: String
):Parcelable