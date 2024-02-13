package com.app.plutope.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class EstimateMinOnChangeValue(
    @SerializedName("providers")
    var providers: List<Provider?>?,
    @SerializedName("summary")
    var summary: Summary?
) : Parcelable {
    @Parcelize
    data class Provider(
        @SerializedName("cashback")
        var cashback: String?,
        @SerializedName("custom")
        var custom: Custom?,
        @SerializedName("error")
        var error: Error?,
        @SerializedName("estimatedAmount")
        var estimatedAmount: Double?,
        @SerializedName("id")
        var id: String?,
        @SerializedName("isAllowed")
        var isAllowed: Boolean?,
        @SerializedName("isAmountInRange")
        var isAmountInRange: Boolean?,
        @SerializedName("isConvertible")
        var isConvertible: Boolean?,
        @SerializedName("isHighNetworkFee")
        var isHighNetworkFee: Int?,
        @SerializedName("label")
        var label: String?,
        @SerializedName("maxAmount")
        var maxAmount: Double?,
        @SerializedName("minAmount")
        var minAmount: Int?,
        @SerializedName("priority")
        var priority: Int?,
        @SerializedName("promoCode")
        var promoCode: String?,
        @SerializedName("type")
        var type: String?
    ) : Parcelable {
        @Parcelize
        data class Custom(
            @SerializedName("depositFee")
            var depositFee: Int?,
            @SerializedName("flow")
            var flow: String?,
            @SerializedName("rateId")
            var rateId: Int?,
            @SerializedName("transactionSpeedForecast")
            var transactionSpeedForecast: String?,
            @SerializedName("type")
            var type: String?,
            @SerializedName("validUntil")
            var validUntil: String?,
            @SerializedName("warningMessage")
            var warningMessage: String?,
            @SerializedName("withdrawalFee")
            var withdrawalFee: Int?
        ) : Parcelable

        @Parcelize
        data class Error(
            @SerializedName("error")
            var error: String?,
            @SerializedName("message")
            var message: String?
        ) : Parcelable
    }

    @Parcelize
    data class Summary(
        @SerializedName("cashback")
        var cashback: String?,
        @SerializedName("estimatedAmount")
        var estimatedAmount: Double?,
        @SerializedName("estimationFrom")
        var estimationFrom: String?,
        @SerializedName("estimationFromLabel")
        var estimationFromLabel: String?,
        @SerializedName("estimationId")
        var estimationId: String?,
        @SerializedName("isHighNetworkFee")
        var isHighNetworkFee: Int?,
        @SerializedName("maxAmount")
        var maxAmount: Int?,
        @SerializedName("minAmount")
        var minAmount: Int?,
        @SerializedName("providers")
        var providers: Int?
    ) : Parcelable
}