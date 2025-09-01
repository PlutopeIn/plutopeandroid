package com.app.plutope.model

import com.app.plutope.utils.constant.typeBuy
import com.app.plutope.utils.constant.typeSell

sealed class BuyCrypto {
    sealed class Domain {
        data class MELD(
            val countryCode: String,
            val sourceAmount: String,
            val sourceCurrencyCode: String,
            val destinationCurrencyCode: String,
            val walletAddress: String,
            val pubKey: String,
            val network: String,
            val tokenAddress: String? = "",
        ) : Domain() {


            val getFormatedCurrency: String
                get() {
                    return if (tokenAddress != "") {
                        when (network.uppercase()) {
                            "POLYGON" -> "${destinationCurrencyCode}_POLYGON"
                            "MATIC20" -> "${destinationCurrencyCode}_POLYGON"
                            "BEP20" -> "${destinationCurrencyCode}_BSC"
                            "ERC20" -> "${destinationCurrencyCode}_ETH"
                            "KIP20" -> destinationCurrencyCode
                            else -> destinationCurrencyCode
                        }
                    } else {
                        when (network.uppercase()) {
                            "POLYGON" -> destinationCurrencyCode
                            "BEP20" -> "${destinationCurrencyCode}_BSC"
                            "ERC20" -> destinationCurrencyCode
                            "KIP20" -> destinationCurrencyCode
                            else -> destinationCurrencyCode
                        }
                    }
                }


        }

        data class OnRamp(
            val coinCode: String,
            val walletAddress: String,
            val fiatAmount: String,
            val fiatType: String? = "1",
            val network: String? = "",
            var isBuyOrSellType: String? = "buy"

        ) : Domain()

        data class ChangeNow(
            val name: String,
            val from: String,
            val to: String,
            val fiatMode: String,
            val amount: String,
            val recipientAddress: String
        ) : Domain()

        data class OnMeta(
            val name: String,
            val apiKey: String,
            val walletAddress: String,
            val fiatAmount: String,
            val chainId: String,
            val tokenAddress: String?,
            var isBuyOrSellType: String? = "buy",
            var tokenSymbol:String?=""
        ) : Domain()

        data class Alchemypay(
            val appId: String,
            val crypto: String,
            val network: String,
            val fiat: String,
            val country: String,
            val cryptoAmount: String,
            val callbackUrl: String?,
            val walletAddress: String
        ) : Domain()

        data class Unlimit(
            val merchantId: String,
            val cryptoCurrency: String,
            val network: String,
            val fiatAmount: String,
            val currency: String,
            val myWalletAddress: String

        ) : Domain()

        data class Rampable(
            val clientSecret: String = "wpyYO6EyVSwx3QGY50d0VHCICTjiBHTTRGo7zbL6G6bxBtCSaGBrEbRB70ZhzdvP",
            var selectRecipient: Boolean = true,
            val currency: String = "USD",
            val inputCurrency: String = "usdc-polygon",
            val outputCurrency: String,
            val inputAmount: String,
            val outputAmount: String,
            val useWalletConnect: Boolean = true

        ) : Domain()


    }

    companion object {

        fun buildURL(forDomain: Domain): String {
            return when (forDomain) {
                is Domain.MELD -> { "https://www.fluidmoney.xyz?publicKey=${forDomain.pubKey}&countryCodeLocked=${forDomain.countryCode}&sourceAmountLocked=${forDomain.sourceAmount}&sourceCurrencyCodeLocked=${forDomain.sourceCurrencyCode}&destinationCurrencyCodeLocked=${forDomain.getFormatedCurrency}&walletAddressLocked=${forDomain.walletAddress}" }
                is Domain.OnRamp -> "https://onramp.money/main/${forDomain.isBuyOrSellType?.lowercase()}/?appId=337391&coinCode=${forDomain.coinCode}&walletAddress=${forDomain.walletAddress}&fiatAmount=${forDomain.fiatAmount}&fiatType=${forDomain.fiatType}&network=${forDomain.network}"
                is Domain.ChangeNow -> "https://changenow.io/exchange?from=${forDomain.from}&to=${forDomain.to}&fiatMode=${forDomain.fiatMode}&amount=${forDomain.amount}&recipientAddress=${forDomain.recipientAddress}"
                is Domain.OnMeta -> {
                    val isOnRamp: String =
                        if (forDomain.isBuyOrSellType?.lowercase() == typeBuy.lowercase()) "enabled" else "disabled"
                    val isOffRamp: String =
                        if (forDomain.isBuyOrSellType?.lowercase() == typeSell.lowercase()) "enabled" else "disabled"

                    "https://plutope.app/api/on-meta?walletAddress=${forDomain.walletAddress}&tokenSymbol=${forDomain.tokenSymbol}&tokenAddress=${forDomain.tokenAddress.orEmpty()}&chainId=${forDomain.chainId}&fiatAmount=${forDomain.fiatAmount}&offRamp=$isOffRamp&onRamp=$isOnRamp"

                }

                is Domain.Alchemypay -> {
                    "https://ramptest.alchemypay.org/?appId=${forDomain.appId}&crypto=${forDomain.crypto}&network=${forDomain.network}&fiat=${forDomain.fiat}&country=${forDomain.country}&cryptoAmount=${forDomain.cryptoAmount}&address=${forDomain.walletAddress}"//&callbackUrl=https://localhost:9090/test/test&withdrawUrl=www.baidu.com&type=sell&source=3&urlType=web&merchantName=AELF&merchantOrderNo=2134545343544334&showAddress=N#/sell-formUserInfo"
                }

                is Domain.Unlimit -> {
                    "https://onramp.gatefi.com/?merchantId=${forDomain.merchantId}&cryptoCurrency=${forDomain.cryptoCurrency}-${forDomain.network}&fiatAmount=${forDomain.fiatAmount}&fiatCurrency=${forDomain.currency}&wallet=${forDomain.myWalletAddress}"
                }

                is Domain.Rampable -> {
                    "https://webview-dev.rampable.co/?clientSecret=${forDomain.clientSecret}&selectRecipient=${forDomain.selectRecipient}&currency=${forDomain.currency}&inputCurrency=${forDomain.inputCurrency}&outputCurrency=${forDomain.outputCurrency}&inputAmount=${forDomain.inputAmount}&outputAmount=${forDomain.outputAmount}&useWalletConnect=${forDomain.useWalletConnect}"
                }

                else -> {
                    ""
                }
            }
        }


    }
}

enum class CoinCode {
    MELD, CHANGENOW, ONMETA, ONRAMP, ALCHEMYPAY, UNLIMIT, OKX, RANGO, RAMPABLE,EXODUS
}