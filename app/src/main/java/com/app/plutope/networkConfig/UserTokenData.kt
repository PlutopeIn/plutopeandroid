package com.app.plutope.networkConfig

data class UserTokenData(
    var price: String,
    var balance: String,
    var lastPriceChangeImpact: String
) {
    companion object {
        fun update(symbol: String, price: String? = null, balance: String? = null, lastPriceChangeImpact: String? = null) {
            if (DataStore.userTokenDataMap.containsKey(symbol)) {
                val userTokenData = DataStore.userTokenDataMap[symbol]
                userTokenData?.price = price ?: userTokenData?.price ?: ""
                userTokenData?.balance = balance ?: userTokenData?.balance ?: ""
                userTokenData?.lastPriceChangeImpact = lastPriceChangeImpact ?: userTokenData?.lastPriceChangeImpact ?: ""
            } else {
                DataStore.userTokenDataMap[symbol] = UserTokenData(
                    price = price ?: "",
                    balance = balance ?: "",
                    lastPriceChangeImpact = lastPriceChangeImpact ?: ""
                )
            }
        }
    }
}
