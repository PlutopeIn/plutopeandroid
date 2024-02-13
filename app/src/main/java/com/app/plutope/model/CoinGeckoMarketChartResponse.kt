package com.app.plutope.model

data class CoinGeckoMarketChartResponse(
    val market_caps: List<List<Double>>,
    val prices: List<List<Double>>,
    val total_volumes: List<List<Double>>
)