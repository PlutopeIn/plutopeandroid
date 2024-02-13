package com.app.plutope.ui.base

import androidx.lifecycle.ViewModel
import com.app.plutope.utils.network.NetworkState
import kotlinx.coroutines.flow.MutableStateFlow
import java.lang.ref.WeakReference

open class BaseViewModel<N> : ViewModel() {

    private var mNavigator: WeakReference<N>? = null


    fun getNavigator(): N? {
        return mNavigator?.get()
    }

    fun setNavigator(navigator: N) {
        this.mNavigator = WeakReference(navigator)
    }


    fun <T> MutableStateFlow<NetworkState<T?>>.collectStateFlow(apiCall: NetworkState<T?>) {
        when (apiCall) {
            is NetworkState.Success -> this.value =
                NetworkState.Success(apiCall.status!!, apiCall.data)

            is NetworkState.Error -> this.value = NetworkState.Error(apiCall.message!!)

            is NetworkState.SessionOut -> this.value =
                NetworkState.SessionOut(apiCall.status.toString(), apiCall.message.toString())

            is NetworkState.SuccessMessage -> {
                this.value =
                    NetworkState.SuccessMessage(apiCall.status!!, apiCall.message.toString())
            }

            else -> {}
        }
    }



}