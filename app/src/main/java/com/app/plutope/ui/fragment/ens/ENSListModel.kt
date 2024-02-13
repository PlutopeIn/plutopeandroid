package com.app.plutope.ui.fragment.ens


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class ENSListModel(
    @SerializedName("data")
    var `data`: Data?,
    @SerializedName("message")
    var message: String?,
    @SerializedName("status")
    var status: Int?
) : Parcelable {
    @Parcelize
    data class Data(
        @SerializedName("availability")
        var availability: Availability?,
        @SerializedName("name")
        var name: String?,
        @SerializedName("tx")
        var tx: Tx?
    ) : Parcelable {
        @Parcelize
        data class Availability(
            @SerializedName("price")
            var price: Price?,
            @SerializedName("status")
            var status: String?
        ) : Parcelable {
            @Parcelize
            data class Price(
                @SerializedName("listPrice")
                var listPrice: ListPrice?,
                @SerializedName("subTotal")
                var subTotal: SubTotal?
            ) : Parcelable {
                @Parcelize
                data class ListPrice(
                    @SerializedName("usdCents")
                    var usdCents: Int?
                ) : Parcelable

                @Parcelize
                data class SubTotal(
                    @SerializedName("usdCents")
                    var usdCents: Int?
                ) : Parcelable
            }
        }

        @Parcelize
        data class Tx(
            @SerializedName("arguments")
            var arguments: Arguments?,
            @SerializedName("chainId")
            var chainId: Int?,
            @SerializedName("function")
            var function: String?,
            @SerializedName("params")
            var params: Params?
        ) : Parcelable {
            @Parcelize
            data class Arguments(
                @SerializedName("expiration")
                var expiration: Int?,
                @SerializedName("keys")
                var keys: List<String?>?,
                @SerializedName("labels")
                var labels: List<String?>?,
                @SerializedName("owner")
                var owner: String?,
                @SerializedName("price")
                var price: String?,
                @SerializedName("signature")
                var signature: String?,
                @SerializedName("values")
                var values: List<String?>?
            ) : Parcelable

            @Parcelize
            data class Params(
                @SerializedName("data")
                var `data`: String?,
                @SerializedName("to")
                var to: String?,
                @SerializedName("value")
                var value: String?
            ) : Parcelable
        }
    }
}