package com.app.plutope.ui.fragment.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentDashboardBinding
import com.app.plutope.dialogs.DashboardSearchBottomSheet
import com.app.plutope.dialogs.DeviceLockFullScreenDialog
import com.app.plutope.model.CurrencyList
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Tokens
import com.app.plutope.model.Wallet
import com.app.plutope.networkConfig.Chains
import com.app.plutope.ui.adapter.ViewPagerAdapter
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.currency.CurrencyViewModel
import com.app.plutope.ui.fragment.dashboard.assets.Assets
import com.app.plutope.ui.fragment.dashboard.nfts.NFTs
import com.app.plutope.ui.fragment.token.TokenViewModel
import com.app.plutope.ui.fragment.transactions.swap.DashboardButtonsModel
import com.app.plutope.utils.BiggerDotPasswordTransformationMethod
import com.app.plutope.utils.ConnectivityReceiver
import com.app.plutope.utils.coinTypeEnum.CoinType
import com.app.plutope.utils.constant.isForceDownLockScreen
import com.app.plutope.utils.constant.isFromReceived
import com.app.plutope.utils.constant.isFromTransactionDetail
import com.app.plutope.utils.constant.isPausedOnce
import com.app.plutope.utils.constant.pageTypeBuy
import com.app.plutope.utils.constant.pageTypeSwap
import com.app.plutope.utils.extractQRCodeScannerInfo
import com.app.plutope.utils.extras.BiometricResult
import com.app.plutope.utils.extras.FirebaseAnalyticsHelper.logEvent
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.extras.setBioMetric
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loadJSONFromRaw
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.app.plutope.utils.showToast
import com.app.plutope.utils.walletConnection.Web3WalletViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode


@AndroidEntryPoint
class Dashboard : BaseFragment<FragmentDashboardBinding, DashboardViewModel>(),
    ConnectivityReceiver.ConnectivityReceiverListener, UpdateBalanceListener {
    private val keyAddress = "key_address"
    private var addressWallet: String? = ""
    private val tokenModel: TokenViewModel by viewModels()
    private val web3walletViewModel: Web3WalletViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val currencyViewModel: CurrencyViewModel by activityViewModels()


    var isFromWalletConnection: Boolean = false
    var walletPairingUrl: String = ""

    override fun getViewModel(): DashboardViewModel {
        return dashboardViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.dashboardViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_dashboard
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // preferenceHelper.appUpdatedFlag = "4"

        hideLoader()
        (activity as BaseActivity).askNotificationPermission()
        addressWallet = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
        viewDataBinding!!.txtWalletAddress.text = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)
        viewDataBinding!!.txtWalletAddress.setOnClickListener {
            onClickCopy()
        }



        viewDataBinding!!.imgPlutoPe.setOnClickListener {
            findNavController().navigate(DashboardDirections.actionGlobalToSettings())
        }

        // animateImageProgress()

        loadTabData()


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (requireActivity() as BaseActivity).openExitDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        // loge("Publickey", "PK : ${Wallet.getPublicWalletAddress(CoinType.ETHEREUM)}")
    }


    private fun onClickCopy() {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(
            keyAddress, addressWallet//qrCodeUrl
        )
        clipboard?.setPrimaryClip(clip)
        requireContext().showToast("Copied: $addressWallet")

    }


    private fun loadTabData() {

        viewDataBinding?.tabLayout?.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                viewDataBinding?.viewpager?.currentItem = tab?.position!!
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                loge("SelectedTab", "${tab?.position}")
                viewDataBinding!!.viewpager.currentItem = tab!!.position
                if (tab.position == 0) {
                    viewDataBinding?.imgAddCustomToken?.visibility = View.VISIBLE
                } else {
                    viewDataBinding?.imgAddCustomToken?.visibility = View.GONE
                }
            }

        })
        val titleTab = arrayListOf(getString(R.string.assets), getString(R.string.nfts))
        /*
                val asset = Assets(callback = { str ->
                    val balance =
                        preferenceHelper.getSelectedCurrency()?.symbol.toString() + "" + (str.toBigDecimal()).setScale(
                            2,
                            RoundingMode.DOWN
                        ).toString()
                    Wallet.walletObject.w_wallet_last_balance = balance
                    viewDataBinding?.txtCurrentBalanceValue?.text = balance


                })
        */

        val asset = Assets.newInstance { str ->
            val balance = preferenceHelper.getSelectedCurrency()?.symbol.toString() +
                    "" + (str.toBigDecimal()).setScale(2, RoundingMode.DOWN).toString()
            Wallet.walletObject.w_wallet_last_balance = balance
            viewDataBinding?.txtCurrentBalanceValue?.text = balance
        }


        viewDataBinding?.txtCurrentBalanceValue?.text = Wallet.walletObject.w_wallet_last_balance
        CoroutineScope(Dispatchers.Main).launch {
            setWalletName(Wallet.walletObject.w_wallet_name)
        }


        val list = arrayListOf<Fragment>()
        list.add(asset)
        list.add(NFTs())
        viewDataBinding?.viewpager?.adapter = ViewPagerAdapter(list, requireActivity())

        viewDataBinding?.viewpager?.isUserInputEnabled = false

        TabLayoutMediator(
            viewDataBinding?.tabLayout!!,
            viewDataBinding?.viewpager!!
        ) { tab, position ->
            tab.text = titleTab[position]
            viewDataBinding?.viewpager?.setCurrentItem(tab.position, true)

            /*viewDataBinding!!.divider.visibility =
                if (tab.text == getString(R.string.nfts)) View.GONE else View.VISIBLE*/
        }.attach()

    }

    override fun setupUI() {
        this.logEvent()
        setUpListener()
//        viewDataBinding?.txtCurrencyTitle?.text = "("+preferenceHelper.getSelectedCurrency()?.code.toString()+")"

        setWalletName(Wallet.walletObject.w_wallet_name)


        val list: ArrayList<DashboardButtonsModel> = arrayListOf()
        list.add(DashboardButtonsModel(R.drawable.ic_receive, getString(R.string.receive)))
        list.add(DashboardButtonsModel(R.drawable.ic_send_bg_ract, getString(R.string.send)))
        list.add(DashboardButtonsModel(R.drawable.ic_add_bg_ract, getString(R.string.buy)))
        list.add(DashboardButtonsModel(R.drawable.ic_swap_with_bg, getString(R.string.swap)))
        list.add(DashboardButtonsModel(R.drawable.ic_sell, getString(R.string.sell)))
        val adapter = DashboardButtonsAdapter(false, list) {
            when (it.name) {
                getString(R.string.receive) -> {
                    findNavController().safeNavigate(DashboardDirections.actionDashboardToReceive())
                }

                getString(R.string.send) -> {
                    findNavController().safeNavigate(DashboardDirections.actionDashboardToSend())
                }

                getString(R.string.buy) -> {
                    val result = Bundle()
                    result.putString("page_type", pageTypeBuy)
                    findNavController().safeNavigate(R.id.action_global_to_buy, result)
                }

                getString(R.string.swap) -> {
                    val result = Bundle()
                    result.putString("page_type", pageTypeSwap)
                    findNavController().safeNavigate(R.id.action_global_to_buy, result)
                }

                getString(R.string.sell) -> {
                    /* val url =
                        "https://webview.rampable.co/?clientSecret=KfoET5E31jh7iikwBwGfHNqB78mbmUmGEpzgVSOj2ovD4AKzuPmdRnX0Up4miXyx&useWalletConnect=true"

                   findNavController().safeNavigate(
                        DashboardDirections.actionDashboardToBrowser(
                            url
                        )
                    )*/

                    findNavController().safeNavigate(
                        DashboardDirections.actionDashboardToSell()
                    )

                }
            }
        }
        viewDataBinding!!.rvButtons.adapter = adapter


        if (!preferenceHelper.isWalletBalanceHidden) {
            viewDataBinding!!.txtCurrentBalanceValue.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            viewDataBinding!!.imgHideShow.setImageResource(R.drawable.ic_show_eye)
        } else {
            viewDataBinding!!.txtCurrentBalanceValue.transformationMethod =
                BiggerDotPasswordTransformationMethod
            viewDataBinding!!.imgHideShow.setImageResource(R.drawable.ic_eye_hide)
        }
        viewDataBinding!!.txtCurrentBalanceValue.manageBalanceHideShow(viewDataBinding!!.imgHideShow)
    }

    private fun setWalletName(walletName: String?) {
        viewDataBinding?.txtWalletTitle?.text = walletName
        viewDataBinding?.txtWalletName?.text = getString(R.string.total_balance)
        viewDataBinding!!.txtWalletAddress.text = Wallet.getPublicWalletAddress(CoinType.ETHEREUM)

    }

    private fun setUpListener() {

        //preferenceHelper.updateTokenText = ""
        dashboardViewModel.executeGetGenerateToken()

        viewDataBinding?.imgScan?.setOnClickListener {
            scanQrCode.launch(null)
        }

        viewDataBinding!!.imgSearch.setOnClickListener {

            // dashboardViewModel.onClick()

            DashboardSearchBottomSheet.newInstance(
                dialogDismissListner = { token ->
                    isFromTransactionDetail = false
                    findNavController().safeNavigate(
                        DashboardDirections.actionDashboardToBuyDetails(
                            token
                        )
                    )
                }).show(childFragmentManager, "")
        }

        viewDataBinding?.imgAddCustomToken?.setOnClickListener {
            findNavController().safeNavigate(DashboardDirections.actionDashboardToAddCustomToken())
        }

        getTableCurrencyList()

    }

    private fun getTableCurrencyList() {
        val listCurrency = currencyViewModel.getTableCurrencyList()
        if (listCurrency.isEmpty()) {
            val currencyList = loadJsonCurrency(R.raw.currency_list)
            currencyViewModel.insertCurrency(currencyList as MutableList<CurrencyModel>)
            //currencyViewModel.executeGetCurrency(COIN_MARKET_CURRENCY_URL)
        }
    }

    override fun setupObserver() {

        /* lifecycleScope.launch {
             repeatOnLifecycle(Lifecycle.State.RESUMED) {
                 if (isPausedOnce) {
                     isPausedOnce = false
                     openPasswordDialogScreen("")
                 }
             }
         }*/

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                dashboardViewModel.getGenerateTokenResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            loge(
                                "getGenerateTokenResponse",
                                "${it.data?.data?.isUpdate} :: ${it.data?.data?.tokenString}"
                            )
                            if (it.data?.data?.isUpdate == true && it.data.data.platform?.lowercase() != "ios") {
                                if (it.data.data.tokenString?.lowercase() != preferenceHelper.updateTokenText) {
                                    loge("getGenerateTokenResponse", "Update token")
                                    findNavController().safeNavigate(DashboardDirections.actionDashboardToUpdateAnything())
                                }
                            }
                        }

                        is NetworkState.Loading -> {}
                        is NetworkState.Error -> {}
                        is NetworkState.SessionOut -> {}
                        else -> {

                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                currencyViewModel.currencyResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {}
                        is NetworkState.Loading -> {}
                        is NetworkState.Error -> {}
                        is NetworkState.SessionOut -> {}
                        else -> {}
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        loge("onResume", "in resume $isPausedOnce")
        // Handler(Looper.getMainLooper()).post {
        if (isPausedOnce) {
            isPausedOnce = false
            openPasswordDialogScreen("")
        }
        // }


        if (isFromReceived) {
            loadTabData()
        }

        Assets.newInstance { str ->
            val balance = preferenceHelper.getSelectedCurrency()?.symbol.toString() +
                    "" + (str.toBigDecimal()).setScale(2, RoundingMode.DOWN).toString()
            Wallet.walletObject.w_wallet_last_balance = balance
            viewDataBinding?.txtCurrentBalanceValue?.text = balance
        }


        // loadTabData()

        dashboardViewModel.getBalance.observe(this) { str ->
            requireActivity().runOnUiThread {
                val balance = preferenceHelper.getSelectedCurrency()?.symbol.toString() +
                        "" + (str.toBigDecimal()).setScale(2, RoundingMode.DOWN).toString()
                Wallet.walletObject.w_wallet_last_balance = balance
                viewDataBinding?.txtCurrentBalanceValue?.text = balance
            }
        }

    }

    override fun onStart() {
        super.onStart()
        loge("onStart", "in onStart")

    }

    private fun loadJsonCurrency(filename: Int): List<CurrencyModel> {
        val jsonString = loadJSONFromRaw(filename)// load the JSON from file or network
        val currencyList = Gson().fromJson(jsonString, CurrencyList::class.java)
        return currencyList.data
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        loge("Dashboard", "isConnected : $isConnected")
        if (isConnected) {
            loadTabData()
        }

    }

    override fun updateBalance(balance: String) {
        // loge("Dashboard", "updateBalance : $balance")
    }


    private val scanQrCode = registerForActivityResult(ScanQRCode()) { result: QRResult ->
        loge("isForceDownLockScreen", "isForceDownLockScreenDashBoard :$isForceDownLockScreen")

        when (result) {
            is QRResult.QRSuccess -> {
                val qrResult = result.content.rawValue
                val partResult = extractQRCodeScannerInfo(qrResult)
                if (partResult?.third == "wc") {

                    // web3walletViewModel.pair(qrResult)
                    isFromWalletConnection = true
                    walletPairingUrl = qrResult
                    openPasswordDialogScreen(qrResult)

                } else if (partResult?.third == "null" || partResult?.third == null || partResult.third == "") {
                    findNavController().safeNavigate(DashboardDirections.actionGlobalToSend(qrResult))
                } else {
                    var selectedToken = Tokens()
                    Chains.values().forEach { chain ->
                        if (chain.chainForTrustWallet == partResult.third) {
                            tokenModel.getAllTokensList().filter { it.t_address == "" }
                                .forEach { token ->
                                    if (token.t_symbol.lowercase() == chain.symbol.lowercase()) {
                                        selectedToken = token
                                    }
                                }


                            /*  token.chain?.coinType = it.coinType
                              token.t_address = ""
                              token.t_name = it.name
                              token.t_symbol = it.symbol
                              token.t_type = it.type*/
                        }
                    }

                    findNavController().safeNavigate(
                        DashboardDirections.actionDashboardToSendCoin(
                            selectedToken,
                            qrResult
                        )
                    )
                }


            }

            QRResult.QRUserCanceled -> {

                Toast.makeText(
                    requireContext(),
                    "Cancelled",
                    Toast.LENGTH_LONG
                ).show()
            }

            QRResult.QRMissingPermission -> {

                Toast.makeText(
                    requireContext(),
                    "Missing permission",
                    Toast.LENGTH_LONG
                ).show()
            }

            is QRResult.QRError -> {
                "${result.exception.javaClass.simpleName}: ${result.exception.localizedMessage}"
            }
        }


        // isForceDownLockScreen = false

    }

    private fun openPasswordDialogScreen(qrResult: String) {
        try {
            activity?.let {
                loge("ActivityLifeCycle", "${it.isFinishing} :: ${it.isDestroyed} : $isAdded")
                if (!it.isFinishing && !it.isDestroyed && isAdded) {
                    if (preferenceHelper.isAppLock) {
                        if (!preferenceHelper.isLockModePassword)
                            setBioMetric(biometricListener)
                        else {

                            /* DeviceLockDialogFragment2.show(
                                 childFragmentManager,
                                 object : DeviceLockDialogFragment2.DialogOnClickBtnListener {
                                     override fun onSubmitClicked(selectedList: String) {
                                         // Handle success
                                         openPasscodeScreen(qrResult)
                                     }
                                 })*/


                            DeviceLockFullScreenDialog.getInstance().show(requireContext(),
                                object : DeviceLockFullScreenDialog.DialogOnClickBtnListner {
                                    override fun onSubmitClicked(selectedList: String) {
                                        loge("LockResult", "here is am_1")
                                        openPasscodeScreen(qrResult)
                                    }
                                })
                        }

                    } else {
                        openPasscodeScreen(qrResult)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }


    private fun TextView.manageBalanceHideShow(imageView: ImageView) {
        imageView.setOnClickListener {
            if (this.transformationMethod == BiggerDotPasswordTransformationMethod) {
                this.transformationMethod = HideReturnsTransformationMethod.getInstance()
                imageView.setImageResource(R.drawable.ic_show_eye)
                dashboardViewModel.updateIsBalanceHidden(false)
                preferenceHelper.isWalletBalanceHidden = false
            } else {
                this.transformationMethod = BiggerDotPasswordTransformationMethod
                imageView.setImageResource(R.drawable.ic_eye_hide)
                dashboardViewModel.updateIsBalanceHidden(true)
                preferenceHelper.isWalletBalanceHidden = true
            }
        }
    }

    private var biometricListener = object : BiometricResult {
        override fun success() {
            loge("LockResult", "here is am_2")
            PreferenceHelper.getInstance().isBiometricAllow = true
            openPasscodeScreen(walletPairingUrl)
        }

        override fun failure(errorCode: Int, errorMessage: String) {
            loge("LockResult", "here is am_3")
            when (errorCode) {

                BiometricPrompt.ERROR_LOCKOUT -> (requireActivity() as BaseActivity).continueWithoutBiometric(
                    "Maximum number of attempts exceeds! Try again later",
                    useDevicePassword = true
                )

                BiometricPrompt.ERROR_USER_CANCELED, BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_CANCELED -> (requireActivity() as BaseActivity).continueWithoutBiometric(
                    "Unlock with Face ID/ Touch ID or password",
                    true
                )

                else -> (requireActivity() as BaseActivity).continueWithoutBiometric(errorMessage)
            }
        }

        override fun successCustomPasscode() {
            loge("LockResult", "here is am_4")
            openPasscodeScreen(walletPairingUrl)
        }

    }

    fun openPasscodeScreen(qrResult: String) {
        if (qrResult != "") {
            isFromWalletConnection = false
            web3walletViewModel.pair(qrResult)
            walletPairingUrl = ""
        }
    }


    /*private fun calculateTotalFragmentHeight(view: View): Int {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED)
        )
        return view.measuredHeight
    }



    fun updateViewPagerHeight(position: Int,isAfterDataLoaded: Boolean = false) {
        val fragment = (viewDataBinding!!.viewpager.adapter as FragmentStateAdapter).createFragment(position)

        fragment.viewLifecycleOwnerLiveData.observe(viewLifecycleOwner) { owner ->
            owner?.lifecycleScope?.launchWhenStarted {
                fragment.view?.let { view ->
                    view.post {
                        if (isAfterDataLoaded){
                            val recyclerView = view.findViewById<RecyclerView>(R.id.rv_assets_list)
                            recyclerView?.let {
                                val height = calculateRecyclerViewHeight(it)
                                if (height > 0) {
                                    val layoutParams = viewDataBinding!!.viewpager.layoutParams
                                    layoutParams.height = height
                                    viewDataBinding!!.viewpager.layoutParams = layoutParams
                                }
                            }
                        }
                        else{
                                val height = calculateTotalFragmentHeight(view)
                                if (height > 0) {
                                    val layoutParams = viewDataBinding!!.viewpager.layoutParams
                                    layoutParams.height = height
                                    viewDataBinding!!.viewpager.layoutParams = layoutParams
                                }
                        }
                    }
                }
            }
        }
    }

    private fun calculateRecyclerViewHeight(recyclerView: RecyclerView): Int {
        val adapter = recyclerView.adapter ?: return 0
        var totalHeight = 0

        for (i in 0 until adapter.itemCount) {
            val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            holder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += holder.itemView.measuredHeight
        }
        return totalHeight + recyclerView.paddingTop + recyclerView.paddingBottom
    }*/


}

