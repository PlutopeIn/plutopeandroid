package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class NFTListModel(
    val amount: String? = "",
    val blockNumber: String? = "",
    val blockNumberMinted: String? = "",
    val contractType: String? = "",
    val lastMetadataSync: String? = "",
    val lastTokenUriSync: String? = "",
    val metadata: Metadata? = Metadata(),
    val minterAddress: String? = "",
    val name: String? = "",
    val ownerOf: String? = "",
    val possibleSpam: String? = "",
    val symbol: String? = "",
    val tokenAddress: String? = "",
    val tokenHash: String? = "",
    val tokenId: String? = "",
    val tokenUri: String? = "",
    val chain: String? = ""
) : Parcelable {
    /* val parsedMetadata: Metadata?
         get() = Gson().fromJson(metadata, Metadata::class.java)*/
}

@Parcelize
data class Metadata(
    val name: String? = "",
    val description: String? = "",
    val image: String? = "",
    val attributes: List<Attribute>? = mutableListOf()
) : Parcelable

@Parcelize
data class Attribute(
    val trait_type: String? = "",
    val value: String? = ""
) : Parcelable