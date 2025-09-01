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
import com.app.plutope.model.ReferralCodesWrapperModel
import com.app.plutope.model.TokenListImageModel
import com.app.plutope.model.TransactionHistoryDetail
import com.app.plutope.model.TransactionHistoryModel
import com.app.plutope.model.TransactionHistoryResponse
import com.app.plutope.model.TransactionResponse
import com.app.plutope.model.TransferTraceDetail
import com.app.plutope.model.UnlimitBestPriceModel
import com.app.plutope.ui.fragment.card.card_dashboard.KycLimitResponseModel
import com.app.plutope.ui.fragment.card.card_history.AssetHistoryWrapperModel
import com.app.plutope.ui.fragment.card.card_history.HistoryCardWrapperModel
import com.app.plutope.ui.fragment.card.card_home.KycStatusResponseModel
import com.app.plutope.ui.fragment.card.card_kyc_1.KycGetResponseModel
import com.app.plutope.ui.fragment.card.card_payment_process.CardWalletCurrencyResponseModel
import com.app.plutope.ui.fragment.card.card_payment_process.CreateOfferPaymentResponseModel
import com.app.plutope.ui.fragment.card.card_user_profile.update_card_user_profile.CardUserProfileResponseModel
import com.app.plutope.ui.fragment.card.card_user_profile.update_card_user_profile.UpdateProfileRequestModel
import com.app.plutope.ui.fragment.card.card_wallet_list.CardWalletListResponseModel
import com.app.plutope.ui.fragment.card.card_wallet_list.add_card_detail.AddCardRequestModel
import com.app.plutope.ui.fragment.card.card_wallet_list.buy_crypto.PayInCardRatesResponseModel
import com.app.plutope.ui.fragment.card.card_wallet_list.buy_crypto.PayInCreateOfferResponse
import com.app.plutope.ui.fragment.card.card_wallet_list.buy_crypto.PayInOfferResponseModel
import com.app.plutope.ui.fragment.card.card_wallet_list.buy_crypto.webview_pay_in_offer.PayInCallbackResponse
import com.app.plutope.ui.fragment.card.exchange.ExchangeCreateOfferResponseModel
import com.app.plutope.ui.fragment.card.exchange.ExchangeCurrencyPairResponseModel
import com.app.plutope.ui.fragment.card.first_touch_card.CardPriceResponseModel
import com.app.plutope.ui.fragment.card.my_card.AddNewCardResponseModel
import com.app.plutope.ui.fragment.card.my_card.CardDashboardImagesModel
import com.app.plutope.ui.fragment.card.my_card.CardListResponseModel
import com.app.plutope.ui.fragment.card.my_card.add_new_card.CardAddRequestModel
import com.app.plutope.ui.fragment.card.my_card.cardsDetail.CardDetailViewResponseModel
import com.app.plutope.ui.fragment.card.my_card.cardsDetail.card_history.CardHistoryByCardID
import com.app.plutope.ui.fragment.card.my_card.top_up_card.CreatePayloadOfferResponseModel
import com.app.plutope.ui.fragment.card.my_card.top_up_card.PayloadCardDetailResponseModel
import com.app.plutope.ui.fragment.card.my_card.top_up_card.PayloadTopUpRequestModel
import com.app.plutope.ui.fragment.card.my_card.update_card_address.CityResponseModel
import com.app.plutope.ui.fragment.card.my_card.update_card_address.CountryStateModel
import com.app.plutope.ui.fragment.card.my_card.update_card_address.UpdateCardAddressRequestModel
import com.app.plutope.ui.fragment.card.send_crypto.send.RequestSendModel
import com.app.plutope.ui.fragment.card.send_crypto.send.SendFeeCurrencyResponseModel
import com.app.plutope.ui.fragment.card.send_crypto.send.SendWalletModel
import com.app.plutope.ui.fragment.card.send_crypto.send.SendWalletValidationModel
import com.app.plutope.ui.fragment.card.sign_up_login.confirmation_sms.ForgetPasswordSmsCodeModel
import com.app.plutope.ui.fragment.card.sign_up_login.confirmation_sms.ResendPhoneOtpRequestModel
import com.app.plutope.ui.fragment.card.sign_up_login.confirmation_sms.SendSmsCodeModel
import com.app.plutope.ui.fragment.card.sign_up_login.confirmation_sms.VerificationResponseModel
import com.app.plutope.ui.fragment.card.sign_up_login.register.CardUserModelSignUP
import com.app.plutope.ui.fragment.card.withdraw.PayOutCardRatesResponseModel
import com.app.plutope.ui.fragment.card.withdraw.PayOutCreateOfferResponseModel
import com.app.plutope.ui.fragment.card.withdraw.PayoutExecuteOfferResponseModel
import com.app.plutope.ui.fragment.card_beta.user_management.bank.subscription_plan.SubPlanModel
import com.app.plutope.ui.fragment.card_beta.user_management.card_dashboard.model.AccountModel
import com.app.plutope.ui.fragment.card_beta.user_management.card_dashboard.model.CryptoModel
import com.app.plutope.ui.fragment.card_beta.user_management.card_dashboard.model.KYCVerificationModel
import com.app.plutope.ui.fragment.card_beta.user_management.history_v2.model.TransactionCardHistoryModel
import com.app.plutope.ui.fragment.card_beta.user_management.history_v2.model.TransactionDetailModel
import com.app.plutope.ui.fragment.card_beta.user_management.phone_number.ForgotPassRequestModel
import com.app.plutope.ui.fragment.card_beta.user_management.phone_number.PhoneNumberRequestModel
import com.app.plutope.ui.fragment.card_beta.user_management.sign_up.SignInRequestModel
import com.app.plutope.ui.fragment.card_beta.user_management.sign_up.SignUpRequestModel
import com.app.plutope.ui.fragment.card_beta.user_management.sign_up.SignUpResponseModel
import com.app.plutope.ui.fragment.dashboard.GenerateTokenModel
import com.app.plutope.ui.fragment.ens.DomainSearchModel
import com.app.plutope.ui.fragment.ens.ENSListModel
import com.app.plutope.ui.fragment.my_referrals.MyReferralWrapperModel
import com.app.plutope.ui.fragment.transactions.buy.buy_btc.BuyRequestModel
import com.app.plutope.ui.fragment.transactions.buy.buy_btc.BuyResponseModel
import com.app.plutope.ui.fragment.transactions.buy.buy_detail.TransactionMoralisResponse
import com.app.plutope.ui.fragment.transactions.buy.buy_detail.TransferHistoryModel
import com.app.plutope.ui.fragment.transactions.sell.SellProviderModel
import com.app.plutope.ui.fragment.transactions.swap.SwapQuoteRequestModel
import com.app.plutope.ui.fragment.transactions.swap.SwapQuoteResponseModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.ExodusSwapResponseModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.RangoSwapResponseModel
import com.app.plutope.utils.constant.ALCHEMY_PAY_APP_ID
import com.app.plutope.utils.constant.BASE_URL_PLUTO_PE
import com.app.plutope.utils.constant.CARD_BETA_BASE_URL
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
import com.app.plutope.utils.constant.VAULT_SAND_BOX_URL
import com.app.plutope.utils.constant.VAULT_SEND_BOX_URL_VERSION_V1
import com.app.plutope.utils.constant.VAULT_SEND_BOX_URL_VERSION_V2
import com.app.plutope.utils.constant.VAULT_SEND_BOX_URL_VERSION_V3
import com.app.plutope.utils.constant.VAULT_SEND_BOX_URL_VERSION_V4
import com.app.plutope.utils.constant.VAULT_X_MERCHANT_ID
import com.app.plutope.utils.constant.VAULT_X_VERSION
import com.app.plutope.utils.constant.VaultCardProgram
import com.app.plutope.utils.constant.getBarrierToken
import com.app.plutope.utils.constant.moralis_access_key
import com.app.plutope.utils.constant.partnerID
import com.app.plutope.utils.extras.PreferenceHelper
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url


