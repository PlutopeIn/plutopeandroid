package com.app.plutope.networkConfig

import androidx.annotation.Keep
import com.app.plutope.R
import com.app.plutope.model.CoinCode
import com.app.plutope.model.TokenList
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.UNLIMIT
import com.app.plutope.utils.loadJSONFromRaw
import com.google.gson.Gson
import java.math.BigInteger


enum class NetworkType {
    Mainnet,
    Testnet
}

@Keep
sealed class Chain : ChainDetails {

    object Ethereum : Chain() {
        override var coinType: CoinType = CoinType.ETHEREUM
        override var name: String = "Ethereum"
        override var symbol: String = "ETH"
        override var tokenStandard: String = "ERC20"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "1" else "5"
        override var icon: String = "https://cryptologos.cc/logos/ethereum-eth-logo.png?v=025"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "wss://ethereum.publicnode.com" /* "https://eth.llamarpc.com"*/ else "https://goerli.blockpi.network/v1/rpc/public"
        override var walletAddress: String? = getWalletAddress(Polygon.coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(Polygon.coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.eth_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(Ethereum)
        override var chainName: String = getChainName(Ethereum)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Ethereum)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "ethereum"


    }

    object BinanceSmartChain : Chain() {
        override var coinType: CoinType = CoinType.SMARTCHAIN
        override var name: String = "BNB Smart Chain"
        override var symbol: String = "BNB"
        override var tokenStandard: String = "BEP20"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "56" else "97"
        override var icon: String =
            "https://assets.coingecko.com/coins/images/825/small/binance-coin-logo.png?1547034615"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://bsc-mainnet.nodereal.io/v1/64a9df0874fb4a93b9d0a3849de012d3" else "https://bsc-testnet.publicnode.com"
        override var walletAddress: String? = getWalletAddress(Polygon.coinType)
        override var privateKey: String? = Wallet.getPrivateKeyData(Polygon.coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.bsc_tokenlist)
        override var providers: List<ProviderModel> =
            getProvidersList(BinanceSmartChain)
        override var chainName: String = getChainName(BinanceSmartChain)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(BinanceSmartChain)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "smartchain"
    }

    object Polygon : Chain() {
        override var coinType: CoinType = CoinType.POLYGON
        override var name: String = "Polygon"
        override var symbol: String = "MATIC"
        override var tokenStandard: String = "POLYGON"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "137" else "80001"
        override var icon: String = "https://i.imgur.com/uIExoAr.png"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://polygon-rpc.com/" else "https://polygon-mumbai.infura.io/v3/4458cf4d1689497b9a38b1d6bbf05e78"
        override var walletAddress: String? =
            getWalletAddress(coinType)//Wallet.getPublicWalletAddress(coinType)
        override var privateKey: String? = Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.polygon_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(Polygon)
        override var chainName: String = getChainName(Polygon)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Polygon)
        override var minGasLimit: BigInteger = 96000.toBigInteger()
        override var chainForTrustWallet: String = "polygon"
    }

    object OKC : Chain() {
        override var coinType: CoinType = CoinType.OKXCHAIN
        override var name: String = "OKschain"
        override var symbol: String = "OKT"
        override var tokenStandard: String = "KIP20"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "66" else "65"
        override var icon: String = "https://stakingcrypto.info/static/assets/coins/okt-logo.png"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://exchainrpc.okex.org" else "https://exchaintestrpc.okex.org"
        override var walletAddress: String? = getWalletAddress(Polygon.coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(Polygon.coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.okx_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(OKC)
        override var chainName: String = getChainName(OKC)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(OKC)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "okschain"

    }


    object Bitcoin : Chain() {
        override var coinType: CoinType = CoinType.BITCOIN
        override var name: String = "Bitcoin"
        override var symbol: String = "BTC"
        override var tokenStandard: String =
            "" // Bitcoin does not have a token standard like ERC20 or BEP20
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "1" else "5"
        override var icon: String =
            "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1696501400"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://bitcoin-mainnet-archive.allthatnode.com/" else "https://bitcoin-testnet-archive.allthatnode.com"
        override var walletAddress: String? = getWalletAddress(Bitcoin.coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(Bitcoin.coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = /*loadJson(R.raw.eth_tokenlist)*/ arrayListOf()
        override var providers: List<ProviderModel> = getProvidersList(Bitcoin)
        override var chainName: String = getChainName(Bitcoin)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Bitcoin)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "bitcoin"


    }


}

private fun loadJson(filename: Int): List<Tokens> {
    val jsonString = loadJSONFromRaw(filename)// load the JSON from file or network
    val tokenList = Gson().fromJson(jsonString, TokenList::class.java)
    return tokenList.tokens
}

interface ChainDetails {
    var name: String
    var symbol: String
    var tokenStandard: String
    var chainIdHex: String
    var icon: String
    var walletAddress: String?
    var privateKey: String?
    var coinType: CoinType
    var decimals: Int
    var rpcURL: String
    var tokens: List<Tokens>
    var providers: List<ProviderModel>
    var chainName: String
    var sellProviders: List<ProviderModel>
    var minGasLimit: BigInteger
    var chainForTrustWallet: String
}

fun getChainName(
    chain: Chain
): String {
    return when (chain) {
        Chain.Ethereum -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "eth" else "goerli"
        }

        Chain.BinanceSmartChain -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "bsc" else "bsc testnet"
        }

        Chain.Polygon -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "polygon" else "mumbai"
        }

