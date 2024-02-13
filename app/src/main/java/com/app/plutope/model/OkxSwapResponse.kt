package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class OkxSwapResponse(
    val code: String,
    @SerializedName("data") val data1: List<Data1>,
    val msg: String
):Parcelable

@Keep
@Parcelize
data class Data1(
    val routerResult: RouterResult,
    val tx: Tx
):Parcelable

@Keep
@Parcelize
data class RouterResult(
    val chainId: String,
    val dexRouterList: List<DexRouter>,
    val estimateGasFee: String,
    val fromToken: FromTokenX,
    val fromTokenAmount: String,
    val toToken: ToTokenX,
    val toTokenAmount: String
):Parcelable

@Keep
@Parcelize
data class Tx(
    val `data`: String,
    val from: String,
    val gas: String,
    val gasPrice: String,
    val minReceiveAmount: String,
    val to: String,
    val value: String
):Parcelable

@Keep
@Parcelize
data class DexRouter(
    val router: String,
    val routerPercent: String,
    val subRouterList: List<SubRouter>
):Parcelable

@Keep
@Parcelize
data class FromTokenX(
    val tokenContractAddress: String,
    val tokenSymbol: String
):Parcelable

@Keep
@Parcelize
data class ToTokenX(
    val tokenContractAddress: String,
    val tokenSymbol: String
):Parcelable

@Keep
@Parcelize
data class SubRouter(
    val dexProtocol: List<DexProtocol>,
    val fromToken: FromTokenX,
    val toToken: ToTokenX
):Parcelable

@Keep
@Parcelize
data class DexProtocol(
    val dexName: String,
    val percent: String
):Parcelable