interface ApiService {


    @GET("info?")
    suspend fun getCoinsAssets(
        @Query("symbol") symbol: String
    ): Response<Info>

    @GET
    suspend fun getGenerateToken(
        @Url url: String
    ): Response<GenerateTokenModel>

    @GET
    suspend fun getTransactionHistory(
        @Header("x-api-key") header: String = "{x-api-key}",
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
        @Header("OK-ACCESS-KEY") okAccessKey: String = OKX_API_KEY,
        @Header("OK-ACCESS-SIGN") okAccessSignKey: String = "",
        @Header("OK-ACCESS-TIMESTAMP") timestamp: String = "",
        @Header("OK-ACCESS-PASSPHRASE") passphrase: String = OKX_PASSPHRASE,
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
    ): Response<MutableList<NFTListModel>>


    //coingecko markets  api call
    @GET
    suspend fun executeCoinGeckoMarketsApi(@Url url: String): Response<MutableList<CoinGeckoMarketsResponse>>


    //currency List coin market cap
    @GET
    suspend fun executeCurrencyListApi(
        @Url url: String,
        @Header("X-CMC_PRO_API_KEY") header: String = COIN_MARKET_CAP_API_KEY
    ): Response<CoinMarketCurrencyResponse>


    @PATCH
    suspend fun executeUpdateCardCurrencyApi(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Header("accept") accept: String = "text/plain",
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "customer/profile",
        @Body body: RequestBody?
    ): Response<ResponseBody>


