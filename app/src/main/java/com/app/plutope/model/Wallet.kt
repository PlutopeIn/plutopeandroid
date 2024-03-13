package com.app.plutope.model


import android.util.Log
import com.app.plutope.ui.base.App
import com.app.plutope.utils.Securities
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.loge
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Singleton


@Singleton
object Wallet{

    var wallet: MyWallet? = null
    private var walletBTC: MyWallet? = null


    lateinit var prefHelper: PreferenceHelper
    var walletObject: Wallets

    private const val chainETH = "ETH"
    private const val chainBTC = "BTC"

    private fun parseWalletJson(walletJson: String): MyWallet {
        val jsonObject: JsonObject = Gson().fromJson(walletJson, JsonObject::class.java)
        return MyWallet(
            address = jsonObject.get("address").asString,
            privateKey = jsonObject.get("privateKey").asString
        )
    }

    init {

        try {
            prefHelper = PreferenceHelper(App.getContext())
            wallet = parseWalletJson(
                CGWallet.CGWallet.generateWallet(
                    (prefHelper.menomonicWallet?.trimStart())?.trimEnd(),
                    chainETH
                )
            )
            walletBTC = parseWalletJson(
                CGWallet.CGWallet.generateWallet(
                    (prefHelper.menomonicWallet?.trimStart())?.trimEnd(),
                    chainBTC
                )
            )

            walletObject = Wallets(
                w_isprimary = 1,
                w_mnemonic = (prefHelper.menomonicWallet?.trimStart())?.trimEnd() ?: "",
                w_wallet_name = "", w_wallet_last_balance = ""
            )
        } catch (e: Exception) {
            Log.e("Wallet", "Wallet here")
            // Handle the exception, log an error, or perform any necessary actions
            e.printStackTrace()
            // Assign a default value or handle the error case accordingly
            wallet = parseWalletJson(CGWallet.CGWallet.generateWallet("", ""))
            walletObject = Wallets(
                w_isprimary = 0,
                w_mnemonic = "",
                w_wallet_name = "",
                w_wallet_last_balance = ""
            )
        }
    }

    fun getPrivateKeyData(coinType: CoinType): String {
        return if (coinType == CoinType.BITCOIN) walletBTC?.privateKey.toString() else wallet?.privateKey.toString()
    }

    fun getPublicWalletAddress(coinType: CoinType): String? {
        loge("CurrentWallet", "${wallet?.address}")
        return if (coinType == CoinType.BITCOIN) walletBTC?.address /*"bc1qs8fwrd8arcu0yrme9xy9hvhz34ryuj3msnxw9s"*/ else wallet?.address
    }


    fun setWalletObjectFromInstance(wallet1: Wallets) {
        walletObject = wallet1
        loge(message = "${wallet1.w_mnemonic}")

        var niks = ""

        niks = if (prefHelper.menomonicWallet != "") {
            if (prefHelper.appUpdatedFlag == "") {
                //Securities.encrypt(wallet1.w_mnemonic)
                wallet1.w_mnemonic!!
            } else {
                wallet1.w_mnemonic!!
            }
        } else {
            wallet1.w_mnemonic!!
        }

        // val nikstt = if (prefHelper.menomonicWallet != "" && prefHelper.appUpdatedFlag == "") Securities.encrypt(wallet1.w_mnemonic) else wallet1.w_mnemonic


        wallet = parseWalletJson(
            CGWallet.CGWallet.generateWallet(
                (Securities.decrypt(niks)?.trimStart())?.trimEnd(),
                chainETH
            )
        )
        walletBTC = parseWalletJson(
            CGWallet.CGWallet.generateWallet(
                ((Securities.decrypt(niks))?.trimStart())?.trimEnd(),
                chainBTC
            )
        )
    }

    fun getInstance(): Wallet {
        return this
    }

    fun refreshWallet() {
        // Wallet.wallet = HDWallet(Wallet.walletObject.w_mnemonic?.trimStart()?.trimEnd(), "")
        val niks =
            if (prefHelper.menomonicWallet != "" && prefHelper.appUpdatedFlag == "") walletObject.w_mnemonic /*Securities.encrypt(walletObject.w_mnemonic)*/ else walletObject.w_mnemonic
        wallet = parseWalletJson(
            CGWallet.CGWallet.generateWallet(
                (Securities.decrypt(niks)?.trimStart())?.trimEnd(),
                chainETH
            )
        )//HDWallet(Wallet.walletObject.w_mnemonic?.trimStart()?.trimEnd(), "")
        walletBTC = parseWalletJson(
            CGWallet.CGWallet.generateWallet(
                (Securities.decrypt(niks).trimStart())?.trimEnd(),
                chainBTC
            )
        )//HDWallet(Wallet.walletObject.w_mnemonic?.trimStart()?.trimEnd(), "")

    }
    data class MyWallet(val address: String, val privateKey: String)

}



