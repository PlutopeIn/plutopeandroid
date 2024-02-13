package com.app.plutope.ui.fragment.transactions.swap.previewSwap


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RangoSwapResponseModel(
    @SerializedName("error")
    var error: String?,
    @SerializedName("requestId")
    var requestId: String?,
    @SerializedName("resultType")
    var resultType: String?,
    @SerializedName("route")
    var route: Route?,
    @SerializedName("tx")
    var tx: Tx?
) : Parcelable {
    @Parcelize
    data class Route(
        @SerializedName("amountRestriction")
        var amountRestriction: AmountRestriction?,
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

    @Parcelize
    data class Tx(
        @SerializedName("approveData")
        var approveData: String?,
        @SerializedName("approveTo")
        var approveTo: String?,
        @SerializedName("blockChain")
        var blockChain: BlockChain?,
        @SerializedName("from")
        var from: String?,
        @SerializedName("gasLimit")
        var gasLimit: String?,
        @SerializedName("gasPrice")
        var gasPrice: String?,
        @SerializedName("maxGasPrice")
        var maxGasPrice: String?,
        @SerializedName("priorityGasPrice")
        var priorityGasPrice: String?,
        @SerializedName("txData")
        var txData: String?,
        @SerializedName("txTo")
        var txTo: String?,
        @SerializedName("type")
        var type: String?,
        @SerializedName("value")
        var value: String?
    ) : Parcelable {
        @Parcelize
        data class BlockChain(
            @SerializedName("addressPatterns")
            var addressPatterns: List<String?>?,
            @SerializedName("chainId")
            var chainId: String?,
            @SerializedName("defaultDecimals")
            var defaultDecimals: Int?,
            @SerializedName("feeAssets")
            var feeAssets: List<FeeAsset?>?,
            @SerializedName("name")
            var name: String?,
            @SerializedName("type")
            var type: String?
        ) : Parcelable {
            @Parcelize
            data class FeeAsset(
                @SerializedName("address")
                var address: String?,
                @SerializedName("blockchain")
                var blockchain: String?,
                @SerializedName("symbol")
                var symbol: String?
            ) : Parcelable
        }
    }
}

@Parcelize
data class AmountRestriction(
    @SerializedName("min")
    var min: String?,
    @SerializedName("max")
    var max: String?,
    @SerializedName("type")
    var type: String?
) : Parcelable
