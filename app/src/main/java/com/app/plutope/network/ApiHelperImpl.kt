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
import com.app.plutope.model.ModelActiveWalletToken
import com.app.plutope.model.NFTListModel
import com.app.plutope.model.OkxApproveResponse
import com.app.plutope.model.OkxSwapResponse
import com.app.plutope.model.OnMetaBestPriceModel
import com.app.plutope.model.OnMetaBestPriceResponseModel
import com.app.plutope.model.OnMetaSellBestPriceModel
import com.app.plutope.model.OnRampBestPriceRequestModel
import com.app.plutope.model.OnRampResponseModel
import com.app.plutope.model.OnRampSellBestPriceRequestModel
import com.app.plutope.model.RangSwapQuoteModel
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.TransactionHistoryDetail
import com.app.plutope.model.TransactionHistoryModel
import com.app.plutope.model.TransactionHistoryResponse
import com.app.plutope.model.TransactionResponse
import com.app.plutope.model.UnlimitBestPriceModel
import com.app.plutope.ui.fragment.ens.DomainSearchModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.RangoSwapResponseModel
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun getCoinsAssets(
        symbol: String,
    ): Response<Info> = apiService.getCoinsAssets(symbol)

    override suspend fun getAssets(
        header: String,
        url: String
    ): Response<ResponseBody> = apiService.getAssets(header, url)

    override suspend fun getTransactionHistory(
        header: String,
        url: String
    ): Response<TransactionResponse> = apiService.getTransactionHistory(header, url)

    override suspend fun executeExchange(
        url: String,
        body: ExchangeRequestModel?
    ): Response<ExchangeResponseModel> = apiService.executeExchange(url = url, body = body)

    override suspend fun rangSwapQuoteCall(
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
    ): Response<RangSwapQuoteModel> = apiService.rangSwapQuoteCall(
        fromBlockchain,
        fromTokenSymbol,
        fromTokenAddress,
        toBlockchain,
        toTokenSymbol,
        toTokenAddress,
        walletAddress,
        price,
        fromWalletAddress,
        toWalletAddress
    )

    override suspend fun rangSwapSubmitCall(
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
    ): Response<RangoSwapResponseModel> = apiService.rangoSwapSubmitCall(
        fromBlockchain,
        fromTokenSymbol,
        fromTokenAddress,
        toBlockchain,
        toTokenSymbol,
        toTokenAddress,
        walletAddress,
        price,
        fromWalletAddress,
        toWalletAddress,

        )

    override suspend fun executeExchangeStatus(url: String): Response<ExchangeStatusResponse> =
        apiService.executeExchangeStatus(url = url)

    override suspend fun getOnRampDetail(
        coinCode: String,
        fiatType: String
    ) = apiService.getOnRampDetail(coinCode, fiatType)

    override suspend fun executeTransactionHistory(url: String): Response<TransactionHistoryModel> =
        apiService.executeTransactionHistory(url)

    override suspend fun executeSwapUsingOkx(
        url: String,
        headerOkxSignKey: String,
        headerTimeStamp: String
    ): Response<OkxSwapResponse> =
        apiService.executeSwapUsingOkx(url=url, okAccessSignKey = headerOkxSignKey, timestamp = headerTimeStamp)

    override suspend fun executeAvailablePairs(url: String): Response<MutableList<AvailablePairsResponseModel>> =
        apiService.executeAvailablePairs(url)

    override suspend fun executeCoinGeckoMarketChartApi(url: String): Response<CoinGeckoMarketChartResponse> =
        apiService.executeCoinGeckoMarketChartApi(url)

    override suspend fun executeNftListApi(url: String): Response<NFTListModel> =
        apiService.executeNftListingApi(url)

    override suspend fun estimationMinChangeNow(url: String): Response<EstimateMinOnChangeValue> =
        apiService.estimatedMinChangeNow(url)

    override suspend fun executeCoinGeckoMarketsApi(url: String): Response<MutableList<CoinGeckoMarketsResponse>> =
        apiService.executeCoinGeckoMarketsApi(url)

    override suspend fun executeGetCurrencyApi(url: String): Response<CoinMarketCurrencyResponse> =
        apiService.executeCurrencyListApi(url)

    override suspend fun executeOkLinkTransactionList(url: String): Response<TransactionHistoryResponse> =
        apiService.executeOkLinkTransactionList(url)

    override suspend fun executeOkLinkTransactionDetail(url: String): Response<TransactionHistoryDetail> =
        apiService.executeOkLinkTransactionDetail(url)

    override suspend fun executeOnMetaBestPriceApi(
        url: String,
        body: OnMetaBestPriceModel?
    ): Response<OnMetaBestPriceResponseModel> =
        apiService.executeOnMetaBestPriceApi(url = url, body = body)

    override suspend fun executeChangeNowBestPrice(url: String): Response<ChangeNowBestPriceResponse> =
        apiService.executeChangeNowBestPrice(url)

    override suspend fun executeUnlimitBestPrice(url: String): Response<UnlimitBestPriceModel> =
        apiService.executeUnlimitBestPrice(url)

    override suspend fun executeOnRampBestPrice(
        headerOnRampSignKey: String,
        headerPayLoad: String,
        url: String,
        body: OnRampBestPriceRequestModel?
    ): Response<OnRampResponseModel> = apiService.executeOnRampBestPrice(
        headerOnRampSignKey = headerOnRampSignKey,
        headerPayLoad = headerPayLoad,
        url = url,
        body = body
    )

    override suspend fun executeOnMeldBestPrice(
        url: String,
        body: MeldRequestModel
    ): Response<MeldResponseModel> = apiService.executeOnMeld(url = url, body = body)

    override suspend fun executeCoinListApi(): Response<ResponseBody> =
        apiService.executeCoinListApi()

    override suspend fun executeTokenImageListApi(): Response<List<TokenListImageModel>> =
        apiService.executeTokenImageListApi()

    override suspend fun executeAlchemyPayBestPrice(
        sign: String,
        timestamp: String,
        url: String
    ): Response<AlchemyPayResponseModel> =
        apiService.executeAlchemyPayBestPrice(signin = sign, timestamp = timestamp, url = url)

    override suspend fun executeOnMetaBestSellPriceApi(
        url: String,
        body: OnMetaSellBestPriceModel?
    ): Response<OnMetaBestPriceResponseModel> =
        apiService.executeOnMetaSellBestPriceApi(url = url, body = body)

    override suspend fun executeOnRampSellBestPrice(
        headerOnRampSignKey: String,
        headerPayLoad: String,
        url: String,
        body: OnRampSellBestPriceRequestModel?
    ): Response<OnRampResponseModel> = apiService.executeOnRampSellBestPrice(
        headerOnRampSignKey = headerOnRampSignKey,
        headerPayLoad = headerPayLoad,
        url = url,
        body = body
    )

    override suspend fun executeCoinDetailApi(url: String): Response<CoinDetailModel> =
        apiService.executeCoinDetail(url)

    override suspend fun executeApproveUsingOkx(
        url: String,
        headerOkxSignKey: String,
        headerTimeStamp: String
    ): Response<OkxApproveResponse> = apiService.executeApproveUsingOkx(
        url = url,
        okAccessSignKey = headerOkxSignKey,
        timestamp = headerTimeStamp
    )


    override suspend fun getBitcoinWalletBalance(address: String): Response<ResponseBody> =
        apiService.getBitcoinWalletBalance(address)

    override suspend fun registerWallet(
        walletAddress: String,
        deviceId: String,
        fcmToken: String
    ): Response<ResponseBody> =
        apiService.registerWallet(walletAddress, 0, deviceId, fcmToken)

    override suspend fun registerWalletMaster(
        deviceId: String,
        walletAddress: String,
        referralCode: String
    ): Response<ResponseBody> =
        apiService.registerWalletMaster(deviceId, 0, walletAddress, referralCode)

    override suspend fun setWalletActive(
        walletAddress: String,
        receiverAddress: String
    ): Response<ResponseBody> =
        apiService.setWalletActive(walletAddress, receiverAddress)

    override suspend fun sendBTCTransaction(
        privateKey: String,
        value: String,
        toAddress: String,
        env: String,
        fromAddress: String
    ): Response<ResponseBody> =
        apiService.sendBTCTransaction(privateKey, value, toAddress, env, fromAddress)


    override suspend fun domainCheck(domainSearchModel: DomainSearchModel) =
        apiService.domainCheck(body = domainSearchModel)


    override suspend fun getAllActiveTokenList(url: String): Response<MutableList<ModelActiveWalletToken>> =
        apiService.getAllActiveTokenList(url)

}