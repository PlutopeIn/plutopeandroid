package com.app.plutope.model

data class CoinDetailModel(
    val additional_notices: List<Any>,
    val asset_platform_id: String,
    val block_time_in_minutes: Int,
    val categories: List<String>,
    val coingecko_rank: Int,
    val coingecko_score: Double,
    val community_score: Double,
    val contract_address: String,
    val country_origin: String,
    val description: Description,
)

data class Description(
    val en: String
)

