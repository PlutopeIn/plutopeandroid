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
import com.app.plutope.ui.fragment.my_referrals.MyReferralWrapperModel
import com.app.plutope.ui.fragment.transactions.buy.buy_btc.BuyRequestModel
import com.app.plutope.ui.fragment.transactions.buy.buy_detail.TransactionMoralisResponse
import com.app.plutope.ui.fragment.transactions.buy.buy_detail.TransferHistoryModel
import com.app.plutope.ui.fragment.transactions.sell.SellProviderModel
import com.app.plutope.ui.fragment.transactions.swap.SwapQuoteRequestModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.ExodusSwapResponseModel
import com.app.plutope.ui.fragment.transactions.swap.previewSwap.RangoSwapResponseModel
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class ApiHelperImpl @Inject constructor(private val apiService: ApiService) : ApiHelper {
    override suspend fun getCoinsAssets(
        symbol: String,
    ): Response<Info> = apiService.getCoinsAssets(symbol)

    override suspend fun getGenerateToken(
        url: String
    ): Response<GenerateTokenModel> = apiService.getGenerateToken(url)

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

    override suspend fun exodusSwapUpdateOrderCall(body: RequestBody): Response<ExodusSwapResponseModel> =
        apiService.exodusSwapUpdateOrderCall(body = body)

    override suspend fun exodusTransactionStatusCall(url: String): Response<ExodusSwapResponseModel> =
        apiService.exodusTransactionStatusCall(url = url)

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
        apiService.executeSwapUsingOkx(
            url = url,
            okAccessSignKey = headerOkxSignKey,
            timestamp = headerTimeStamp
        )

    override suspend fun executeAvailablePairs(url: String): Response<MutableList<AvailablePairsResponseModel>> =
        apiService.executeAvailablePairs(url)

    override suspend fun executeCoinGeckoMarketChartApi(url: String): Response<CoinGeckoMarketChartResponse> =
        apiService.executeCoinGeckoMarketChartApi(url)

    override suspend fun executeNftListApi(url: String): Response<MutableList<NFTListModel>> =
        apiService.executeNftListingApi(url)

    override suspend fun estimationMinChangeNow(url: String): Response<EstimateMinOnChangeValue> =
        apiService.estimatedMinChangeNow(url)

    override suspend fun executeCoinGeckoMarketsApi(url: String): Response<MutableList<CoinGeckoMarketsResponse>> =
        apiService.executeCoinGeckoMarketsApi(url)

    override suspend fun executeGetCurrencyApi(url: String): Response<CoinMarketCurrencyResponse> =
        apiService.executeCurrencyListApi(url)

    override suspend fun executeUpdateCardCurrencyApi(body: RequestBody): Response<ResponseBody> =
        apiService.executeUpdateCardCurrencyApi(body = body)

    override suspend fun executeOkLinkTransactionList(url: String): Response<TransactionHistoryResponse> =
        apiService.executeOkLinkTransactionList(url)

    override suspend fun executeHistoryCardTransactions(
        offset: String,
        size: String,
    ): Response<HistoryCardWrapperModel> =
        apiService.executeHistoryCardTransactions(offset = offset, size = size)

    override suspend fun getAssetHistory(
        currencyFilter: List<String>
    ): Response<AssetHistoryWrapperModel> =
        apiService.getAssetHistory(currencyFilter = currencyFilter)

    override suspend fun getCardDashboardImages(): Response<CardDashboardImagesModel> =
        apiService.getCardDashboardImages()

    override suspend fun executeHistoryByCardId(
        url: String,
        offset: String,
        size: String,
        cardId: String,
    ): Response<CardHistoryByCardID> =
        apiService.executeHistoryByCardId(url = url, offset = offset, size = size)

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

    override suspend fun executeTokenImageListApi(): Response<MutableList<TokenListImageModel>> =
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

    override suspend fun updateNonClaimedTokens(walletAddress: String): Response<ResponseBody> =
        apiService.updateNonClaimedTokens(walletAddress)

    override suspend fun getMyReferrals(walletAddress: String): Response<MyReferralWrapperModel> =
        apiService.getMyReferrals(walletAddress)

    override suspend fun getMyUserCode(walletAddress: String): Response<ReferralCodesWrapperModel> =
        apiService.getMyUserCode(walletAddress)

    override suspend fun registerWallet(
        walletAddress: String,
        deviceId: String,
        fcmToken: String,
        type: String,
        referralCode: String
    ): Response<ResponseBody> =
        apiService.registerWallet(walletAddress, 0, deviceId, fcmToken, type, referralCode)

    /*override suspend fun registerWalletMaster(
        deviceId: String,
        walletAddress: String,
        referralCode: String,
    ): Response<ResponseBody> =
        apiService.registerWalletMaster(deviceId, 0, walletAddress, referralCode)*/

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

    override suspend fun transactionTrackActivityLog(body: TransferTraceDetail) =
        apiService.transactionTrackActivityLog(body = body)

    override suspend fun swapQuoteSingleCall(body: SwapQuoteRequestModel) =
        apiService.swapQuoteSingleCall(body = body)

    override suspend fun buyQuoteSingleCall(body: BuyRequestModel) =
        apiService.buyQuoteSingleCall(body = body)


    /**
     * Card module api start from here
     * **/
    override suspend fun cardUserSignUp(
        url: String,
        cardUserModelSignUP: CardUserModelSignUP
    ): Response<ResponseBody> =
        apiService.cardUserSignUp(url = url, body = cardUserModelSignUP)

    override suspend fun cardUserLogout(): Response<ResponseBody> =
        apiService.cardUserLogout()

    override suspend fun cardUserForgetPassword(
        url: String,
        cardUserModelSignUP: CardUserModelSignUP
    ): Response<ResponseBody> =
        apiService.cardUserForgetPassword(url = url, body = cardUserModelSignUP)

    /*
        override suspend fun cardUserLogin(
            url: String,
            cardUserModelSignUP: CardUserModelSignUP
        ): Response<VerificationResponseModel> =
            apiService.cardUserLogin(url = url, body = cardUserModelSignUP)
    */
    override suspend fun cardUserLogin(
        url: String,
        cardUserModelSignUP: CardUserModelSignUP
    ): Response<VerificationResponseModel> =
        apiService.cardUserLogin(
            url = url,
            number = cardUserModelSignUP.phone!!,
            password = cardUserModelSignUP.password!!
        )

    override suspend fun cardUserVarificationCodeConfirm(
        url: String,
        body: SendSmsCodeModel
    ): Response<VerificationResponseModel> =
        apiService.cardUserVarificationCodeConfirm(url = url, body = body)

    override suspend fun cardUserForgetPasswordVerificationCodeConfirm(
        url: String,
        body: ForgetPasswordSmsCodeModel
    ): Response<VerificationResponseModel> =
        apiService.cardUserForgetPasswordVerificationCodeConfirm(url = url, body = body)

    override suspend fun cardUserAddEmail(
        auth: String,
        url: String,
        body: RequestBody
    ): Response<ResponseBody> =
        apiService.cardUserAddEmail(/*auth = auth,*/url = url, body = body)

    /*override suspend fun cardUserChangeForgetPassword(
        auth: String,
        url: String,
        body: RequestBody,
        code: RequestBody,
        password: RequestBody
    ): Response<ResponseBody> =
        apiService.cardUserChangeForgetPassword(url = url, body = body)*/

    override suspend fun cardUserChangeForgetPassword(
        auth: String,
        url: String,
        body: ForgetPasswordSmsCodeModel,

        ): Response<ResponseBody> =
        apiService.cardUserChangeForgetPassword(url = url, body = body)

    override suspend fun getCardUserProfile(
    ): Response<CardUserProfileResponseModel> =
        apiService.getCardUserProfile()

    override suspend fun updateCardUserProfile(
        body: UpdateProfileRequestModel,
    ): Response<ResponseBody> =
        apiService.updateCardUserProfile(body = body)

    override suspend fun resendEmailPhoneVerification(): Response<ResponseBody> =
        apiService.resendEmailPhoneVerification()

    override suspend fun resendPhoneVerification(body: ResendPhoneOtpRequestModel?): Response<ResponseBody> =
        apiService.resendPhoneVerification(body = body)


    override suspend fun startKyc1(): Response<ResponseBody> =
        apiService.startKyc1()

    override suspend fun finishKyc1(body: RequestBody): Response<ResponseBody> =
        apiService.finishKyc1(body = body)

    override suspend fun getKycStatus(): Response<KycStatusResponseModel> =
        apiService.getKycStatus()

    override suspend fun getCardWalletList(): Response<CardWalletListResponseModel> =
        apiService.getCardWallet()

    override suspend fun createCardWallet(tokenList: RequestBody): Response<CardWalletListResponseModel> =
        apiService.createCardWallet(currencies = tokenList)

    override suspend fun getCardList(): Response<CardListResponseModel> =
        apiService.getCardList()

    override suspend fun addNewCardRequest(body: CardAddRequestModel): Response<AddNewCardResponseModel> =
        apiService.addNewCardRequest(body = body)

    override suspend fun cancelCardRequest(url: String): Response<ResponseBody> =
        apiService.cancelCardRequest(url = url)

    override suspend fun updateCardAddress(
        url: String,
        body: UpdateCardAddressRequestModel
    ): Response<ResponseBody> =
        apiService.updateCardAddress(url = url, body = body)

    override suspend fun changePassword(
        body: RequestBody
    ): Response<ResponseBody> =
        apiService.changePassword(body = body)

    override suspend fun getKyc(
        url: String
    ): Response<KycGetResponseModel> =
        apiService.getKyc(url = url)

    override suspend fun getCardPrice(): Response<CardPriceResponseModel> =
        apiService.getCardPrice()

    override suspend fun getKycLimit(): Response<KycLimitResponseModel> =
        apiService.getKycLimit()

    override suspend fun getCountryStateList(): Response<CountryStateModel> =
        apiService.getCountryStateList()

    override suspend fun getCityList(body: RequestBody): Response<CityResponseModel> =
        apiService.getCityList(body)

    override suspend fun cardAdditionalPersonalInfoCall(body: RequestBody): Response<ResponseBody> =
        apiService.cardAdditionalPersonalInfoCall(body = body)

    override suspend fun getWalletCurrencyPrice(url: String): Response<CardWalletCurrencyResponseModel> =
        apiService.getWalletCurrencyPrice(url = url)

    override suspend fun cardRequestPaymentOffer(url: String): Response<CreateOfferPaymentResponseModel> =
        apiService.cardRequestPaymentOffer(url = url)

    override suspend fun cardRequestPaymentOfferExecute(url: String): Response<ResponseBody> =
        apiService.cardRequestPaymentOfferExecute(url = url)

    override suspend fun cardNumberDecryptionCall(
        url: String,
        body: RequestBody
    ): Response<CardDetailViewResponseModel> =
        apiService.cardNumberDecryptionCall(url = url, body = body)

    override suspend fun getDetailVerificationCode(url: String): Response<ResponseBody> =
        apiService.getDetailVerificationCode(url = url)

    override suspend fun cardAllDecryptionCall(
        url: String,
        body: RequestBody
    ): Response<CardDetailViewResponseModel> =
        apiService.cardAllDecryptionCall(url = url, body = body)

    override suspend fun changePin(
        url: String,
        body: RequestBody
    ): Response<ResponseBody> =
        apiService.changePin(url = url, body = body)

    override suspend fun getCardPayloadData(url: String): Response<PayloadCardDetailResponseModel> =
        apiService.getCardPayloadData(url = url)

    override suspend fun cardPayloadTopUpCall(
        url: String,
        body: PayloadTopUpRequestModel
    ): Response<CreatePayloadOfferResponseModel> =
        apiService.cardPayloadTopUpCall(url = url, body = body)

    override suspend fun cardPayloadTopUpConfirmCall(
        url: String
    ): Response<ResponseBody> =
        apiService.cardPayloadTopUpConfirmCall(url = url)

    override suspend fun cardSoftBlockCall(url: String, body: RequestBody): Response<ResponseBody> =
        apiService.cardSoftBlockCall(url = url, body = body)


    override suspend fun getPayInCardDetail(): Response<PayInCardRatesResponseModel> =
        apiService.getPayInCardDetail()

    override suspend fun payInAddCardCall(body: AddCardRequestModel): Response<ResponseBody> =
        apiService.payInAddCardCall(body = body)

    override suspend fun payInCreateOfferCall(body: RequestBody): Response<PayInCreateOfferResponse> =
        apiService.payInCreateOfferCall(body = body)

    override suspend fun payInOfferExecuteCall(
        url: String,
        body: RequestBody
    ): Response<PayInOfferResponseModel> =
        apiService.payInOfferExecuteCall(url = url, body = body)


    override suspend fun getSendFeeCurrency(
        url: String,
        amount: String,
        address: String?,
        phone: String?,
    ): Response<SendFeeCurrencyResponseModel> =

        apiService.getSendFeeCurrency(
            url = url,
            amount = amount,
            address = address,
            phone = phone
        )


    override suspend fun sendWalletValidation(body: RequestSendModel): Response<SendWalletValidationModel> =
        apiService.sendWalletValidation(body = body)

    override suspend fun sendWalletCrypto(body: RequestSendModel): Response<SendWalletModel> =
        apiService.sendWalletCrypto(body = body)

    override suspend fun exchangeCurrencyPairCall(): Response<ExchangeCurrencyPairResponseModel> =
        apiService.exchangeCurrencyPairCall()

    override suspend fun exchangeCurrencyCall(body: RequestBody): Response<ExchangeCreateOfferResponseModel> =
        apiService.exchangeCurrencyCall(body = body)

    override suspend fun exchangeCurrencyExecuteCall(url: String): Response<ResponseBody> =
        apiService.exchangeCurrencyExecuteCall(url = url)

    override suspend fun payInPayCallback(url: String): Response<PayInCallbackResponse> =
        apiService.payInPayCallback(url = url)


    override suspend fun getPayOutCardDetail(): Response<PayOutCardRatesResponseModel> =
        apiService.getPayOutCardDetail()

    override suspend fun payOutAddCardCall(body: AddCardRequestModel): Response<ResponseBody> =
        apiService.payOutAddCardCall(body = body)

    override suspend fun payOutCreateOffer(body: RequestBody): Response<PayOutCreateOfferResponseModel> =
        apiService.payOutCreateOffer(body = body)

    override suspend fun payOutExecuteOffer(url: String): Response<PayoutExecuteOfferResponseModel> =
        apiService.payOutExecuteOffer(url = url)

    /**
     *@Pravin Card Beta Apis
     **/

    override suspend fun cardBetaSignUp(
        model: SignUpRequestModel
    ): Response<SignUpResponseModel> =
        apiService.cardBetaSignUp(body = model)

    override suspend fun resetPasswordRequest(
        model: ForgotPassRequestModel
    ): Response<ResponseBody> =
        apiService.resetPasswordRequest(body = model)

    override suspend fun cardBetaLogin(
        model: SignInRequestModel
    ): Response<SignUpResponseModel> =
        apiService.cardBetaLogin(body = model)

    override suspend fun createPhone(
        model: PhoneNumberRequestModel
    ): Response<ResponseBody> =
        apiService.createPhone(model = model)

    override suspend fun requestConfirmPhone(
        model: PhoneNumberRequestModel
    ): Response<ResponseBody> =
        apiService.requestConfirmPhone(model = model)

    override suspend fun getCurrencyList(): Response<MutableList<CryptoModel>> =
        apiService.getCurrencyList()

    override suspend fun getAccountList(): Response<MutableList<AccountModel>> =
        apiService.getAccountList()

    override suspend fun createAccount(url: String): Response<AccountModel> =
        apiService.createAccount(url)

    override suspend fun getKycVerification(body: RequestBody): Response<KYCVerificationModel> =
        apiService.getKycVerification(body = body)


    override suspend fun subscriptionPlanList(): Response<MutableList<SubPlanModel>> =
        apiService.subscriptionPlanList()

    override suspend fun getTransactions(
        accountIds: List<String>,
        page: Int,
        size: Int
    ): Response<TransactionCardHistoryModel> =
        apiService.getTransactions(
            accountIds = arrayListOf(),
            page = page,
            size = size
        )

    override suspend fun getTransactionsHistoryDetail(url: String): Response<TransactionDetailModel> =
        apiService.getTransactionsHistoryDetail(
            url = url,

            )

    override suspend fun getAllSellProvider(): Response<SellProviderModel> =
        apiService.getAllSellProvider()


    override suspend fun executeMoralisTransactionList(url: String): Response<TransactionMoralisResponse> =
        apiService.executeMoralisTransactionList(url)

    override suspend fun executeTransactionHistoryList(url: String): Response<TransferHistoryModel> =
        apiService.executeTransactionHistoryList(url)


}