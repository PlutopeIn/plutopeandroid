package com.app.plutope.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class RangSwapQuoteModel(
    @SerializedName("error")
    var error: String?,
    @SerializedName("requestId")
    var requestId: String?,
    @SerializedName("resultType")
    var resultType: String?,
    @SerializedName("route")
    var route: Route?
) : Parcelable {
    @Keep
    @Parcelize
    data class Route(
        @SerializedName("amountRestriction")
        var amountRestriction: String?,
        @SerializedName("estimatedTimeInSeconds")
        var estimatedTimeInSeconds: Int?,
        @SerializedName("fee")
        var fee: List<Fee?>?,
        @SerializedName("feeUsd")
        var feeUsd: Double?,
        @SerializedName("from")
        var from: From?,
        @SerializedName("outputAmount")
        var outputAmount: String?,
        @SerializedName("outputAmountMin")
        var outputAmountMin: String?,
        @SerializedName("outputAmountUsd")
        var outputAmountUsd: Double?,
        @SerializedName("path")
        var path: List<Path?>?,
        @SerializedName("swapper")
        var swapper: Swapper?,
        @SerializedName("to")
        var to: To?
    ) : Parcelable {
        @Keep
        @Parcelize
        data class Fee(
            @SerializedName("amount")
            var amount: String?,
            @SerializedName("expenseType")
            var expenseType: String?,
            @SerializedName("name")
            var name: String?,
            @SerializedName("token")
            var token: Token?
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Token(
                @SerializedName("address")
                var address: String?,
                @SerializedName("blockchain")
                var blockchain: String?,
                @SerializedName("blockchainImage")
                var blockchainImage: String?,
                @SerializedName("chainId")
                var chainId: String?,
                @SerializedName("decimals")
                var decimals: Int?,
                @SerializedName("image")
                var image: String?,
                @SerializedName("isPopular")
                var isPopular: Boolean?,
                @SerializedName("name")
                var name: String?,
                @SerializedName("supportedSwappers")
                var supportedSwappers: List<String?>?,
                @SerializedName("symbol")
                var symbol: String?,
                @SerializedName("usdPrice")
                var usdPrice: Double?
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class From(
            @SerializedName("address")
            var address: String?,
            @SerializedName("blockchain")
            var blockchain: String?,
            @SerializedName("blockchainImage")
            var blockchainImage: String?,
            @SerializedName("chainId")
            var chainId: String?,
            @SerializedName("decimals")
            var decimals: Int?,
            @SerializedName("image")
            var image: String?,
            @SerializedName("isPopular")
            var isPopular: Boolean?,
            @SerializedName("name")
            var name: String?,
            @SerializedName("supportedSwappers")
            var supportedSwappers: List<String?>?,
            @SerializedName("symbol")
            var symbol: String?,
            @SerializedName("usdPrice")
            var usdPrice: Double?
        ) : Parcelable

        @Keep
        @Parcelize
        data class Path(
            @SerializedName("estimatedTimeInSeconds")
            var estimatedTimeInSeconds: Int?,
            @SerializedName("expectedOutput")
            var expectedOutput: String?,
            @SerializedName("from")
            var from: From?,
            @SerializedName("inputAmount")
            var inputAmount: String?,
            @SerializedName("swapper")
            var swapper: Swapper?,
            @SerializedName("swapperType")
            var swapperType: String?,
            @SerializedName("to")
            var to: To?
        ) : Parcelable {
            @Keep
            @Parcelize
            data class From(
                @SerializedName("address")
                var address: String?,
                @SerializedName("blockchain")
                var blockchain: String?,
                @SerializedName("blockchainImage")
                var blockchainImage: String?,
                @SerializedName("chainId")
                var chainId: String?,
                @SerializedName("decimals")
                var decimals: Int?,
                @SerializedName("image")
                var image: String?,
                @SerializedName("isPopular")
                var isPopular: Boolean?,
                @SerializedName("name")
                var name: String?,
                @SerializedName("supportedSwappers")
                var supportedSwappers: List<String?>?,
                @SerializedName("symbol")
                var symbol: String?,
                @SerializedName("usdPrice")
                var usdPrice: Double?
            ) : Parcelable

            @Keep
            @Parcelize
            data class Swapper(
                @SerializedName("enabled")
                var enabled: Boolean?,
                @SerializedName("id")
                var id: String?,
                @SerializedName("logo")
                var logo: String?,
                @SerializedName("swapperGroup")
                var swapperGroup: String?,
                @SerializedName("title")
                var title: String?,
                @SerializedName("types")
                var types: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class To(
                @SerializedName("address")
                var address: String?,
                @SerializedName("blockchain")
                var blockchain: String?,
                @SerializedName("blockchainImage")
                var blockchainImage: String?,
                @SerializedName("chainId")
                var chainId: String?,
                @SerializedName("decimals")
                var decimals: Int?,
                @SerializedName("image")
                var image: String?,
                @SerializedName("isPopular")
                var isPopular: Boolean?,
                @SerializedName("name")
                var name: String?,
                @SerializedName("supportedSwappers")
                var supportedSwappers: List<String?>?,
                @SerializedName("symbol")
                var symbol: String?,
                @SerializedName("usdPrice")
                var usdPrice: Double?
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class Swapper(
            @SerializedName("enabled")
            var enabled: Boolean?,
            @SerializedName("id")
            var id: String?,
            @SerializedName("logo")
            var logo: String?,
            @SerializedName("swapperGroup")
            var swapperGroup: String?,
            @SerializedName("title")
            var title: String?,
            @SerializedName("types")
            var types: List<String?>?
        ) : Parcelable

        @Keep
        @Parcelize
        data class To(
            @SerializedName("address")
            var address: String?,
            @SerializedName("blockchain")
            var blockchain: String?,
            @SerializedName("blockchainImage")
            var blockchainImage: String?,
            @SerializedName("chainId")
            var chainId: String?,
            @SerializedName("decimals")
            var decimals: Int?,
            @SerializedName("image")
            var image: String?,
            @SerializedName("isPopular")
            var isPopular: Boolean?,
            @SerializedName("name")
            var name: String?,
            @SerializedName("supportedSwappers")
            var supportedSwappers: List<String?>?,
            @SerializedName("symbol")
            var symbol: String?,
            @SerializedName("usdPrice")
            var usdPrice: Double?
        ) : Parcelable
    }
}