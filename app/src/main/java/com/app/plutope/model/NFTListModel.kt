package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
data class NFTListModel(
    @SerializedName("result")
    val result: List<NFTModel>
)

@Keep
@Parcelize
data class NFTModel(
    val amount: String,
    val block_number: String,
    val block_number_minted: String,
    val contract_type: String,
    val last_metadata_sync: String,
    val last_token_uri_sync: String,
    val metadata: String,
    val minter_address: String,
    val name: String,
    val owner_of: String,
    val possible_spam: Boolean,
    val symbol: String,
    val token_address: String,
    val token_hash: String,
    val token_id: String,
    val token_uri: String
):Parcelable{
    val parsedMetadata: Metadata?
        get() = Gson().fromJson(metadata, Metadata::class.java)
}

data class Metadata(
    val name: String,
    val description: String,
    val image: String,
    val attributes: List<Attribute>
)

data class Attribute(
    val trait_type: String,
    val value: Any
)