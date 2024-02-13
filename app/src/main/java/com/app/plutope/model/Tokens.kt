package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.app.plutope.data.blockChainFunction.BlockchainFunctions
import com.app.plutope.data.blockChainFunction.ChainFunctions
import com.app.plutope.data.blockChainFunction.TokenFunctions
import com.app.plutope.networkConfig.Chain
import com.app.plutope.networkConfig.DataStore
import com.app.plutope.networkConfig.UserTokenData
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Entity(tableName = "Tokens")
@Parcelize
data class Tokens(
    @PrimaryKey(autoGenerate = true)
    var t_pk: Int = 0,
    @SerializedName("decimals") var t_decimal: Int? = 18,
    @SerializedName("address") var t_address: String? = "",
    @SerializedName("balance") var t_balance: String = "0.0",
    @SerializedName("lastPriceChangeImpact") var t_last_price_change_impact: String? = "",
    @SerializedName("logoURI") var t_logouri: String? = "",
    @SerializedName("name") var t_name: String? = "",
    @SerializedName("price") var t_price: String? = "0",
    @SerializedName("symbol") var t_symbol: String? = "",
    @SerializedName("type") var t_type: String? = "",
    @SerializedName("isEnable") var isEnable: Boolean? = false,
    @SerializedName("isCustomTokens") var isCustomTokens: Boolean? = false,
    var tokenId: String? = "",
    var tokenPerPrice:String?="0"
) : Parcelable{
    val userTokenData: UserTokenData?
        get() = DataStore.userTokenDataMap[t_symbol]

    val chain: Chain?
        get() = DataStore.chainByTokenStandard[t_type]

    val callFunction: BlockchainFunctions
        get() = if (t_address != "") TokenFunctions(this) else ChainFunctions(this)

}

enum class SwapExchangeStatus(){
    finished,
    failed,
    new,
    waiting,
    confirming,
    exchanging,
    sending,
    refunded,
    verifying
}

