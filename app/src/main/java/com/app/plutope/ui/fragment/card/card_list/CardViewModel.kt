package com.app.plutope.ui.fragment.card.card_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.utils.common.CommonNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor() : BaseViewModel<CommonNavigator>() {

    private val _cardList = MutableLiveData<List<CardListModel>>()

    val cardList: LiveData<List<CardListModel>>
        get() = _cardList

    fun setCardList(list: MutableList<CardListModel>) {
        _cardList.value = list
    }

}