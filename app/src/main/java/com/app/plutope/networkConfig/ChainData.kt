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
            if (DataStore.networkEnv == NetworkType.Mainnet) "1" else "11155111"
        override var icon: String = "https://cryptologos.cc/logos/ethereum-eth-logo.png?v=025"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://mainnet.infura.io/v3/04a80aa5bc014c95b0ff68d784f97ab4" /*"wss://ethereum.publicnode.com"*/ /* "https://eth.llamarpc.com"*/ else "https://ethereum-sepolia-rpc.publicnode.com"
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
        override var privateKey: String? =
            Wallet.getPrivateKeyData(Polygon.coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
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
        override var symbol: String = "POL"
        override var tokenStandard: String = "POL"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "137" else "80001"
        override var icon: String = "https://i.imgur.com/uIExoAr.png"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://polygon-rpc.com/" else "https://polygon-testnet.public.blastapi.io"
        override var walletAddress: String? =
            getWalletAddress(coinType)//Wallet.getPublicWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
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
            if (DataStore.networkEnv == NetworkType.Mainnet) "0" else "5"
        override var icon: String =
            "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1696501400"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://bitcoin-mainnet-archive.allthatnode.com/" else "https://bitcoin-testnet-archive.allthatnode.com"
        override var walletAddress: String? = getWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = /*loadJson(R.raw.eth_tokenlist)*/ arrayListOf()
        override var providers: List<ProviderModel> = getProvidersList(Bitcoin)
        override var chainName: String = getChainName(Bitcoin)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Bitcoin)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "bitcoin"


    }


    object Optimism : Chain() {
        override var coinType: CoinType = CoinType.OPTIMISM
        override var name: String = "ETH"
        override var symbol: String = "OP"
        override var tokenStandard: String = "ETH"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "10" else "420"
        override var icon: String =
            "https://coin-images.coingecko.com/coins/images/25244/large/Optimism.png?1696524385"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://optimism-rpc.publicnode.com" /*"wss://ethereum.publicnode.com"*/ /* "https://eth.llamarpc.com"*/ else "https://optimism-goerli.public.blastapi.io"
        override var walletAddress: String? = getWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.optimism_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(Optimism)
        override var chainName: String = getChainName(Optimism)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Optimism)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "optimism"
    }

    object Avalanche : Chain() {
        override var coinType: CoinType = CoinType.AVALANCHECCHAIN
        override var name: String = "Avalanche C-Chain"
        override var symbol: String = "AVAX"
        override var tokenStandard: String = "AVAX"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "43114" else "43113"
        override var icon: String = "https://icons.llamao.fi/icons/chains/rsz_avalanche.jpg"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://avalanche-c-chain-rpc.publicnode.com" else "https://avalanche.drpc.org"
        override var walletAddress: String? = getWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.optimism_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(Avalanche)
        override var chainName: String = getChainName(Avalanche)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Avalanche)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "avalanche"

    }

    object Arbitrum : Chain() {
        override var coinType: CoinType = CoinType.ARBITRUM
        override var name: String = "ETH"
        override var symbol: String = "ARB"
        override var tokenStandard: String = "ETH"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "42161" else "421614"
        override var icon: String =
            "https://assets.coingecko.com/coins/images/16547/large/photo_2023-03-29_21.47.00.jpeg?1680097630"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "wss://arbitrum.callstaticrpc.com" else "https://endpoints.omniatech.io/v1/arbitrum/sepolia/public"
        override var walletAddress: String? = getWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.optimism_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(Arbitrum)
        override var chainName: String = getChainName(Arbitrum)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Arbitrum)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "arbitrum"

    }

    object Tron : Chain() {
        override var coinType: CoinType = CoinType.TRON
        override var name: String = "Tron"
        override var symbol: String = "TRX"
        override var tokenStandard: String = "TRC20"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "1000" else "1001"
        override var icon: String = "https://s2.coinmarketcap.com/static/img/coins/64x64/1958.png"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://tron-rpc.publicnode.com" else "https://endpoints.omniatech.io/v1/arbitrum/sepolia/public"
        override var walletAddress: String? = getWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.optimism_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(Tron)
        override var chainName: String = getChainName(Tron)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Tron)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "tron"

    }

    object Solana : Chain() {
        override var coinType: CoinType = CoinType.SOLANA
        override var name: String = "Solana"
        override var symbol: String = "SOL"
        override var tokenStandard: String = "Solana"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "900" else "901"
        override var icon: String =
            "https://s2.coinmarketcap.com/static/img/coins/64x64/5426.png"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://solana-rpc.publicnode.com" else "https://solana-rpc.publicnode.com"
        override var walletAddress: String? = getWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.optimism_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(Solana)
        override var chainName: String = getChainName(Solana)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(Solana)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "solana"

    }

    object BaseMainnet : Chain() {
        override var coinType: CoinType = CoinType.BASEMAINNET
        override var name: String = "Base Mainnet"
        override var symbol: String = "base"
        override var tokenStandard: String = "base"
        override var chainIdHex: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "8453" else "84532"
        override var icon: String =
            "https://s2.coinmarketcap.com/static/img/coins/64x64/5426.png"
        override var decimals: Int = 18
        override var rpcURL: String =
            if (DataStore.networkEnv == NetworkType.Mainnet) "https://base-rpc.publicnode.com" else "https://base-sepolia-rpc.publicnode.com"
        override var walletAddress: String? = getWalletAddress(coinType)
        override var privateKey: String? =
            Wallet.getPrivateKeyData(coinType)//Wallet.getPrivateKeyData(coinType)?.toHexString()
        override var tokens: List<Tokens> = loadJson(R.raw.optimism_tokenlist)
        override var providers: List<ProviderModel> = getProvidersList(BaseMainnet)
        override var chainName: String = getChainName(BaseMainnet)
        override var sellProviders: List<ProviderModel> = getSellProvidersList(BaseMainnet)
        override var minGasLimit: BigInteger = 21000.toBigInteger()
        override var chainForTrustWallet: String = "base"

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
            if (DataStore.networkEnv == NetworkType.Mainnet) "eth" else "sepolia_testnet"
        }

        Chain.BinanceSmartChain -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "bsc" else "bsc_testnet"
        }

        Chain.Polygon -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "polygon" else "mumbai_testnet"
        }

        Chain.OKC -> {
            "okc"
        }

        Chain.Bitcoin -> {
            "btc"
        }

        Chain.Optimism -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "optimism" else "op_testnet"
        }

        Chain.Avalanche -> {
            //if (DataStore.networkEnv == NetworkType.Mainnet) "avaxc" else "avaxc_testnet"
            if (DataStore.networkEnv == NetworkType.Mainnet) "Avalanche" else "avaxc_testnet"
        }

        Chain.Arbitrum -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "arbitrum" else "arb_testnet"
        }

        Chain.Tron -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "tron" else "tron_testnet"
        }

        Chain.Solana -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "sol" else "sol_testnet"
        }

        Chain.BaseMainnet -> {
            if (DataStore.networkEnv == NetworkType.Mainnet) "base" else "base_testnet"
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
    val chainId: String = "$chainNamespace:$chainReference",
    var chainForTrustWallet: String = "",
    var rpcUrl: String = ""
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
        chainForTrustWallet = Chain.Ethereum.chainForTrustWallet,
        rpcUrl = Chain.Ethereum.rpcURL,
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
        chainForTrustWallet = Chain.BinanceSmartChain.chainForTrustWallet,
        rpcUrl = Chain.BinanceSmartChain.rpcURL,
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
        chainForTrustWallet = Chain.Polygon.chainForTrustWallet,
        rpcUrl = Chain.Polygon.rpcURL,
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
        chainForTrustWallet = Chain.OKC.chainForTrustWallet,
        rpcUrl = Chain.OKC.rpcURL,
        order = 4
    ),
    Bitcoin(
        mainName = Chain.Bitcoin.name,
        coinType = Chain.Bitcoin.coinType,
        chainName = Chain.Bitcoin.chainName,
        chainNamespace = "bitcoin",
        chainReference = Chain.Bitcoin.chainIdHex,
        icon = Chain.Bitcoin.icon,
        symbol = Chain.Bitcoin.symbol,
        type = Chain.Bitcoin.tokenStandard,
        walletAddress = getWalletAddress(Chain.Bitcoin.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.Bitcoin.chainForTrustWallet,
        rpcUrl = Chain.Bitcoin.rpcURL,
        order = 5
    ),

    Optimism(
        mainName = Chain.Optimism.name,
        coinType = Chain.Optimism.coinType,
        chainName = Chain.Optimism.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.Optimism.chainIdHex,
        icon = Chain.Optimism.icon,
        symbol = Chain.Optimism.symbol,
        type = Chain.Optimism.tokenStandard,
        walletAddress = getWalletAddress(Chain.Optimism.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.Optimism.chainForTrustWallet,
        rpcUrl = Chain.Optimism.rpcURL,
        order = 6
    ),

    Avalanche(
        mainName = Chain.Avalanche.name,
        coinType = Chain.Avalanche.coinType,
        chainName = Chain.Avalanche.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.Avalanche.chainIdHex,
        icon = Chain.Avalanche.icon,
        symbol = Chain.Avalanche.symbol,
        type = Chain.Avalanche.tokenStandard,
        walletAddress = getWalletAddress(Chain.Avalanche.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.Avalanche.chainForTrustWallet,
        rpcUrl = Chain.Avalanche.rpcURL,
        order = 7
    ),

    Arbitrum(
        mainName = Chain.Arbitrum.name,
        coinType = Chain.Arbitrum.coinType,
        chainName = Chain.Arbitrum.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.Arbitrum.chainIdHex,
        icon = Chain.Arbitrum.icon,
        symbol = Chain.Arbitrum.symbol,
        type = Chain.Arbitrum.tokenStandard,
        walletAddress = getWalletAddress(Chain.Arbitrum.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.Arbitrum.chainForTrustWallet,
        rpcUrl = Chain.Arbitrum.rpcURL,
        order = 8
    ),

    Tron(
        mainName = Chain.Tron.name,
        coinType = Chain.Tron.coinType,
        chainName = Chain.Tron.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.Tron.chainIdHex,
        icon = Chain.Tron.icon,
        symbol = Chain.Tron.symbol,
        type = Chain.Tron.tokenStandard,
        walletAddress = getWalletAddress(Chain.Tron.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.Tron.chainForTrustWallet,
        rpcUrl = Chain.Tron.rpcURL,
        order = 9
    ),

    Solana(
        mainName = Chain.Solana.name,
        coinType = Chain.Solana.coinType,
        chainName = Chain.Solana.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.Solana.chainIdHex,
        icon = Chain.Solana.icon,
        symbol = Chain.Solana.symbol,
        type = Chain.Solana.tokenStandard,
        walletAddress = getWalletAddress(Chain.Solana.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.Solana.chainForTrustWallet,
        rpcUrl = Chain.Solana.rpcURL,
        order = 10
    ),

    BaseMainnet(
        mainName = Chain.BaseMainnet.name,
        coinType = Chain.BaseMainnet.coinType,
        chainName = Chain.BaseMainnet.chainName,
        chainNamespace = "eip155",
        chainReference = Chain.BaseMainnet.chainIdHex,
        icon = Chain.BaseMainnet.icon,
        symbol = Chain.BaseMainnet.symbol,
        type = Chain.BaseMainnet.tokenStandard,
        walletAddress = getWalletAddress(Chain.BaseMainnet.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.BaseMainnet.chainForTrustWallet,
        rpcUrl = Chain.BaseMainnet.rpcURL,
        order = 11
    ),


    //test net eip155:97

    BNB_TEST(
        mainName = "BNB Smart Chain Testnet",
        coinType = Chain.BinanceSmartChain.coinType,
        chainName = Chain.BinanceSmartChain.chainName,
        chainNamespace = "eip155",
        chainReference = "97",
        icon = Chain.BinanceSmartChain.icon,
        symbol = Chain.BinanceSmartChain.symbol,
        type = Chain.BinanceSmartChain.tokenStandard,
        walletAddress = getWalletAddress(Chain.BinanceSmartChain.coinType)!!,
        currentPrice = "0",
        chainForTrustWallet = Chain.BinanceSmartChain.chainForTrustWallet,
        rpcUrl = "https://bsc-testnet-rpc.publicnode.com",
        order = 12
    ),

    /* BNB_USDT_Test(
         mainName = "B-USDT",
         coinType = Chain.BinanceSmartChain.coinType,
         chainName = Chain.BinanceSmartChain.chainName,
         chainNamespace = "eip155",
         chainReference = "97",
         icon = Chain.BinanceSmartChain.icon,
         symbol = Chain.BinanceSmartChain.symbol,
         type = Chain.BinanceSmartChain.tokenStandard,
         walletAddress = getWalletAddress(Chain.BinanceSmartChain.coinType)!!,
         currentPrice = "0",
         chainForTrustWallet = Chain.BinanceSmartChain.chainForTrustWallet,
         rpcUrl = "https://bsc-testnet-rpc.publicnode.com",
         order = 8
     ),*/


}








