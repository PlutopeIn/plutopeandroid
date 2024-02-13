package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class TransactionResponse(
    val apiVersion: String,
    val `data`: Data,
    val requestId: String
):Parcelable

@Keep
@Parcelize
data class Data(
    val items: List<Item>,
    val limit: Int,
    val offset: Int,
    val total: Int
):Parcelable

@Keep
@Parcelize
data class Item(
    val blockchainSpecific: BlockchainSpecific,
    val fee: Fee,
    val index: Int,
    val minedInBlockHash: String,
    val minedInBlockHeight: Int,
    val recipients: List<Recipient>,
    val senders: List<Sender>,
    val timestamp: Int,
    val transactionHash: String,
    val transactionId: String
):Parcelable

@Keep
@Parcelize
data class BlockchainSpecific(
    val gasLimit: String,
    val gasPrice: GasPrice,
    val gasUsed: String,
    val inputData: String,
    val internalTransactionsCount: Int,
    val nonce: Int,
    val tokenTransfersCount: Int,
    val transactionStatus: String
):Parcelable

@Keep
@Parcelize
data class Fee(
    val amount: String,
    val unit: String
):Parcelable

@Keep
@Parcelize
data class Recipient(
    val address: String,
    val amount: String
):Parcelable

@Keep
@Parcelize
data class Sender(
    val address: String,
    val amount: String
):Parcelable

@Keep
@Parcelize
data class GasPrice(
    val amount: String,
    val unit: String
):Parcelable