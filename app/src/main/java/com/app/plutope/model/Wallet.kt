package com.app.plutope.model


import android.util.Log
import com.app.plutope.ui.base.App
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.utils.Securities
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.logd
import com.app.plutope.utils.loge
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Singleton


@Singleton
object Wallet : BaseActivity(){

    var wallet: MyWallet? = null
    private var walletBTC: MyWallet? = null
    private var walletTron: MyWallet? = null



    lateinit var prefHelper: PreferenceHelper
    var walletObject: Wallets

    private const val chainETH = "ETH"
    private const val chainBTC = "BTC"
    private const val chainTRX = "TRX"

    private fun parseWalletJson(walletJson: String): MyWallet {
        val jsonObject: JsonObject = Gson().fromJson(walletJson, JsonObject::class.java)
        return MyWallet(
            address = jsonObject.get("address").asString,
            privateKey = jsonObject.get("privateKey").asString
        )
    }

    private fun parseTronWalletJson(walletJson: String): MyWallet {

       // tronweb?.onCompleted

       // loge("walletJson","walletJson=> $walletJson")

       // this.importAccountFromMnemonicAction(walletJson)

        var tronAddress = ""
        var tronPrivateKey = ""


        tronweb?.importAccountFromMnemonic(walletJson, onCompleted = { state: Boolean, address: String, privateKey: String, publicKey: String, error: String ->
            tronAddress = address
            tronPrivateKey = privateKey

            loge("importAccountFromMnemonicAction","address : $address")
            loge("importAccountFromMnemonicAction","privateKey : $privateKey")
            loge("importAccountFromMnemonicAction","publicKey : $publicKey")
            loge("importAccountFromMnemonicAction","error : $error")


            MyWallet(
                address = tronAddress,
                privateKey = tronPrivateKey
            )


        })


       /* val mnemonicList = walletJson.split(" ")
        val seed = MnemonicUtils.generateSeed(walletJson, null)
        val seedPhrase = DeterministicSeed(mnemonicList, null, "", 0)
        val keyChain = DeterministicKeyChain.builder().seed(seedPhrase).build()
        val bip44Path = HDUtils.parsePath("M/44H/195H/0H/0/0")
        val key = keyChain.getKeyByPath(bip44Path, true)

        val privateKeyHex = Numeric.toHexStringNoPrefix(key.privKey)
        val ecKey = ECKey.fromPrivate(key.privKeyBytes)

        // Get public key in hex format
        val publicKeyHex = Numeric.toHexStringNoPrefix(ecKey.pubKey)

        loge("walletJson","publicKeyHex => $publicKeyHex")*/

        return MyWallet(
            address = tronAddress,
            privateKey = tronPrivateKey
        )
    }


    init {

        try {
            prefHelper = PreferenceHelper(App.getContext())
            wallet = parseWalletJson(
                CGWallet.CGWallet.generateWallet(
                    (prefHelper.menomonicWallet.trimStart()).trimEnd(),
                    chainETH
                )
            )
            walletBTC = parseWalletJson(
                CGWallet.CGWallet.generateWallet(
                    (prefHelper.menomonicWallet.trimStart()).trimEnd(),
                    chainBTC
                )
            )

            walletTron = parseTronWalletJson(
                prefHelper.menomonicWallet.trimStart().trimEnd()
            )

            walletObject = Wallets(
                w_isprimary = 1,
                w_mnemonic = (prefHelper.menomonicWallet.trimStart()).trimEnd(),
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
        return if (coinType == CoinType.BITCOIN) walletBTC?.privateKey.toString() else if (coinType == CoinType.TRON) walletTron?.privateKey.toString() else wallet?.privateKey.toString()
    }

    fun getPublicWalletAddress(coinType: CoinType): String? {
        logd("CurrentWallet", "${wallet?.address}")
        logd("CurrentWalletBTC", "${walletBTC?.address}")
        return if (coinType == CoinType.BITCOIN) walletBTC?.address else if (coinType == CoinType.TRON) walletTron?.address else wallet?.address
    }
    private fun getWalletFromMnemonic(mnemonic: String, coinType: CoinType): MyWallet {
        val chain = if (coinType == CoinType.BITCOIN) chainBTC else if (coinType == CoinType.TRON) chainTRX else chainETH
        val walletJson = CGWallet.CGWallet.generateWallet(mnemonic.trim(), chain)
        return parseWalletJson(walletJson)
    }

    fun getPublicWalletAddressFromMnemonic(mnemonic: String, coinType: CoinType): String {
        val wallet = getWalletFromMnemonic(mnemonic, coinType)
        return wallet.address
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
                (Securities.decrypt(niks).trimStart()).trimEnd(),
                chainETH
            )
        )
        walletBTC = parseWalletJson(
            CGWallet.CGWallet.generateWallet(
                ((Securities.decrypt(niks)).trimStart()).trimEnd(),
                chainBTC
            )
        )

        walletTron = parseTronWalletJson(
            (Securities.decrypt(niks)).trimStart().trimEnd()
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
                (Securities.decrypt(niks).trimStart()).trimEnd(),
                chainETH
            )
        )//HDWallet(Wallet.walletObject.w_mnemonic?.trimStart()?.trimEnd(), "")
        walletBTC = parseWalletJson(
            CGWallet.CGWallet.generateWallet(
                (Securities.decrypt(niks).trimStart()).trimEnd(),
                chainBTC
            )
        )//HDWallet(Wallet.walletObject.w_mnemonic?.trimStart()?.trimEnd(), "")

        walletTron = parseTronWalletJson(
            (Securities.decrypt(niks)).trimStart().trimEnd()
        )

    }
    data class MyWallet(val address: String, val privateKey: String)

}



