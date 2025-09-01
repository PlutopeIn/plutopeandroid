package com.app.plutope.networkConfig


object DataStore {
    val networkEnv: NetworkType = NetworkType.Mainnet

    val chainByTokenStandard: Map<String, Chain> = mapOf(
        "BEP20" to Chain.BinanceSmartChain,
        "ERC20" to Chain.Ethereum,
        "POLYGON" to Chain.Polygon,
        "KIP20" to Chain.OKC,
        "BTC" to Chain.Bitcoin,
        "OP Mainnet" to Chain.Optimism,
        "Avalanche" to Chain.Avalanche,
        "TRC20" to Chain.Tron,
        "Arbitrum" to Chain.Arbitrum,
        "Base" to Chain.BaseMainnet
    )

    val userTokenDataMap: MutableMap<String, UserTokenData> = mutableMapOf()

}