    @GET
    suspend fun executeOkLinkTransactionList(
        @Url url: String,
        @Header("OK-ACCESS-KEY") header: String = OK_LINK_ACCESS_KEY
    ): Response<TransactionHistoryResponse>


    @GET
    suspend fun executeHistoryCardTransactions(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "history/operations?",
        @Query("offset") offset: String,
        @Query("size") size: String,
    ): Response<HistoryCardWrapperModel>


    @GET(/*BASE_URL_PLUTO_PE+"user/history-operations"*/)
    suspend fun getAssetHistory(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "history/operations?",
        @Query("currencyFilter") currencyFilter: List<String>,
        @Query("offset") offset: String = "0",
        @Query("size") size: String = "50",
    ): Response<AssetHistoryWrapperModel>

    @GET(BASE_URL_PLUTO_PE + "user/card-dashboard-images")
    suspend fun getCardDashboardImages(): Response<CardDashboardImagesModel>

    @GET(/*BASE_URL_PLUTO_PE+"user/history-by-cardId"*/)
    suspend fun executeHistoryByCardId(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "history/card/",
        @Query("offset") offset: String,
        @Query("size") size: String,
        @Query("cp") cardProgram: String = VaultCardProgram
    ): Response<CardHistoryByCardID>

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
    suspend fun executeTokenImageListApi(@Url url: String = TOKEN_IMAGE_LIST_API): Response<MutableList<TokenListImageModel>>

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
    suspend fun executeCoinDetail(@Url url: String): Response<CoinDetailModel>


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
    @POST(BASE_URL_PLUTO_PE + "rango-swap-quote")
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

    @POST(BASE_URL_PLUTO_PE + "exodus-swap-update-orders")
    suspend fun exodusSwapUpdateOrderCall(
        @Body body: RequestBody
    ): Response<ExodusSwapResponseModel>


    @GET
    suspend fun exodusTransactionStatusCall(
        @Url url: String,
    ): Response<ExodusSwapResponseModel>


    @FormUrlEncoded
    @POST(BASE_URL_PLUTO_PE + "rango-swap-exchange")
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

    @GET(BASE_URL_PLUTO_PE + "user/update-claim/{walletAddress}")
    suspend fun updateNonClaimedTokens(
        @Path("walletAddress") walletAddress: String
    ): Response<ResponseBody>

    @GET(BASE_URL_PLUTO_PE + "user/my-refferal-user/{walletAddress}")
    suspend fun getMyReferrals(
        @Path("walletAddress") walletAddress: String
    ): Response<MyReferralWrapperModel>

