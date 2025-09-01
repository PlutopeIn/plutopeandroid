package com.app.plutope.utils.network

sealed class NetworkState<T>(
    val status: String? = null,
    val message: Any? = null,
    val data: T? = null,
    val dataList: ArrayList<T>? = null
) {
    class SessionOut<T>(code: String, msg: String) : NetworkState<T>(code, msg)
    class Loading<T> : NetworkState<T>()
    class Success<T>(code: String, data: T) : NetworkState<T>(code, data = data)

    data class SuccessMessage<T>(val code: String, val successMessage: String) :
        NetworkState<T>(code, message = successMessage)

    class Empty<T> : NetworkState<T>()
    class Error<T>(code: Any) : NetworkState<T>(message = code)
}

const val defaultErrorMessage = "Something Went Wrong"

sealed class UiState<out T : Any?> {

    //  object Loading : UiState<Nothing>()

    //  data class Success<out T : Any>(val data: T) : UiState<T>()

    // data class Error(val errorMessage: String) : UiState<Nothing>()
}