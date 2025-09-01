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
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response

interface ApiHelper {
    suspend fun getCoinsAssets(symbol: String): Response<Info>
    // suspend fun getAssets(symbol: String): Response<Info>

    suspend fun getGenerateToken(
        url: String
    ): Response<GenerateTokenModel>

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


    suspend fun exodusSwapUpdateOrderCall(body: RequestBody): Response<ExodusSwapResponseModel>
    suspend fun exodusTransactionStatusCall(url: String): Response<ExodusSwapResponseModel>

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
    suspend fun executeNftListApi(url: String): Response<MutableList<NFTListModel>>
    suspend fun estimationMinChangeNow(url: String): Response<EstimateMinOnChangeValue>

    suspend fun executeCoinGeckoMarketsApi(url: String): Response<MutableList<CoinGeckoMarketsResponse>>

    suspend fun executeGetCurrencyApi(url: String): Response<CoinMarketCurrencyResponse>

    suspend fun executeUpdateCardCurrencyApi(body: RequestBody): Response<ResponseBody>

    suspend fun executeOkLinkTransactionList(url: String): Response<TransactionHistoryResponse>
    suspend fun getAssetHistory(currencyFilter: List<String>): Response<AssetHistoryWrapperModel>
    suspend fun getCardDashboardImages(): Response<CardDashboardImagesModel>
    suspend fun executeHistoryCardTransactions(
        offset: String,
        size: String,
    ): Response<HistoryCardWrapperModel>

    suspend fun executeHistoryByCardId(
        url: String,
        offset: String,
        size: String,
        cardId: String
    ): Response<CardHistoryByCardID>

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


    suspend fun executeTokenImageListApi(): Response<MutableList<TokenListImageModel>>
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
    suspend fun getMyReferrals(walletAddress: String): Response<MyReferralWrapperModel>
    suspend fun updateNonClaimedTokens(walletAddress: String): Response<ResponseBody>
    suspend fun getMyUserCode(walletAddress: String): Response<ReferralCodesWrapperModel>
    suspend fun registerWallet(
        walletAddress: String,
        deviceId: String,
        fcmToken: String,
        type: String,
        referralCode: String
    ): Response<ResponseBody>

    /*    suspend fun registerWalletMaster(
            deviceId: String,
            walletAddress: String,
            referralCode: String,
        ): Response<ResponseBody>*/


    suspend fun setWalletActive(
        walletAddress: String,
        receiverAddress: String
    ): Response<ResponseBody>

    suspend fun sendBTCTransaction(
        privateKey: String,
        value: String,
        toAddress: String,
        env: String,
        fromAddress: String
    ): Response<ResponseBody>

    suspend fun domainCheck(domainSearchModel: DomainSearchModel): Response<ENSListModel>

    suspend fun getAllActiveTokenList(url: String): Response<MutableList<ModelActiveWalletToken>>

    suspend fun transactionTrackActivityLog(body: TransferTraceDetail): Response<ResponseBody>
    suspend fun swapQuoteSingleCall(body: SwapQuoteRequestModel): Response<SwapQuoteResponseModel>
    suspend fun buyQuoteSingleCall(body: BuyRequestModel): Response<BuyResponseModel>

    /**
     * @author Pravin Patel
     * Card module api start from here
     * **/
    suspend fun cardUserSignUp(
        url: String,
        cardUserModelSignUP: CardUserModelSignUP
    ): Response<ResponseBody>

    suspend fun cardUserLogout(): Response<ResponseBody>

    suspend fun cardUserForgetPassword(
        url: String,
        cardUserModelSignUP: CardUserModelSignUP
    ): Response<ResponseBody>

    suspend fun cardUserLogin(
        url: String,
        cardUserModelSignUP: CardUserModelSignUP
    ): Response<VerificationResponseModel>


    suspend fun cardUserVarificationCodeConfirm(
        url: String,
        body: SendSmsCodeModel
    ): Response<VerificationResponseModel>

    suspend fun cardUserForgetPasswordVerificationCodeConfirm(
        url: String,
        body: ForgetPasswordSmsCodeModel
    ): Response<VerificationResponseModel>


    suspend fun cardUserAddEmail(
        auth: String,
        url: String,
        body: RequestBody
    ): Response<ResponseBody>

    /*suspend fun cardUserChangeForgetPassword(
        auth: String,
        url: String,
        body: RequestBody,
        code: RequestBody,
        password: RequestBody
    ): Response<ResponseBody>*/

    suspend fun cardUserChangeForgetPassword(
        auth: String,
        url: String,
        body: ForgetPasswordSmsCodeModel
    ): Response<ResponseBody>

    suspend fun getCardUserProfile(
    ): Response<CardUserProfileResponseModel>

    suspend fun updateCardUserProfile(
        body: UpdateProfileRequestModel,
    ): Response<ResponseBody>

    suspend fun resendEmailPhoneVerification(): Response<ResponseBody>

    suspend fun resendPhoneVerification(body: ResendPhoneOtpRequestModel?): Response<ResponseBody>


    suspend fun startKyc1(): Response<ResponseBody>

    suspend fun finishKyc1(body: RequestBody): Response<ResponseBody>
    suspend fun getKycStatus(): Response<KycStatusResponseModel>
    suspend fun getCardWalletList(): Response<CardWalletListResponseModel>