    @GET(BASE_URL_PLUTO_PE + "user/my-referral-codes/{walletAddress}")
    suspend fun getMyUserCode(
        @Path("walletAddress") walletAddress: String
    ): Response<ReferralCodesWrapperModel>


    @FormUrlEncoded
    @POST(BASE_URL_PLUTO_PE + "user/register-user")
    suspend fun registerWallet(
        @Field("walletAddress") address: String,
        @Field("appType") appType: Int,
        @Field("deviceId") deviceId: String,
        @Field("fcmToken") fcmToken: String,
        @Field("walletAction") type: String,
        @Field("referral_code") referralCode: String
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


    @GET
    suspend fun getAllActiveTokenList(
        @Url url: String,
    ): Response<MutableList<ModelActiveWalletToken>>


    @POST
    suspend fun transactionTrackActivityLog(
        @Url url: String = BASE_URL_PLUTO_PE + "wallet-activity-log",
        @Body body: TransferTraceDetail?
    ): Response<ResponseBody>


    @POST
    suspend fun swapQuoteSingleCall(
        @Url url: String = BASE_URL_PLUTO_PE + "swap-exchange-okx-rangoswap-quote",
        @Body body: SwapQuoteRequestModel?
    ): Response<SwapQuoteResponseModel>

    @POST
    suspend fun buyQuoteSingleCall(
        @Url url: String = BASE_URL_PLUTO_PE + "quote-buy",
        @Body body: BuyRequestModel?
    ): Response<BuyResponseModel>


    /**
     * Card module api start from here
     * **/

    @POST
    suspend fun cardUserSignUp(
        @Header("X-Merchant-ID") xMerchantId: String = VAULT_X_MERCHANT_ID,
        @Header("X-Version") xVersion: String = VAULT_X_VERSION,
        @Url url: String,
        @Body body: CardUserModelSignUP?
    ): Response<ResponseBody>

    @POST
    suspend fun cardUserLogout(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SAND_BOX_URL + "signout",
    ): Response<ResponseBody>

    @POST
    suspend fun cardUserForgetPassword(
        @Header("X-Fingerprint") xFingerPrint: String = "1234454",
        @Header("X-Version") xVersion: String = VAULT_X_VERSION,
        @Header("X-Merchant-ID") xMerchantId: String = VAULT_X_MERCHANT_ID,
        @Url url: String,
        @Body body: CardUserModelSignUP?
    ): Response<ResponseBody>

    @POST
    suspend fun cardUserVarificationCodeConfirm(
        @Header("X-Merchant-ID") xMerchantId: String = VAULT_X_MERCHANT_ID,
        @Url url: String,
        @Body body: SendSmsCodeModel?
    ): Response<VerificationResponseModel>

    @POST
    suspend fun cardUserForgetPasswordVerificationCodeConfirm(
        @Header("X-Merchant-ID") xMerchantId: String = VAULT_X_MERCHANT_ID,
        @Header("X-Version") xVersion: String = VAULT_X_VERSION,
        @Url url: String,
        @Body body: ForgetPasswordSmsCodeModel?
    ): Response<VerificationResponseModel>


    @PUT
    suspend fun cardUserAddEmail(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Body body: RequestBody?
    ): Response<ResponseBody>

    @POST
    suspend fun cardUserChangeForgetPassword(
        @Header("X-Merchant-ID") xMerchantId: String = VAULT_X_MERCHANT_ID,
        @Header("X-Version") xVersion: String = VAULT_X_VERSION,
        @Url url: String,
        @Body body: ForgetPasswordSmsCodeModel?
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST
    suspend fun cardUserLogin(
        @Header("X-Merchant-ID") xMerchantId: String = VAULT_X_MERCHANT_ID,
        @Url url: String,
        // @Body body: CardUserModelSignUP?
        @Field("number") number: String,
        @Field("password") password: String,
        @Field("grant_type") grant_type: String = "mobile_phone",
    ): Response<VerificationResponseModel>


    @GET
    suspend fun getCardUserProfile(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "customer/profile",
    ): Response<CardUserProfileResponseModel>

    @PATCH
    suspend fun updateCardUserProfile(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Header("accept") accept: String = "text/plain",
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "customer/profile",
        @Body body: UpdateProfileRequestModel?
    ): Response<ResponseBody>

    @POST
    suspend fun resendEmailPhoneVerification(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "mobile/email/verify/resend",
    ): Response<ResponseBody>

    @POST
    suspend fun resendPhoneVerification(
        @Header("X-Fingerprint") xFingerPrint: String = "1234444",
        @Header("X-Merchant-ID") xMerchantId: String = VAULT_X_MERCHANT_ID,
        @Header("X-Version") xVersion: String = VAULT_X_VERSION,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "mobile/phone/verify/resend",
        @Body body: ResendPhoneOtpRequestModel?
    ): Response<ResponseBody>

    @POST(BASE_URL_PLUTO_PE + "user/kyc-start")
    suspend fun startKyc1(
        @Header("auth") auth: String = PreferenceHelper.getInstance().cardAccessToken,
        @Query("platform") platform: String = "COMMON",
        @Query("deviceType") deviceType: String = "Android"
    ): Response<ResponseBody>

    @POST(BASE_URL_PLUTO_PE + "user/kyc-update-status")
    suspend fun finishKyc1(
        @Header("auth") auth: String = PreferenceHelper.getInstance().cardAccessToken,
        // @Query("platform") platform: String = "COMMON",
        @Body body: RequestBody
    ): Response<ResponseBody>

    @GET
    suspend fun getKyc(
        @Header("auth") auth: String = PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
    ): Response<KycGetResponseModel>

    @GET
    suspend fun getKycStatus(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "customer/kyc/data",
    ): Response<KycStatusResponseModel>


    @GET()
    suspend fun getCardWallet(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "wallets",
    ): Response<CardWalletListResponseModel>

    @POST
    suspend fun createCardWallet(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "wallets",
        @Body currencies: RequestBody
    ): Response<CardWalletListResponseModel>

    @GET()
    suspend fun getCardList(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "card/list"
    ): Response<CardListResponseModel>


    @POST
    suspend fun addNewCardRequest(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "card/card-requests",
        @Query("cp") cardProgram: String = VaultCardProgram,
        @Body body: CardAddRequestModel?
    ): Response<AddNewCardResponseModel>

    @POST
    suspend fun cancelCardRequest(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Query("cp") cardProgram: String = VaultCardProgram
    ): Response<ResponseBody>


    @PUT
    suspend fun updateCardAddress(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Body body: UpdateCardAddressRequestModel,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<ResponseBody>

    @PUT
    /*(BASE_URL_PLUTO_PE + "user/change-password")*/
    suspend fun changePassword(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "mobile/password/change",
        @Body body: RequestBody,
    ): Response<ResponseBody>


    @GET(/*BASE_URL_PLUTO_PE + "user/card-price?UserAgent=browser"*/)
    suspend fun getCardPrice(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Header("User-Agent") userAgent: String = "vault/4.0(508) dart/3.2 (dart:io) ios/17.3.1; iphone 9da12fa6-716c-4cdc-a24c",
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "card/prices",
    ): Response<CardPriceResponseModel>

    @GET
    suspend fun getKycLimit(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V1 + "kyc/limits",
    ): Response<KycLimitResponseModel>


    @POST
    suspend fun cardAdditionalPersonalInfoCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V3 + "card/additional-personal-info",
        @Body body: RequestBody,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<ResponseBody>

    @GET
    suspend fun getWalletCurrencyPrice(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<CardWalletCurrencyResponseModel>

    @POST
    suspend fun cardRequestPaymentOffer(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Query("cp") cardProgram: String = VaultCardProgram
    ): Response<CreateOfferPaymentResponseModel>

    @POST
    suspend fun cardRequestPaymentOfferExecute(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Query("cp") cardProgram: String = VaultCardProgram
    ): Response<ResponseBody>

    @POST
    suspend fun cardNumberDecryptionCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Body body: RequestBody,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<CardDetailViewResponseModel>

    @GET
    suspend fun getDetailVerificationCode(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<ResponseBody>

    @POST
    suspend fun cardAllDecryptionCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Body body: RequestBody,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<CardDetailViewResponseModel>

    @POST
    suspend fun changePin(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Body body: RequestBody,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<ResponseBody>


    @GET
    suspend fun getCardPayloadData(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<PayloadCardDetailResponseModel>


    @POST
    suspend fun cardPayloadTopUpCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Body body: PayloadTopUpRequestModel,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<CreatePayloadOfferResponseModel>

    @POST
    suspend fun cardPayloadTopUpConfirmCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        // @Body body: PayloadTopUpRequestModel,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<ResponseBody>


    @POST
    suspend fun cardSoftBlockCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
        @Body body: RequestBody,
        @Query("cp") cardProgram: String = VaultCardProgram,
    ): Response<ResponseBody>


    @GET
    suspend fun getPayInCardDetail(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V3 + "payin/data",
    ): Response<PayInCardRatesResponseModel>

    /*  @GET
     suspend fun getPayInCardDetail(
         @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
         @Url url: String = VAULT_SEND_BOX_URL_VERSION_V4 + "payout/data",
     ): Response<PayInCardRatesResponseModel>

    */
    @POST
    suspend fun payInAddCardCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V3 + "payin/card",
        @Body body: AddCardRequestModel
    ): Response<ResponseBody>


    @POST
    suspend fun payInCreateOfferCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V3 + "payin/offer",
        @Body body: RequestBody
    ): Response<PayInCreateOfferResponse>


    @POST
    suspend fun payInOfferExecuteCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V3 + "payin/pay/0",
        @Body body: RequestBody
    ): Response<PayInOfferResponseModel>


    @GET
    suspend fun getSendFeeCurrency(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V1 + "wallet/send/fee/",
        @Query("amount") amount: String,
        @Query("address") address: String?,
        @Query("phone") phone: String?,
    ): Response<SendFeeCurrencyResponseModel>


    @POST
    suspend fun sendWalletValidation(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V1 + "wallet/send/validate",
        @Body body: RequestSendModel
    ): Response<SendWalletValidationModel>

    @POST
    suspend fun sendWalletCrypto(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V1 + "wallet/send",
        @Body body: RequestSendModel
    ): Response<SendWalletModel>

    @GET
    suspend fun exchangeCurrencyPairCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V2 + "exchange/currencies",
    ): Response<ExchangeCurrencyPairResponseModel>

    @POST
    suspend fun exchangeCurrencyCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V1 + "mobile/exchange/offer",
        @Body body: RequestBody
    ): Response<ExchangeCreateOfferResponseModel>

    @PUT
    suspend fun exchangeCurrencyExecuteCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Header("X-Sdk-Version") xSdkVersion: String = "payoutCard=1.2",
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V1 + "mobile/exchange/offer",
    ): Response<ResponseBody>


