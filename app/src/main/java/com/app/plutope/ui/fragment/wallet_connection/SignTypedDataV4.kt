package com.app.plutope.ui.fragment.wallet_connection

import com.google.gson.annotations.SerializedName

data class TypedDataDomain(
    val name: String,
    val version: String,
    val chainId: Long,
    @SerializedName("verifyingContract") val verifyingContract: String
)

data class Person(
    val name: String,
    val wallet: String
)

data class TypedDataMessage(
    val from: Person,
    val to: Person,
    val contents: String
)

data class TypedDataTypes(
    @SerializedName("EIP712Domain") val eip712Domain: List<Map<String, String>>,
    @SerializedName("Person") val person: List<Map<String, String>>,
    @SerializedName("Mail") val mail: List<Map<String, String>>
)

/*data class SignTypedDataParams(
    val types: TypedDataTypes,
    val primaryType: String,
    val domain: TypedDataDomain,
    val message: TypedDataMessage
)*/

data class SignTypedDataParams(
    val types: TypedDataTypes,
    val primaryType: String,
    val domain: TypedDataDomain,
    val message: TypedDataMessage
)


/*data class SignTypedDataV4Request(
    val params: List<Any>,
    val id: String,
    val topic: String
)*/

data class SignTypedDataV4Request(
    @SerializedName("0") val address: String,
    @SerializedName("1") val params: SignTypedDataParams,
    @SerializedName("2") val id: String,
    @SerializedName("3") val topic: String
)

data class SignTypedDataArrayWrapper(
    @SerializedName("0") val firstElement: String,
    @SerializedName("1") val secondElement: SignTypedDataV4Request
)
