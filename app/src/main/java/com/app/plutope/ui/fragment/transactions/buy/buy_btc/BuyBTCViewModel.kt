package com.app.plutope.ui.fragment.transactions.buy.buy_btc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.app.plutope.data.repository.ProviderDetailsRepo
import com.app.plutope.data.repository.TokensRepo
import com.app.plutope.model.MeldRequestModel
import com.app.plutope.model.OnMetaSellBestPriceModel
import com.app.plutope.model.OnRampSellBestPriceRequestModel
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
class BuyBTCViewModel @Inject constructor(
    private val providerDetailsRepo: ProviderDetailsRepo,
    private val tokenRepo: TokensRepo
) :
    BaseViewModel<CommonNavigator>() {

    //Get On Ramp Detail
    private val _tagGetOnRampDetail =
        MutableStateFlow<NetworkState<ProviderOnRampPriceDetail?>>(NetworkState.Empty())

    val getOnRampDetailResponse: StateFlow<NetworkState<ProviderOnRampPriceDetail?>>
        get() = _tagGetOnRampDetail

    fun executeOnRampDetails(coinCode: String, fiatType: String) {
        viewModelScope.launch {
            _tagGetOnRampDetail.emit(NetworkState.Loading())
            _tagGetOnRampDetail.collectStateFlow(
                providerDetailsRepo.getOnRampDetail(
                    coinCode,
                    fiatType
                )
            )
        }
    }




    var holdSelectedProvider: ProviderModel? = null
    var holdBestPrice: String? = null


    private val _tagChangeNowBestPrice = MutableLiveData<Boolean>()
    var urlChangeStr = MutableLiveData<String>()
    val getChangeNowBestPrice = _tagChangeNowBestPrice.switchMap {
        invokeChangeNowBestPrice(urlChangeStr.value!!)
    }

    fun executeChangeNowBestPrice(url: String) {
        urlChangeStr.value = url
        _tagChangeNowBestPrice.value = true
    }

    private fun invokeChangeNowBestPrice(url: String) =
        providerDetailsRepo.executeChangeNowBestPriceApi(url)



    private val _tagOnMeldBestPrice = MutableLiveData<Boolean>()
    var bodyMeld = MutableLiveData<MeldRequestModel>()
    var urlMeldStr = MutableLiveData<String>()
    val executeOnMeldResponse = _tagOnMeldBestPrice.switchMap {
        invokeOnMeldBestPrice(urlMeldStr.value!!, bodyMeld.value!!)
    }

    fun executeOnMeldBestPrice(url: String, body: MeldRequestModel) {
        urlMeldStr.value = url
        bodyMeld.value = body
        _tagOnMeldBestPrice.value = true
    }

    private fun invokeOnMeldBestPrice(url: String, body: MeldRequestModel) =
        providerDetailsRepo.executeMeldBestPrice(url, body)


    //
    private var sign = MutableLiveData<String>()
    var timestamp = MutableLiveData<String>()
    private val _tagAlchemyPayBestPrice = MutableLiveData<Boolean>()

    //var bodyAlchemyPay = MutableLiveData<AlchemyPayResponseModel>()
    var urlAlchemy = MutableLiveData<String>()
    val executeAlchemyPayResponse = _tagAlchemyPayBestPrice.switchMap {
        invokeAlchemyPayBestPrice(sign.value!!, timestamp.value!!, urlAlchemy.value!!)
    }

    fun executeAlchemyPayBestPrice(sign: String, timestamp: String, url: String) {
        urlAlchemy.value = url
        this.sign.value = sign
        this.timestamp.value = timestamp
        _tagAlchemyPayBestPrice.value = true
    }

    private fun invokeAlchemyPayBestPrice(sign: String, timestamp: String, url: String) =
        providerDetailsRepo.executeAlchemyPayBestPrice(sign, timestamp, url)


    //sell onmeta
    private val _tagOnMetaBestPriceSell = MutableLiveData<Boolean>()
    var urlSellStr = MutableLiveData<String>()
    var bodyOnMetaSell = MutableLiveData<OnMetaSellBestPriceModel>()
    val executeOnMetaSellResponse = _tagOnMetaBestPriceSell.switchMap {
        invokeOnMetaBestSellPrice(urlSellStr.value!!, bodyOnMetaSell.value!!)
    }

    fun executeOnMetaSellBestPrice(url: String, body: OnMetaSellBestPriceModel) {
        urlSellStr.value = url
        bodyOnMetaSell.value = body
        _tagOnMetaBestPriceSell.value = true
    }

    private fun invokeOnMetaBestSellPrice(url: String, body: OnMetaSellBestPriceModel) =
        providerDetailsRepo.executeOnMetaSellBestPrice(url, body)


    //sell on Ramp
    private val _tagOnRampSellBestPrice = MutableLiveData<Boolean>()
    var headerOnRampSellSignKey = MutableLiveData<String>()
    var headerSellPayLoad = MutableLiveData<String>()
    var urlRampSellStr = MutableLiveData<String>()
    private var bodySellRamp = MutableLiveData<OnRampSellBestPriceRequestModel>()
    val executeOnRampSellResponse = _tagOnRampSellBestPrice.switchMap {
        invokeOnRampSellBestPrice(
            headerOnRampSellSignKey.value!!,
            headerSellPayLoad.value!!,
            urlRampSellStr.value!!,
            bodySellRamp.value!!
        )
    }

    fun executeOnRampSellBestPrice(
        headerOnRampSignKey: String,
        headerPayLoad: String,
        url: String,
        body: OnRampSellBestPriceRequestModel
    ) {
        urlRampSellStr.value = url
        this.headerOnRampSellSignKey.value = headerOnRampSignKey
        this.headerSellPayLoad.value = headerPayLoad
        bodySellRamp.value = body
        _tagOnRampSellBestPrice.value = true
    }

    private fun invokeOnRampSellBestPrice(
        headerOnRampSignKey: String,
        headerPayLoad: String,
        url: String,
        body: OnRampSellBestPriceRequestModel?
    ) =
        providerDetailsRepo.executeOnRampSellBestPrice(
            headerOnRampSignKey,
            headerPayLoad,
            url,
            body
        )





    /**
     * Set Active wallet
     * */

    private val _tagSetWalletActive =
        MutableStateFlow<NetworkState<String?>>(NetworkState.Empty())

    val setWalletActive: StateFlow<NetworkState<String?>>
        get() = _tagSetWalletActive

    fun setWalletActiveCall(address: String, receiverAddress: String) {
        viewModelScope.launch {
            _tagSetWalletActive.emit(NetworkState.Loading())
            _tagSetWalletActive.collectStateFlow(
                tokenRepo.setWalletActive(
                    address,
                    receiverAddress
                )
            )
        }
    }


    /**
     * Buy quote Single transaction
     * */

    private val _tagBuyQuoteSingleApi =
        MutableStateFlow<NetworkState<BuyResponseModel?>>(NetworkState.Empty())

    val buyQuoteSingleCallResponse: StateFlow<NetworkState<BuyResponseModel?>>
        get() = _tagBuyQuoteSingleApi

    fun buyQuoteSingleCall(
        body: BuyRequestModel
    ) {
        viewModelScope.launch {
            _tagBuyQuoteSingleApi.emit(NetworkState.Loading())
            _tagBuyQuoteSingleApi.collectStateFlow(
                providerDetailsRepo.buyQuoteSingleCall(
                    body = body
                )
            )
        }
    }

}