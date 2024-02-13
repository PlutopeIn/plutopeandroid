package com.app.plutope.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "walletTokens",
    foreignKeys = [ForeignKey(
        entity = Wallets::class,
        parentColumns = ["w_id"],
        childColumns = ["walletId"],
        onDelete = CASCADE
    ),
        ForeignKey(
            entity = Tokens::class,
            parentColumns = ["t_pk"],
            childColumns = ["t_pk"],
            onDelete = CASCADE
        )]
)

@Keep
data class WalletTokens(
    var walletId: Int,
    var t_pk: Int,
    var isEnable: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var balance:String = "0.0"
)