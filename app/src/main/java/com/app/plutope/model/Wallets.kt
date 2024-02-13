package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity("Wallets")
data class Wallets(
    @PrimaryKey(autoGenerate = true)
    var w_id: Int = 0,
    var w_isprimary: Int? = 0,
    var w_mnemonic: String? = "",
    var w_wallet_name: String? = "",
    var w_wallet_last_balance: String? = "0.0",
    var w_is_cloud_backup:Boolean=false,
    var w_is_manual_backup:Boolean=false,
    var folderId:String="",
    var fileId:String=""
) : Parcelable