    @GET
    suspend fun payInPayCallback(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String,
    ): Response<PayInCallbackResponse>


    @GET
    suspend fun getPayOutCardDetail(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V4 + "payout/data",
    ): Response<PayOutCardRatesResponseModel>

    @POST
    suspend fun payOutAddCardCall(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V4 + "payout/card",
        @Body body: AddCardRequestModel
    ): Response<ResponseBody>

    @POST
    suspend fun payOutCreateOffer(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Url url: String = VAULT_SEND_BOX_URL_VERSION_V4 + "payout/offer",
        @Body body: RequestBody
    ): Response<PayOutCreateOfferResponseModel>


    @POST
    suspend fun payOutExecuteOffer(
        @Header("authorization") authorization: String = "Bearer " + PreferenceHelper.getInstance().cardAccessToken,
        @Header("X-Sdk-Version") xSdkVersion: String = "payoutCard=1.2",
        @Url url: String = "",
    ): Response<PayoutExecuteOfferResponseModel>


    @GET("https://countriesnow.space/api/v0.1/countries/states")
    suspend fun getCountryStateList(): Response<CountryStateModel>

    @GET("https://countriesnow.space/api/v0.1/countries/state/cities")
    suspend fun getCityList(body: RequestBody): Response<CityResponseModel>

