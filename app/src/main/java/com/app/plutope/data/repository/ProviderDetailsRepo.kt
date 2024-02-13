package com.app.plutope.data.repository

import androidx.lifecycle.liveData
import com.app.plutope.model.AlchemyPayResponseModel
import com.app.plutope.model.ChangeNowBestPriceResponse
import com.app.plutope.model.EstimateMinOnChangeValue
import com.app.plutope.model.MeldRequestModel
import com.app.plutope.model.MeldResponseModel
import com.app.plutope.model.OnMetaBestPriceModel
import com.app.plutope.model.OnMetaSellBestPriceModel
import com.app.plutope.model.OnRampBestPriceRequestModel
import com.app.plutope.model.OnRampResponseModel
import com.app.plutope.model.OnRampSellBestPriceRequestModel
import com.app.plutope.model.ProviderOnRampPriceDetail
import com.app.plutope.model.ResultOnMeta
import com.app.plutope.model.UnlimitBestPriceModel
import com.app.plutope.network.ApiHelper
import com.app.plutope.network.NoConnectivityException
import com.app.plutope.utils.Event
import com.app.plutope.utils.constant.NO_INTERNET_CONNECTION
import com.app.plutope.utils.constant.responseServerError
import com.app.plutope.utils.constant.serverErrorMessage
import com.app.plutope.utils.network.NetworkState
import java.net.UnknownHostException
import javax.inject.Inject

class ProviderDetailsRepo @Inject constructor(private val apiHelper: ApiHelper) {

