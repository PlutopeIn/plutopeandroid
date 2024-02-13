package com.app.plutope.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class GenericResponseModel<T> (
    @SerializedName("status") var status: Int,
    @SerializedName("error_msg") var error_msg: String,
    @Expose @SerializedName("data") var data: ArrayList<T>
//    @SerializedName("success") var success: Boolean
)