        Chain.OKC -> {
            "okc"
        }

        Chain.Bitcoin -> {
            "btc"
        }

        else -> {
            ""
        }
    }
}


fun getProvidersList(
    chain: Chain
): MutableList<ProviderModel> {
    val list: MutableList<ProviderModel> = mutableListOf()


    //  Log.e("ChainDate", "getProvidersList: ${pref.getSelectedCurrency()?.code}")

    when (chain) {
        Chain.Ethereum -> {
            list.add(ProviderModel("Meld", CoinCode.MELD, icon = R.drawable.ic_meld))
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )
            list.add(ProviderModel("On Meta", CoinCode.ONMETA, icon = R.drawable.ic_on_meta))
            list.add(ProviderModel("On Ramp", CoinCode.ONRAMP, icon = R.drawable.ic_on_ramp))
            list.add(
                ProviderModel(
                    "Alchemy PAy",
                    CoinCode.ALCHEMYPAY,
                    icon = R.drawable.ic_on_ramp
                )
            )

            list.add(ProviderModel(UNLIMIT, CoinCode.UNLIMIT, icon = R.drawable.img_unlimit_logo))


        }

        Chain.BinanceSmartChain -> {
            list.add(ProviderModel("Meld", CoinCode.MELD, icon = R.drawable.ic_meld))
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )

            list.add(ProviderModel("On Meta", CoinCode.ONMETA, icon = R.drawable.ic_on_meta))
            list.add(ProviderModel("On Ramp", CoinCode.ONRAMP, icon = R.drawable.ic_on_ramp))
            list.add(
                ProviderModel(
                    "Alchemy PAy",
                    CoinCode.ALCHEMYPAY,
                    icon = R.drawable.ic_on_ramp
                )
            )

            list.add(ProviderModel(UNLIMIT, CoinCode.UNLIMIT, icon = R.drawable.img_unlimit_logo))


        }

        Chain.Polygon -> {
            list.add(ProviderModel("Meld", CoinCode.MELD, icon = R.drawable.ic_meld))
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )
            list.add(ProviderModel("On Meta", CoinCode.ONMETA, icon = R.drawable.ic_on_meta))
            list.add(ProviderModel("On Ramp", CoinCode.ONRAMP, icon = R.drawable.ic_on_ramp))
            list.add(
                ProviderModel(
                    "Alchemy PAy",
                    CoinCode.ALCHEMYPAY,
                    icon = R.drawable.ic_on_ramp
                )
            )

            list.add(ProviderModel(UNLIMIT, CoinCode.UNLIMIT, icon = R.drawable.img_unlimit_logo))

        }

        Chain.OKC -> {
            list.add(ProviderModel("Meld", CoinCode.MELD, icon = R.drawable.ic_meld))
        }

        Chain.Bitcoin -> {
            list.add(ProviderModel("Meld", CoinCode.MELD, icon = R.drawable.ic_meld))
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )
            list.add(ProviderModel("On Meta", CoinCode.ONMETA, icon = R.drawable.ic_on_meta))
            list.add(ProviderModel("On Ramp", CoinCode.ONRAMP, icon = R.drawable.ic_on_ramp))
            list.add(
                ProviderModel(
                    "Alchemy PAy",
                    CoinCode.ALCHEMYPAY,
                    icon = R.drawable.ic_on_ramp
                )
            )

            list.add(ProviderModel(UNLIMIT, CoinCode.UNLIMIT, icon = R.drawable.img_unlimit_logo))
        }

        else -> {}
    }
    return list
}