    suspend fun getOnRampDetail(
        coinCode: String, fiatType: String
    ): NetworkState<ProviderOnRampPriceDetail?> {
        return try {
            val response = apiHelper.getOnRampDetail(coinCode = coinCode, fiatType = fiatType)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful && result?.data != null) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error("")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.message.toString())
            }
        }

    }


    suspend fun estimationMinChangeNow(url: String): NetworkState<EstimateMinOnChangeValue?> {
        return try {
            val response = apiHelper.estimationMinChangeNow(url)
            val result = response.body()
            if (response.code() == responseServerError) {
                NetworkState.Error(serverErrorMessage)
            } else {
                if (response.isSuccessful) {
                    NetworkState.Success("", result)
                } else {
                    NetworkState.Error(response.errorBody().toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                NetworkState.SessionOut("", NO_INTERNET_CONNECTION)
            }else {
                NetworkState.Error(e.localizedMessage.toString())
            }

        }
    }
    fun executeOnMetaBestPrice( url: String,
                                   body: OnMetaBestPriceModel) = liveData<Event<NetworkState<ResultOnMeta>>>{
        emit(Event(NetworkState.Loading()))
        try {
            val response = apiHelper.executeOnMetaBestPriceApi(url,body)
            val result = response.body()
            if (response.isSuccessful) {
                if (result != null) {
                    val empList = response.body()?.result

                    if(empList!=null){
                        emit(Event(NetworkState.Success("", empList)))
                    }else{
                        emit(Event(NetworkState.Error(response.errorBody().toString())))
                    }
                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } else {
                emit(Event(NetworkState.Error(response.errorBody().toString())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
            }else {
                emit(Event(NetworkState.Error(e.message.toString())))
            }

        }
    }

    fun executeOnMetaSellBestPrice( url: String,
                                body: OnMetaSellBestPriceModel) = liveData<Event<NetworkState<ResultOnMeta>>>{
        emit(Event(NetworkState.Loading()))
        try {
            val response = apiHelper.executeOnMetaBestSellPriceApi(url,body)
            val result = response.body()
            if (response.isSuccessful) {
                if (result != null) {
                    val empList = response.body()?.result
                    if(empList!=null){
                        emit(Event(NetworkState.Success("", empList)))
                    }else{
                        emit(Event(NetworkState.Error(response.errorBody().toString())))
                    }

                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } else {
                emit(Event(NetworkState.Error(response.errorBody().toString())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
            }else {
                emit(Event(NetworkState.Error(e.message.toString())))
            }

        }
    }


    fun executeChangeNowBestPriceApi( url: String) = liveData<Event<NetworkState<ChangeNowBestPriceResponse>>> {
        emit(Event(NetworkState.Loading()))
        try {
            val response = apiHelper.executeChangeNowBestPrice(url)
            val result = response.body()
            if (response.isSuccessful) {
                if (result != null) {

                    emit(Event(NetworkState.Success("", result)))
                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } else {
                emit(Event(NetworkState.Error(response.errorBody().toString())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
            }else {
                emit(Event(NetworkState.Error(e.message.toString())))
            }

        }
    }


    fun executeOnRampBestPrice(  headerOnRampSignKey: String,
                                 headerPayLoad:String,
                                 url:String,
                                 body: OnRampBestPriceRequestModel?) = liveData<Event<NetworkState<OnRampResponseModel>>> {
        emit(Event(NetworkState.Loading()))
        try {
            val response = apiHelper.executeOnRampBestPrice(headerOnRampSignKey,headerPayLoad,url,body)
            val result = response.body()
            if (response.isSuccessful) {
                if (result?.response != null) {

                    emit( Event(NetworkState.Success("", result)))
                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } else {
                emit(Event(NetworkState.Error(response.errorBody().toString())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
            }else {
                emit(Event(NetworkState.Error(e.message.toString())))
            }

        }
    }



    fun executeMeldBestPrice(  url:String,
                               body: MeldRequestModel?) = liveData<Event<NetworkState<MeldResponseModel>>> {
        emit(Event(NetworkState.Loading()))
        try {
            val response = apiHelper.executeOnMeldBestPrice(url,body!!)
            val result = response.body()
            if (response.isSuccessful) {
                if (result?.quotes != null) {

                    emit(Event(NetworkState.Success("", result)))
                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } else {
                emit(Event(NetworkState.Error(response.errorBody().toString())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
            }else {
                emit(Event(NetworkState.Error(e.message.toString())))
            }

        }
    }

    fun executeAlchemyPayBestPrice(sign: String,
                                      timeStamp:String,
                                      url:String,) = liveData<Event<NetworkState<AlchemyPayResponseModel>>> {
        emit(Event(NetworkState.Loading()))
        try {
            val response = apiHelper.executeAlchemyPayBestPrice(sign,timeStamp,url)
            val result = response.body()
            if (response.isSuccessful) {
                if (result?.resultList != null) {

                    emit(Event(NetworkState.Success("", result)))
                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } else {
                emit(Event(NetworkState.Error(response.errorBody().toString())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if(e is NoConnectivityException || e is UnknownHostException) {
                emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
            }else {
                emit(Event(NetworkState.Error(e.message.toString())))
            }
        }
    }


    fun executeOnRampSellBestPrice(  headerOnRampSignKey: String,
                                 headerPayLoad:String,
                                 url:String,
                                 body: OnRampSellBestPriceRequestModel?) = liveData<Event<NetworkState<OnRampResponseModel>>> {
        emit(Event(NetworkState.Loading()))
        try {
            val response = apiHelper.executeOnRampSellBestPrice(headerOnRampSignKey,headerPayLoad,url,body)
            val result = response.body()
            if (response.isSuccessful) {
                if (result?.response != null) {

                    emit( Event(NetworkState.Success("", result)))
                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } else {
                emit(Event(NetworkState.Error(response.errorBody().toString())))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is NoConnectivityException || e is UnknownHostException) {
                emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
            } else {
                emit(Event(NetworkState.Error(e.message.toString())))
            }
        }
    }


    fun executeUnlimitBestPrice(url: String) =
        liveData<Event<NetworkState<UnlimitBestPriceModel>>> {
            emit(Event(NetworkState.Loading()))
            try {
                val response = apiHelper.executeUnlimitBestPrice(url)
                val result = response.body()
                if (response.isSuccessful) {
                    if (result != null) {
                        emit(Event(NetworkState.Success("", result)))
                    } else {
                        emit(Event(NetworkState.Error(response.errorBody().toString())))
                    }
                } else {
                    emit(Event(NetworkState.Error(response.errorBody().toString())))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (e is NoConnectivityException || e is UnknownHostException) {
                    emit(Event(NetworkState.SessionOut("", NO_INTERNET_CONNECTION)))
                } else {
                    emit(Event(NetworkState.Error(e.message.toString())))
                }

            }
        }

}