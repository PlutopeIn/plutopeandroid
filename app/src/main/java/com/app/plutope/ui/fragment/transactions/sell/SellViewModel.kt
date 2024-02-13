package com.app.plutope.ui.fragment.transactions.sell

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.ProviderDetailsRepo
import com.app.plutope.model.ChangeNowBestPriceResponse
import com.app.plutope.model.EstimateMinOnChangeValue
import com.app.plutope.model.ExchangeRequestModel
import com.app.plutope.model.ExchangeResponseModel
import com.app.plutope.model.MeldRequestModel
import com.app.plutope.model.MeldResponseModel
import com.app.plutope.model.OnMetaBestPriceModel
import com.app.plutope.model.OnMetaBestPriceResponseModel
import com.app.plutope.model.OnRampBestPriceRequestModel
import com.app.plutope.model.OnRampResponseModel
import com.app.plutope.model.ProviderOnRampPriceDetail
import com.app.plutope.ui.base.BaseViewModel
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.app.plutope.utils.common.CommonNavigator
import com.app.plutope.utils.network.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellViewModel @Inject constructor(private val providerDetailsRepo: ProviderDetailsRepo) :
    BaseViewModel<CommonNavigator>() {

    //Get On Ramp Detail
    var holdSelectedProvider: ProviderModel? = null



}