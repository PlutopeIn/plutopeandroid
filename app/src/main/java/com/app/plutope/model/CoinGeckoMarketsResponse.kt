package com.app.plutope.model

data class CoinGeckoMarketsResponse(
    val ath: String,
    val ath_change_percentage: Double,
    val ath_date: String,
    val atl: Double,
    val atl_change_percentage: Double,
    val atl_date: String,
    val circulating_supply: String,
    val current_price: String?,
    val fully_diluted_valuation: String,
    val high_24h: String,
    val id: String,
    val image: String,
    val last_updated: String,
    val low_24h: String,
    val market_cap: String? = "0",
    val market_cap_change_24h: String,
    val market_cap_change_percentage_24h: String,
    val market_cap_rank: String,
    val max_supply: String,
    val name: String,
    val price_change_24h: String,
    val price_change_percentage_24h: String? = "0",
    val roi: Roi? = null,
    var symbol: String,
    val total_supply: String,
    val total_volume: String? = "0"
)

data class Roi(
    val currency: String,
    val percentage: Double,
    val times: Double
)
