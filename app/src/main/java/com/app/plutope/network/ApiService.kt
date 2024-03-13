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
import com.app.plutope.utils.constant.ALCHEMY_PAY_APP_ID
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE
import com.app.plutope.utils.constant.CHANGE_NOW_API_KEY
import com.app.plutope.utils.constant.COIN_GEKO_COIN_LIST_API
import com.app.plutope.utils.constant.COIN_MARKET_CAP_API_KEY
import com.app.plutope.utils.constant.NFT_MORALIS_API_KEY
import com.app.plutope.utils.constant.OKX_API_KEY
import com.app.plutope.utils.constant.OKX_HEADER_SOURCE
import com.app.plutope.utils.constant.OKX_PASSPHRASE
import com.app.plutope.utils.constant.OK_LINK_ACCESS_KEY
import com.app.plutope.utils.constant.ON_MELD_KEY
import com.app.plutope.utils.constant.ON_META_API_KEY
import com.app.plutope.utils.constant.ON_RAMP_API_KEY
import com.app.plutope.utils.constant.TOKEN_IMAGE_LIST_API
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface ApiService {


    @GET("info?")
    suspend fun getCoinsAssets(
        @Query("symbol") symbol: String
    ): Response<Info>

    @GET
    suspend fun getAssets(
        @Header("X-CMC_PRO_API_KEY") header: String = COIN_MARKET_CAP_API_KEY,
        @Url url: String
    ): Response<ResponseBody>

    @GET
    suspend fun getTransactionHistory(
        @Header("x-api-key") header: String = "292e27401187168579f797d458bc4e2c748ac7cb",
        @Url url: String
    ): Response<TransactionResponse>


    @POST
    suspend fun executeExchange(
        @Header("x-changenow-api-key") header: String = CHANGE_NOW_API_KEY,
        @Url url: String,
        @Body body: ExchangeRequestModel?
    ): Response<ExchangeResponseModel>

    //get Exchange status api
    @GET
    suspend fun executeExchangeStatus(
        @Header("x-changenow-api-key") header: String = CHANGE_NOW_API_KEY,
        @Url url: String
    ): Response<ExchangeStatusResponse>

    @GET
    suspend fun estimatedMinChangeNow(
        @Url url: String
    ): Response<EstimateMinOnChangeValue>

    @GET("https://api.onramp.money/onramp/api/v3/buy/public/coinNetworks?")
    suspend fun getOnRampDetail(
        @Query("coinCode") coinCode: String,
        @Query("fiatType") fiatType: String = "INR"
    ): Response<ProviderOnRampPriceDetail>


    //get transaction history
    @GET
    suspend fun executeTransactionHistory(
        @Url url: String,
        @Header("x-changenow-api-key") header: String = CHANGE_NOW_API_KEY
    ): Response<TransactionHistoryModel>

    //swap using okx
    @GET()
    suspend fun executeSwapUsingOkx(
        @Url url: String,
        @Header("OK-ACCESS-KEY") okAccessKey:String = OKX_API_KEY,
        @Header("OK-ACCESS-SIGN") okAccessSignKey:String = "",
        @Header("OK-ACCESS-TIMESTAMP") timestamp: String="",
        @Header("OK-ACCESS-PASSPHRASE") passphrase: String=OKX_PASSPHRASE,
        @Header("source") header: String = OKX_HEADER_SOURCE
    ): Response<OkxSwapResponse>

    //change now pair swap
    @GET
    suspend fun executeAvailablePairs(
        @Url url: String,
        @Header("x-changenow-api-key") header: String = CHANGE_NOW_API_KEY
    ): Response<MutableList<AvailablePairsResponseModel>>

    //coingecko market chart api
    @GET
    suspend fun executeCoinGeckoMarketChartApi(@Url url: String): Response<CoinGeckoMarketChartResponse>

    //nft listing
    @GET
    suspend fun executeNftListingApi(
        @Url url: String,
        @Header("x-api-key") header: String = NFT_MORALIS_API_KEY
    ): Response<NFTListModel>


    //coingecko markets  api call
    @GET
    suspend fun executeCoinGeckoMarketsApi(@Url url: String): Response<MutableList<CoinGeckoMarketsResponse>>


    //currency List coin market cap
    @GET
    suspend fun executeCurrencyListApi(
        @Url url: String,
        @Header("X-CMC_PRO_API_KEY") header: String = COIN_MARKET_CAP_API_KEY
    ): Response<CoinMarketCurrencyResponse>

    @GET
    suspend fun executeOkLinkTransactionList(
        @Url url: String,
        @Header("OK-ACCESS-KEY") header: String = OK_LINK_ACCESS_KEY
    ): Response<TransactionHistoryResponse>

    @GET
    suspend fun executeOkLinkTransactionDetail(
        @Url url: String,
        @Header("OK-ACCESS-KEY") header: String = OK_LINK_ACCESS_KEY
    ): Response<TransactionHistoryDetail>


    @POST
    suspend fun executeOnMetaBestPriceApi(
        @Header("x-api-key") header: String = ON_META_API_KEY,
        @Url url: String,
        @Body body: OnMetaBestPriceModel?
    ): Response<OnMetaBestPriceResponseModel>

    @GET
    suspend fun executeChangeNowBestPrice(
        @Url url: String,
        @Header("X-CHANGENOW-API-KEY") header: String = CHANGE_NOW_API_KEY
    ): Response<ChangeNowBestPriceResponse>



    @POST
    suspend fun executeOnRampBestPrice(
        @Header("X-ONRAMP-APIKEY") headerOnRampApiKey: String = ON_RAMP_API_KEY,
        @Header("X-ONRAMP-SIGNATURE") headerOnRampSignKey: String,
        @Header("X-ONRAMP-PAYLOAD") headerPayLoad: String,
        @Url url: String,
        @Body body: OnRampBestPriceRequestModel?
    ): Response<OnRampResponseModel>

    @POST
    suspend fun executeOnMeld(
        @Header("Authorization") authorization: String = "BASIC $ON_MELD_KEY",
        @Url url: String,
        @Body body: MeldRequestModel
    ): Response<MeldResponseModel>

    @GET
    suspend fun executeCoinListApi(@Url url: String = COIN_GEKO_COIN_LIST_API): Response<ResponseBody>

    @GET
    suspend fun executeTokenImageListApi(@Url url: String = TOKEN_IMAGE_LIST_API): Response<List<TokenListImageModel>>

    @POST
    suspend fun executeAlchemyPayBestPrice(
        @Header("appid") appid: String = ALCHEMY_PAY_APP_ID,
        @Header("sign") signin: String,
        @Header("timestamp") timestamp: String,
        @Url url: String/*,@Body body: MeldRequestModel*/
    ): Response<AlchemyPayResponseModel>

    @POST
    suspend fun executeOnMetaSellBestPriceApi(
        @Header("x-api-key") header: String = ON_META_API_KEY,
        @Url url: String,
        @Body body: OnMetaSellBestPriceModel?
    ): Response<OnMetaBestPriceResponseModel>


    @POST
    suspend fun executeOnRampSellBestPrice(
        @Header("X-ONRAMP-APIKEY") headerOnRampApiKey: String = ON_RAMP_API_KEY,
        @Header("X-ONRAMP-SIGNATURE") headerOnRampSignKey: String,
        @Header("X-ONRAMP-PAYLOAD") headerPayLoad: String,
        @Url url: String,
        @Body body: OnRampSellBestPriceRequestModel?
    ): Response<OnRampResponseModel>

    @GET
    suspend fun executeCoinDetail(@Url url:String) : Response<CoinDetailModel>


    //Swap Approve Okx
    @GET
    suspend fun executeApproveUsingOkx(
        @Url url: String,
        @Header("source") header: String = OKX_HEADER_SOURCE,
        @Header("OK-ACCESS-KEY") okAccessKey: String = OKX_API_KEY,
        @Header("OK-ACCESS-SIGN") okAccessSignKey: String = "",
        @Header("OK-ACCESS-TIMESTAMP") timestamp: String = "",
        @Header("OK-ACCESS-PASSPHRASE") passphrase: String = OKX_PASSPHRASE
    ): Response<OkxApproveResponse>


    /**
     * Start new provider api call of Unlimit
     * */

    /*  @GET("onramp/v1/configuration")
      suspend fun getConfiguration(
          @Header("Accept") acceptHeader: String = "application/json, application/xml, multipart/form-data",
          @Header("api-key") apiKeyHeader: String = "123",
          @Header("signature") signatureHeader: String = "2b6b6c58d175ec6bd13c92a17d262fce9336fe1bb41fc1bae0753927c0bbcf2d"
      ): Response<YourResponseType>
  */

    @GET
    suspend fun executeUnlimitBestPrice(
        @Url url: String,
    ): Response<UnlimitBestPriceModel>


    @FormUrlEncoded
    @POST("https://plutope.app/api/rango-swap-quote")
    suspend fun rangSwapQuoteCall(
        @Field("fromBlockchain") fromBlockchain: String,
        @Field("fromTokenSymbol") fromTokenSymbol: String,
        @Field("fromTokenAddress") fromTokenAddress: String,
        @Field("toBlockchain") toBlockchain: String,
        @Field("toTokenSymbol") toTokenSymbol: String,
        @Field("toTokenAddress") toTokenAddress: String,
        @Field("walletAddress") walletAddress: String,
        @Field("price") price: String,
        @Field("fromWalletAddress") fromWalletAddress: String,
        @Field("toWalletAddress") toWalletAddress: String,

        ): Response<RangSwapQuoteModel>

    /*  @FormUrlEncoded
      @POST("https://plutope.app/api/rango-swap")
      suspend fun rangoSwapSubmitCall(
          @Field("fromBlockchain") fromBlockchain: String,
          @Field("fromTokenSymbol") fromTokenSymbol: String,
          @Field("fromTokenAddress") fromTokenAddress: String,
          @Field("toBlockchain") toBlockchain: String,
          @Field("toTokenSymbol") toTokenSymbol: String,
          @Field("toTokenAddress") toTokenAddress: String,
          @Field("walletAddress") walletAddress: String,
          @Field("price") price: String,

          ): Response<RangoSwapResponseModel>*/


    @FormUrlEncoded
    @POST("https://plutope.app/api/rango-swap-exchange")
    suspend fun rangoSwapSubmitCall(
        @Field("fromBlockchain") fromBlockchain: String,
        @Field("fromTokenSymbol") fromTokenSymbol: String,
        @Field("fromTokenAddress") fromTokenAddress: String,
        @Field("toBlockchain") toBlockchain: String,
        @Field("toTokenSymbol") toTokenSymbol: String,
        @Field("toTokenAddress") toTokenAddress: String,
        @Field("walletAddress") walletAddress: String,
        @Field("price") price: String,
        @Field("fromWalletAddress") fromWalletAddress: String,
        @Field("toWalletAddress") toWalletAddress: String,

        ): Response<RangoSwapResponseModel>

    @GET(BASE_URL_PLUTO_PE + "btc-balance")
    suspend fun getBitcoinWalletBalance(
        @Query("address") address: String
    ): Response<ResponseBody>


    @FormUrlEncoded
    @POST(BASE_URL_PLUTO_PE + "user/register-user")
    suspend fun registerWallet(
        @Field("walletAddress") address: String,
        @Field("appType") appType: Int,
        @Field("deviceId") deviceId: String,
        @Field("fcmToken") fcmToken: String,
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST(BASE_URL_PLUTO_PE + "admin/set-wallet-active")
    suspend fun setWalletActive(
        @Field("walletAddress") address: String,
        @Field("receiverAddress") receiverAddress: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST(BASE_URL_PLUTO_PE + "btc-transfer")
    suspend fun sendBTCTransaction(
        @Field("privateKey") privateKey: String,
        @Field("value") value: String,
        @Field("toAddress") toAddress: String,
        @Field("env") env: String,
        @Field("fromAddress") fromAddress: String,
    ): Response<ResponseBody>


    @POST
    suspend fun domainCheck(
        @Url url: String = BASE_URL_PLUTO_PE + "domain-check",
        @Body body: DomainSearchModel?
    ): Response<ENSListModel>


    @FormUrlEncoded
    @POST(BASE_URL_PLUTO_PE + "user/wallet-register-master")
    suspend fun registerWalletMaster(
        @Field("deviceId") deviceId: String,
        @Field("deviceType") deviceType: Int,
        @Field("walletAddress") walletAddress: String,
        @Field("referral_code") referralCode: String,
    ): Response<ResponseBody>


    @GET
    suspend fun getAllActiveTokenList(
        @Url url: String,
    ): Response<MutableList<ModelActiveWalletToken>>

}