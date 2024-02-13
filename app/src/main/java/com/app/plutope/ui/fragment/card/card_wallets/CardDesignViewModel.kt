package com.app.plutope.ui.fragment.card.card_wallets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.plutope.model.CurrencyModel
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardDesignViewModel @Inject constructor() : BaseViewModel<CommonNavigator>() {

    private val _currencyList = MutableLiveData<List<CurrencyModel>>()

    val currencyList: LiveData<List<CurrencyModel>>
        get() = _currencyList

    fun setCurrencyList(list: MutableList<CurrencyModel>) {
        _currencyList.value = list
    }

    private val _cryptoList = MutableLiveData<List<CurrencyModel>>()

    val cryptoList: LiveData<List<CurrencyModel>>
        get() = _cryptoList

    fun setCryptoListList(list: MutableList<CurrencyModel>) {
        _cryptoList.value = list
    }


}