    @POST
    suspend fun cardBetaSignUp(
        @Header("partnerId") partnerId: String = partnerID,
        @Url url: String = CARD_BETA_BASE_URL + "reg/user",
        @Body body: SignUpRequestModel?
    ): Response<SignUpResponseModel>

    @POST
    suspend fun resetPasswordRequest(
        @Header("partnerId") partnerId: String = partnerID,
        @Url url: String = CARD_BETA_BASE_URL + "reg/user/resetPasswordRequest",
        @Body body: ForgotPassRequestModel
    ): Response<ResponseBody>


    @POST
    suspend fun cardBetaLogin(
        @Url url: String = CARD_BETA_BASE_URL + "reg/auth/token",
        @Body body: SignInRequestModel?
    ): Response<SignUpResponseModel>

    @POST
    suspend fun createPhone(
        @Url url: String = CARD_BETA_BASE_URL + "reg/user/phone",
        @Header("Authorization") authorization: String? = getBarrierToken(),
        @Body model: PhoneNumberRequestModel
    ): Response<ResponseBody>

    @POST
    suspend fun requestConfirmPhone(
        @Url url: String = CARD_BETA_BASE_URL + "reg/user/requestConfirm",
        @Header("Authorization") authorization: String? = getBarrierToken(),
        @Body model: PhoneNumberRequestModel
    ): Response<ResponseBody>


