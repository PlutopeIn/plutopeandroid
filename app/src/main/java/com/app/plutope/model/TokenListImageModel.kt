package com.app.plutope.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "TokenImageModel")
data class TokenListImageModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val coin_id: String,
    //   @SerializedName("id") val image_id: Int,
    val image: String,
    val name: String,
    val symbol: String
)