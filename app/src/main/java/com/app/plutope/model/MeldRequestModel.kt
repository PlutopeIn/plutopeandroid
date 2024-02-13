package com.app.plutope.model


data class MeldRequestModel(
    val countryCode: String,
    val destinationCurrencyCode: String,
    val sourceAmount: String,
    val sourceCurrencyCode: String
)