    @GET
    suspend fun getCurrencyList(
        @Url url: String = CARD_BETA_BASE_URL + "currency/currency",
        @Header("Authorization") authorization: String? = getBarrierToken(),
    ): Response<MutableList<CryptoModel>>

    @GET
    suspend fun getAccountList(
        @Url url: String = CARD_BETA_BASE_URL + "wallet/account",
        @Header("Authorization") authorization: String? = getBarrierToken(),
    ): Response<MutableList<AccountModel>>

    @POST
    suspend fun createAccount(
        @Url url: String = CARD_BETA_BASE_URL + "wallet/account",
        @Header("Authorization") authorization: String? = getBarrierToken(),
    ): Response<AccountModel>

    @POST
    suspend fun getKycVerification(
        @Url url: String = CARD_BETA_BASE_URL + "reg/verification",
        @Header("Authorization") authorization: String? = getBarrierToken(),
        @Body body: RequestBody
    ): Response<KYCVerificationModel>


    @GET
    suspend fun subscriptionPlanList(
        @Url url: String = CARD_BETA_BASE_URL + "reg/subscription/details/available",
        @Header("Authorization") authorization: String? = getBarrierToken(),
    ): Response<MutableList<SubPlanModel>>


    @GET
    suspend fun getTransactions(
        @Url url: String = CARD_BETA_BASE_URL + "wallet/balance/log",
        @Header("Authorization") authorization: String? = getBarrierToken(),
        @Query("accountIds") accountIds: List<String>,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<TransactionCardHistoryModel>

    @GET
    suspend fun getTransactionsHistoryDetail(
        @Url url: String = CARD_BETA_BASE_URL + "wallet/balance/log",
        @Header("Authorization") authorization: String? = getBarrierToken(),
    ): Response<TransactionDetailModel>


    @GET(BASE_URL_PLUTO_PE + "get-all-sell-provider")
    suspend fun getAllSellProvider(): Response<SellProviderModel>


    @GET
    suspend fun executeMoralisTransactionList(
        @Url url: String,
        @Header("x-api-key") header: String = moralis_access_key
    ): Response<TransactionMoralisResponse>


    @GET
    suspend fun executeTransactionHistoryList(
        @Url url: String,
    ): Response<TransferHistoryModel>


}