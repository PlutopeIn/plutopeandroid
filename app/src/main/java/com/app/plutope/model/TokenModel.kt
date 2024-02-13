package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.app.plutope.utils.coinTypeEnum.CoinType
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Keep
@Parcelize
data class TokenModel(val tokenInfo: TokenInfoModel, val balance: BigDecimal) : Parcelable

@Keep
@Parcelize
data class TokenInfoModel(var address:String, val name:String, val symbol:String, val decimals:Int, val price : String,val coinType: CoinType?=null):Parcelable


@Keep
class TokenList(
    val name: String,
    val logoURI: String,
    val tokens: List<Tokens>
)