fun getSellProvidersList(
    chain: Chain
): MutableList<ProviderModel> {
    val list: MutableList<ProviderModel> = mutableListOf()
    when (chain) {
        Chain.Ethereum -> {
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )
            list.add(ProviderModel("On Meta", CoinCode.ONMETA, icon = R.drawable.ic_on_meta))
            list.add(ProviderModel("On Ramp", CoinCode.ONRAMP, icon = R.drawable.ic_on_ramp))
            list.add(
                ProviderModel(
                    "Alchemy PAy",
                    CoinCode.ALCHEMYPAY,
                    icon = R.drawable.ic_on_ramp
                )
            )

        }

        Chain.BinanceSmartChain -> {
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )
            list.add(ProviderModel("On Meta", CoinCode.ONMETA, icon = R.drawable.ic_on_meta))
            list.add(ProviderModel("On Ramp", CoinCode.ONRAMP, icon = R.drawable.ic_on_ramp))
            list.add(
                ProviderModel(
                    "Alchemy PAy",
                    CoinCode.ALCHEMYPAY,
                    icon = R.drawable.ic_on_ramp
                )
            )
        }

        Chain.Polygon -> {
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )
            list.add(ProviderModel("On Meta", CoinCode.ONMETA, icon = R.drawable.ic_on_meta))
            list.add(ProviderModel("On Ramp", CoinCode.ONRAMP, icon = R.drawable.ic_on_ramp))
            list.add(
                ProviderModel(
                    "Alchemy PAy",
                    CoinCode.ALCHEMYPAY,
                    icon = R.drawable.ic_on_ramp
                )
            )
        }

        Chain.OKC -> {

        }

        Chain.Bitcoin -> {
            list.add(
                ProviderModel(
                    "Change Now",
                    CoinCode.CHANGENOW,
                    icon = R.drawable.ic_change_now
                )
            )


        }

        else -> {

        }
    }
    return list
}

fun getWalletAddress(coinType: CoinType): String? {
    Wallet.refreshWallet()
    return Wallet.getPublicWalletAddress(coinType)
}


enum class Chains(
    var mainName: String = "",
    var coinType: CoinType = CoinType.ETHEREUM,
    val chainName: String = "",
    val chainNamespace: String = "",
    val chainReference: String = "",
    val icon: String = "",
    var symbol: String = "OKT",
    var type: String = "",
    val tokenAddress: String = "",
    var walletAddress: String = "",
    val order: Int = 1,
    var currentPrice: String = "0",
    val chainId: String = "$chainNamespace:$chainReference"
) {

    ETHEREUM(
        mainName = Chain.Ethereum.name,
        coinType = Chain.Ethereum.coinType,
        chainName = Chain.Ethereum.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.Ethereum.chainIdHex,
        icon = Chain.Ethereum.icon,
        symbol = Chain.Ethereum.symbol,
        type = Chain.Ethereum.tokenStandard,
        walletAddress = getWalletAddress(Chain.Ethereum.coinType)!!,
        currentPrice = "0",
        order = 1
    ),

    BNB(
        mainName = Chain.BinanceSmartChain.name,
        coinType = Chain.BinanceSmartChain.coinType,
        chainName = Chain.BinanceSmartChain.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.BinanceSmartChain.chainIdHex,
        icon = Chain.BinanceSmartChain.icon,
        symbol = Chain.BinanceSmartChain.symbol,
        type = Chain.BinanceSmartChain.tokenStandard,
        walletAddress = getWalletAddress(Chain.BinanceSmartChain.coinType)!!,
        currentPrice = "0",
        order = 2
    ),

    POLYGON(
        mainName = Chain.Polygon.name,
        coinType = Chain.Polygon.coinType,
        chainName = Chain.Polygon.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.Polygon.chainIdHex,
        icon = Chain.Polygon.icon,
        symbol = Chain.Polygon.symbol,
        type = Chain.Polygon.tokenStandard,
        walletAddress = getWalletAddress(Chain.Polygon.coinType)!!,
        currentPrice = "0",
        order = 3
    ),

    OKC(
        mainName = Chain.OKC.name,
        coinType = Chain.OKC.coinType,
        chainName = Chain.OKC.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.OKC.chainIdHex,
        icon = Chain.OKC.icon,
        symbol = Chain.OKC.symbol,
        type = Chain.OKC.tokenStandard,
        walletAddress = getWalletAddress(Chain.OKC.coinType)!!,
        currentPrice = "0",
        order = 4
    ),
}








