package com.app.plutope.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Entity("Currency")
@Parcelize
data class CurrencyModel(
    val name: String = "",
    @SerializedName("sign") val symbol: String = "",
    @SerializedName("symbol") val code: String = "",
    @PrimaryKey val id: Int = 0,
    var isSelected: Boolean = false
):Parcelable


class CurrencyList(
    val data: List<CurrencyModel>
)