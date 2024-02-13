package com.app.plutope.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class PreviewSwapDetail(
    val payObject: Tokens,
    val getObject: Tokens,
    val routerResult: Data1?,
    val payAmount: String,
    val getAmount: String,
    val quote: String
) : Parcelable