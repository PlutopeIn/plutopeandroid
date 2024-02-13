package com.app.plutope.model


import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ProviderOnRampPriceDetail(
    @SerializedName("code")
    var code: Int?,
    @SerializedName("data")
    var `data`: Data?,
    @SerializedName("status")
    var status: Int?
) : Parcelable {
    @Keep
    @Parcelize
    data class Data(
        @SerializedName("coinConfig")
        var coinConfig: CoinConfig?,
        @SerializedName("gasFee")
        var gasFee: GasFee?,
        @SerializedName("minimumBuyAmount")
        var minimumBuyAmount: MinimumBuyAmount?,
        @SerializedName("networkConfig")
        var networkConfig: NetworkConfig?
    ) : Parcelable {
        @Keep
        @Parcelize
        data class CoinConfig(
            @SerializedName("balanceFloatPlaces")
            var balanceFloatPlaces: Int?,
            @SerializedName("coinIcon")
            var coinIcon: String?,
            @SerializedName("coinId")
            var coinId: String?,
            @SerializedName("coinName")
            var coinName: String?,
            @SerializedName("markets")
            var markets: Markets?,
            @SerializedName("networks")
            var networks: List<Int?>?,
            @SerializedName("tradeFloatPlaces")
            var tradeFloatPlaces: Int?
        ) : Parcelable {
            @Keep
            @Parcelize
            data class Markets(
                @SerializedName("decimals")
                var decimals: Decimals?
            ) : Parcelable {
                @Keep
                @Parcelize
                data class Decimals(
                    @SerializedName("inr")
                    var inr: Int?,
                    @SerializedName("usdt")
                    var usdt: Int?
                ) : Parcelable
            }
        }

        @Keep
        @Parcelize
        data class GasFee(
            @SerializedName("0")
            var x0: X0?,
            @SerializedName("1")
            var x1: X1?,
            @SerializedName("10")
            var x10: X10?,
            @SerializedName("10013")
            var x10013: X10013?,
            @SerializedName("10039")
            var x10039: X10039?,
            @SerializedName("10042")
            var x10042: X10042?,
            @SerializedName("10046")
            var x10046: X10046?,
            @SerializedName("10076")
            var x10076: X10076?,
            @SerializedName("10163")
            var x10163: X10163?,
            @SerializedName("11")
            var x11: X11?,
            @SerializedName("13")
            var x13: X13?,
            @SerializedName("2")
            var x2: X2?,
            @SerializedName("3")
            var x3: X3?,
            @SerializedName("4")
            var x4: X4?,
            @SerializedName("5")
            var x5: X5?,
            @SerializedName("8")
            var x8: X8?
        ) : Parcelable {
            @Keep
            @Parcelize
            data class X0(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X1(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10013(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10039(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10042(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10046(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10076(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10163(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X11(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X13(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X2(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X3(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X4(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X5(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X8(
                @SerializedName("minimumWithdrawal")
                var minimumWithdrawal: String?,
                @SerializedName("nodeInSync")
                var nodeInSync: Int?,
                @SerializedName("withdrawalFee")
                var withdrawalFee: String?
            ) : Parcelable
        }

        @Keep
        @Parcelize
        data class MinimumBuyAmount(
            @SerializedName("0")
            var x0: Double?,
            @SerializedName("1")
            var x1: Int?,
            @SerializedName("10")
            var x10: Int?,
            @SerializedName("10013")
            var x10013: Int?,
            @SerializedName("10039")
            var x10039: Int?,
            @SerializedName("10042")
            var x10042: Int?,
            @SerializedName("10046")
            var x10046: Int?,
            @SerializedName("10076")
            var x10076: Double?,
            @SerializedName("10163")
            var x10163: Int?,
            @SerializedName("13")
            var x13: Double?,
            @SerializedName("2")
            var x2: Int?,
            @SerializedName("3")
            var x3: Int?,
            @SerializedName("4")
            var x4: Int?,
            @SerializedName("5")
            var x5: Int?,
            @SerializedName("8")
            var x8: Int?
        ) : Parcelable

        @Keep
        @Parcelize
        data class NetworkConfig(
            @SerializedName("0")
            var x0: X0?,
            @SerializedName("1")
            var x1: X1?,
            @SerializedName("10")
            var x10: X10?,
            @SerializedName("10013")
            var x10013: X10013?,
            @SerializedName("10039")
            var x10039: X10039?,
            @SerializedName("10042")
            var x10042: X10042?,
            @SerializedName("10046")
            var x10046: X10046?,
            @SerializedName("10076")
            var x10076: X10076?,
            @SerializedName("10163")
            var x10163: X10163?,
            @SerializedName("13")
            var x13: X13?,
            @SerializedName("2")
            var x2: X2?,
            @SerializedName("3")
            var x3: X3?,
            @SerializedName("4")
            var x4: X4?,
            @SerializedName("5")
            var x5: X5?,
            @SerializedName("8")
            var x8: X8?
        ) : Parcelable {
            @Keep
            @Parcelize
            data class X0(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("nativeToken")
                var nativeToken: Int?,
                @SerializedName("networkId")
                var networkId: Int?,
                @SerializedName("node")
                var node: Int?,
                @SerializedName("startingWith")
                var startingWith: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X1(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("nativeToken")
                var nativeToken: Int?,
                @SerializedName("networkId")
                var networkId: Int?,
                @SerializedName("node")
                var node: Int?,
                @SerializedName("startingWith")
                var startingWith: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10(
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("confirmations")
                var confirmations: Int?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("nativeToken")
                var nativeToken: Int?,
                @SerializedName("networkId")
                var networkId: Int?,
                @SerializedName("node")
                var node: Int?,
                @SerializedName("startingWith")
                var startingWith: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10013(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10039(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10042(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10046(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10076(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X10163(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X13(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("nativeToken")
                var nativeToken: Int?,
                @SerializedName("networkId")
                var networkId: Int?,
                @SerializedName("node")
                var node: Int?,
                @SerializedName("startingWith")
                var startingWith: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X2(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("nativeToken")
                var nativeToken: Int?,
                @SerializedName("networkId")
                var networkId: Int?,
                @SerializedName("node")
                var node: Int?,
                @SerializedName("startingWith")
                var startingWith: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X3(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("nativeToken")
                var nativeToken: Int?,
                @SerializedName("networkId")
                var networkId: Int?,
                @SerializedName("node")
                var node: Int?,
                @SerializedName("startingWith")
                var startingWith: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X4(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("nativeToken")
                var nativeToken: Int?,
                @SerializedName("networkId")
                var networkId: Int?,
                @SerializedName("node")
                var node: Int?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X5(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("node")
                var node: Int?,
                @SerializedName("startingWith")
                var startingWith: List<String?>?
            ) : Parcelable

            @Keep
            @Parcelize
            data class X8(
                @SerializedName("addressRegex")
                var addressRegex: String?,
                @SerializedName("chainName")
                var chainName: String?,
                @SerializedName("chainSymbol")
                var chainSymbol: String?,
                @SerializedName("hashLink")
                var hashLink: String?,
                @SerializedName("memoRegex")
                var memoRegex: String?,
                @SerializedName("node")
                var node: Int?
            ) : Parcelable
        }
    }
}