    suspend fun createCardWallet(tokenList: RequestBody): Response<CardWalletListResponseModel>
    suspend fun getCardList(): Response<CardListResponseModel>

    suspend fun addNewCardRequest(body: CardAddRequestModel): Response<AddNewCardResponseModel>
    suspend fun cancelCardRequest(url: String): Response<ResponseBody>

    suspend fun updateCardAddress(
        url: String,
        body: UpdateCardAddressRequestModel
    ): Response<ResponseBody>

    suspend fun changePassword(
        body: RequestBody
    ): Response<ResponseBody>

    suspend fun getKyc(
        url: String
    ): Response<KycGetResponseModel>

    suspend fun getCardPrice(): Response<CardPriceResponseModel>

    suspend fun getKycLimit(): Response<KycLimitResponseModel>

    suspend fun getCountryStateList(): Response<CountryStateModel>

    suspend fun getCityList(body: RequestBody): Response<CityResponseModel>

    suspend fun cardAdditionalPersonalInfoCall(body: RequestBody): Response<ResponseBody>

    suspend fun getWalletCurrencyPrice(url: String): Response<CardWalletCurrencyResponseModel>
    suspend fun cardRequestPaymentOffer(url: String): Response<CreateOfferPaymentResponseModel>
    suspend fun cardRequestPaymentOfferExecute(url: String): Response<ResponseBody>
    suspend fun cardNumberDecryptionCall(
        url: String,
        body: RequestBody
    ): Response<CardDetailViewResponseModel>

    suspend fun getDetailVerificationCode(url: String): Response<ResponseBody>
    suspend fun cardAllDecryptionCall(
        url: String,
        body: RequestBody
    ): Response<CardDetailViewResponseModel>

    suspend fun changePin(
        url: String,
        body: RequestBody
    ): Response<ResponseBody>

    suspend fun getCardPayloadData(url: String): Response<PayloadCardDetailResponseModel>
    suspend fun cardPayloadTopUpCall(
        url: String,
        body: PayloadTopUpRequestModel
    ): Response<CreatePayloadOfferResponseModel>

    suspend fun cardPayloadTopUpConfirmCall(
        url: String
    ): Response<ResponseBody>

    suspend fun cardSoftBlockCall(url: String, body: RequestBody): Response<ResponseBody>

    suspend fun getPayInCardDetail(): Response<PayInCardRatesResponseModel>
    suspend fun payInAddCardCall(body: AddCardRequestModel): Response<ResponseBody>
    suspend fun payInCreateOfferCall(body: RequestBody): Response<PayInCreateOfferResponse>
    suspend fun payInOfferExecuteCall(
        url: String,
        body: RequestBody
    ): Response<PayInOfferResponseModel>

    suspend fun getSendFeeCurrency(
        url: String,
        amount: String,
        address: String?,
        phone: String?,
    ): Response<SendFeeCurrencyResponseModel>

    suspend fun sendWalletValidation(body: RequestSendModel): Response<SendWalletValidationModel>
    suspend fun sendWalletCrypto(body: RequestSendModel): Response<SendWalletModel>

    suspend fun exchangeCurrencyPairCall(): Response<ExchangeCurrencyPairResponseModel>
    suspend fun exchangeCurrencyCall(body: RequestBody): Response<ExchangeCreateOfferResponseModel>
    suspend fun exchangeCurrencyExecuteCall(url: String): Response<ResponseBody>

    suspend fun payInPayCallback(url: String): Response<PayInCallbackResponse>

    suspend fun getPayOutCardDetail(): Response<PayOutCardRatesResponseModel>
    suspend fun payOutAddCardCall(body: AddCardRequestModel): Response<ResponseBody>

    suspend fun payOutCreateOffer(body: RequestBody): Response<PayOutCreateOfferResponseModel>
    suspend fun payOutExecuteOffer(url: String): Response<PayoutExecuteOfferResponseModel>

    //Card Beta Api

    suspend fun cardBetaSignUp(model: SignUpRequestModel): Response<SignUpResponseModel>

    suspend fun resetPasswordRequest(
        model: ForgotPassRequestModel
    ): Response<ResponseBody>

    suspend fun cardBetaLogin(
        model: SignInRequestModel
    ): Response<SignUpResponseModel>

    suspend fun createPhone(model: PhoneNumberRequestModel): Response<ResponseBody>

    suspend fun requestConfirmPhone(
        model: PhoneNumberRequestModel
    ): Response<ResponseBody>

    suspend fun getCurrencyList(): Response<MutableList<CryptoModel>>

    suspend fun getAccountList(): Response<MutableList<AccountModel>>
    suspend fun createAccount(url: String): Response<AccountModel>
    suspend fun getKycVerification(body: RequestBody): Response<KYCVerificationModel>

    suspend fun subscriptionPlanList(): Response<MutableList<SubPlanModel>>
    suspend fun getTransactions(
        accountIds: List<String>,
        page: Int,
        size: Int
    ): Response<TransactionCardHistoryModel>

    suspend fun getTransactionsHistoryDetail(url: String): Response<TransactionDetailModel>

    suspend fun getAllSellProvider(): Response<SellProviderModel>

    suspend fun executeMoralisTransactionList(url: String): Response<TransactionMoralisResponse>

    suspend fun executeTransactionHistoryList(url: String): Response<TransferHistoryModel>

}