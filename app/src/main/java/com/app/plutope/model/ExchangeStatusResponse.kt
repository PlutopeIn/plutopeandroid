package com.app.plutope.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExchangeStatusResponse(
    val actionsAvailable: Boolean,
    val amountFrom: String?,
    val amountTo: String?,
    val createdAt: String,
    val depositReceivedAt: String?,
    val expectedAmountFrom: Double,
    val expectedAmountTo: Double,
    val fromCurrency: String,
    val fromLegacyTicker: String,
    val fromNetwork: String,
    val id: String,
    val payinAddress: String,
    val payinExtraId: String?,
    val payinHash: String?,
    val payoutAddress: String,
    val payoutExtraId: String?,
    val payoutHash: String?,
    val refundAddress: String?,
    val refundAmount: String?,
    val refundExtraId: String?,
    val refundHash: String?,
    val status: String,
    val toCurrency: String,
    val toLegacyTicker: String,
    val toNetwork: String,
    val updatedAt: String,
    val validUntil: String?
):Parcelable