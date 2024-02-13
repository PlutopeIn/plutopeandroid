package com.app.plutope.networkConfig

import com.app.plutope.model.Tokens


object DataStore {
    val networkEnv: NetworkType = NetworkType.Mainnet

    private val supportedChains: List<Chain> = listOf(
        Chain.Ethereum,
        Chain.BinanceSmartChain,
        Chain.OKC,
        Chain.Polygon,
        Chain.Bitcoin
    )

    val chainByTokenStandard: Map<String, Chain> = mapOf(
        "BEP20" to Chain.BinanceSmartChain,
        "ERC20" to Chain.Ethereum,
        "POLYGON" to Chain.Polygon,
        "KIP20" to Chain.OKC,
        "BTC" to Chain.Bitcoin
    )

    val userTokenDataMap: MutableMap<String, UserTokenData> = mutableMapOf()

    fun getAllTokens(): List<Tokens> {
        var allTokens: List<Tokens> = Chain.BinanceSmartChain.tokens + Chain.Ethereum.tokens + Chain.Polygon.tokens + Chain.OKC.tokens

        allTokens = allTokens.sortedBy { token -> token.t_symbol }

        return allTokens
    }

    fun enabledTokens(): List<Tokens> {
        val coins: MutableList<Tokens> = mutableListOf()

        supportedChains.forEach { chain ->
            coins.add(
                Tokens(
                    t_address = "",
                    t_name = chain.name,
                    t_symbol = chain.symbol,
                    t_decimal = chain.decimals,
                    t_logouri = chain.icon,
                    t_type = chain.tokenStandard,
                    t_balance = "0",
                    t_price = "0",
                    t_last_price_change_impact = "0",
                    isEnable = true
                )
            )
        }

        return coins
    }
}
