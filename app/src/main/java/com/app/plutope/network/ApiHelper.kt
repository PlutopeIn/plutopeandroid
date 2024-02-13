package com.app.plutope.network

import com.app.plutope.model.AlchemyPayResponseModel
import com.app.plutope.model.AvailablePairsResponseModel
import com.app.plutope.model.ChangeNowBestPriceResponse
import com.app.plutope.model.CoinDetailModel
import com.app.plutope.model.CoinGeckoMarketChartResponse
import com.app.plutope.model.CoinGeckoMarketsResponse
import com.app.plutope.model.CoinMarketCurrencyResponse
import com.app.plutope.model.EstimateMinOnChangeValue
import com.app.plutope.model.ExchangeRequestModel
import com.app.plutope.model.ExchangeResponseModel
import com.app.plutope.model.ExchangeStatusResponse
import com.app.plutope.model.Info
import com.app.plutope.model.MeldRequestModel
import com.app.plutope.model.MeldResponseModel
import com.app.plutope.model.NFTListModel
import com.app.plutope.model.OkxApproveResponse
import com.app.plutope.model.OkxSwapResponse
import com.app.plutope.model.OnMetaBestPriceModel
import com.app.plutope.model.OnMetaBestPriceResponseModel
import com.app.plutope.model.OnMetaSellBestPriceModel
import com.app.plutope.model.OnRampBestPriceRequestModel
import com.app.plutope.model.OnRampResponseModel
import com.app.plutope.model.OnRampSellBestPriceRequestModel
import com.app.plutope.model.ProviderOnRampPriceDetail
import com.app.plutope.model.RangSwapQuoteModel
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.TransactionHistoryDetail
import com.app.plutope.model.TransactionHistoryModel
import com.app.plutope.model.TransactionHistoryResponse
import com.app.plutope.model.TransactionResponse
import com.app.plutope.model.UnlimitBestPriceModel
import com.app.plutope.ui.fragment.ens.DomainSearchModel
import com.app.plutope.ui.fragment.ens.ENSListModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.RangoSwapResponseModel
import okhttp3.ResponseBody
import retrofit2.Response

interface ApiHelper {
    suspend fun getCoinsAssets(symbol: String): Response<Info>
    // suspend fun getAssets(symbol: String): Response<Info>

    suspend fun getAssets(
        header: String,
        url: String
    ): Response<ResponseBody>

    suspend fun getTransactionHistory(header: String, url: String): Response<TransactionResponse>

    suspend fun executeExchange(
        url: String,
        body: ExchangeRequestModel?
    ): Response<ExchangeResponseModel>

    suspend fun rangSwapQuoteCall(
        fromBlockchain: String,
        fromTokenSymbol: String,
        fromTokenAddress: String,
        toBlockchain: String,
        toTokenSymbol: String,
        toTokenAddress: String,
        walletAddress: String,
        price: String,
        fromWalletAddress: String,
        toWalletAddress: String,
    ): Response<RangSwapQuoteModel>

    suspend fun rangSwapSubmitCall(
        fromBlockchain: String,
        fromTokenSymbol: String,
        fromTokenAddress: String,
        toBlockchain: String,
        toTokenSymbol: String,
        toTokenAddress: String,
        walletAddress: String,
        price: String,
        fromWalletAddress: String,
        toWalletAddress: String,
    ): Response<RangoSwapResponseModel>

    suspend fun executeExchangeStatus(url: String): Response<ExchangeStatusResponse>
    suspend fun executeTransactionHistory(url: String): Response<TransactionHistoryModel>

    suspend fun getOnRampDetail(
        coinCode: String,
        fiatType: String
    ): Response<ProviderOnRampPriceDetail>

    suspend fun executeSwapUsingOkx(
        url: String, headerOkxSignKey: String,
        headerTimeStamp: String,
    ): Response<OkxSwapResponse>

    suspend fun executeAvailablePairs(url: String): Response<MutableList<AvailablePairsResponseModel>>

    suspend fun executeCoinGeckoMarketChartApi(url: String): Response<CoinGeckoMarketChartResponse>
    suspend fun executeNftListApi(url: String): Response<NFTListModel>
    suspend fun estimationMinChangeNow(url: String): Response<EstimateMinOnChangeValue>

    suspend fun executeCoinGeckoMarketsApi(url: String): Response<MutableList<CoinGeckoMarketsResponse>>

    suspend fun executeGetCurrencyApi(url: String): Response<CoinMarketCurrencyResponse>

    suspend fun executeOkLinkTransactionList(url: String): Response<TransactionHistoryResponse>
    suspend fun executeOkLinkTransactionDetail(url: String): Response<TransactionHistoryDetail>

    suspend fun executeOnMetaBestPriceApi(
        url: String,
        body: OnMetaBestPriceModel?
    ): Response<OnMetaBestPriceResponseModel>

    suspend fun executeChangeNowBestPrice(url: String): Response<ChangeNowBestPriceResponse>
    suspend fun executeUnlimitBestPrice(url: String): Response<UnlimitBestPriceModel>

    suspend fun executeOnRampBestPrice(
        headerOnRampSignKey: String,
        headerPayLoad: String,
        url: String,
        body: OnRampBestPriceRequestModel?
    ): Response<OnRampResponseModel>

    suspend fun executeOnMeldBestPrice(
        url: String,
        body: MeldRequestModel
    ): Response<MeldResponseModel>

    suspend fun executeCoinListApi(): Response<ResponseBody>


    suspend fun executeTokenImageListApi(): Response<List<TokenListImageModel>>
    suspend fun executeAlchemyPayBestPrice(
        sign: String,
        timestamp: String, url: String
    ): Response<AlchemyPayResponseModel>

    suspend fun executeOnMetaBestSellPriceApi(
        url: String,
        body: OnMetaSellBestPriceModel?
    ): Response<OnMetaBestPriceResponseModel>

    suspend fun executeOnRampSellBestPrice(
        headerOnRampSignKey: String,
        headerPayLoad: String,
        url: String,
        body: OnRampSellBestPriceRequestModel?
    ): Response<OnRampResponseModel>

    suspend fun executeCoinDetailApi(url: String): Response<CoinDetailModel>

    suspend fun executeApproveUsingOkx(
        url: String, headerOkxSignKey: String,
        headerTimeStamp: String,
    ): Response<OkxApproveResponse>

    suspend fun getBitcoinWalletBalance(address: String): Response<ResponseBody>
    suspend fun registerWallet(
        walletAddress: String,
        deviceId: String,
        fcmToken: String
    ): Response<ResponseBody>

    suspend fun setWalletActive(walletAddress: String): Response<ResponseBody>
    suspend fun sendBTCTransaction(
        privateKey: String,
        value: String,
        toAddress: String,
        env: String,
        fromAddress: String
    ): Response<ResponseBody>

    suspend fun domainCheck(domainSearchModel: DomainSearchModel): Response<ENSListModel>


}