package com.app.plutope.ui.fragment.dashboard

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentDashboardBinding
import com.app.plutope.model.CurrencyList
import com.app.plutope.model.CurrencyModel
import com.app.plutope.model.Wallet
import com.app.plutope.ui.adapter.ViewPagerAdapter
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.ui.fragment.currency.CurrencyViewModel
import com.app.plutope.ui.fragment.dashboard.assets.Assets
import com.app.plutope.ui.fragment.dashboard.nfts.NFTs
import com.app.plutope.utils.ConnectivityReceiver
import com.app.plutope.utils.constant.isFromReceived
import com.app.plutope.utils.hideLoader
import com.app.plutope.utils.loadJSONFromRaw
import com.app.plutope.utils.loge
import com.app.plutope.utils.network.NetworkState
import com.app.plutope.utils.safeNavigate
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.RoundingMode


@AndroidEntryPoint
class Dashboard : BaseFragment<FragmentDashboardBinding, DashboardViewModel>(),
    ConnectivityReceiver.ConnectivityReceiverListener, UpdateBalanceListener {

    private val dashboardViewModel: DashboardViewModel by activityViewModels()
    private val currencyViewModel: CurrencyViewModel by activityViewModels()


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
        hideLoader()
        (activity as BaseActivity).askNotificationPermission()


        animateImageProgress()
        loadTabData()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (requireActivity() as BaseActivity).openExitDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        /*
                viewLifecycleOwner.lifecycleScope.launch {
                    dashboardViewModel.getBalance.observe(viewLifecycleOwner){
                        loge("Dashboard", "updateBalance1 : $it")
                    }

                }
        */

        // loge("PrivateKey","PK : ${Wallet.getPrivateKeyData(CoinType.ETHEREUM)}")

    }

    private fun animateImageProgress() {
        val durationMillis = 3000
        val repeatCount = Animation.INFINITE
        val fromDegrees = 0f
        val toDegrees = 360f
        val rotateAnimation = RotateAnimation(
            fromDegrees, toDegrees,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotateAnimation.duration = durationMillis.toLong()
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.repeatCount = repeatCount
        viewDataBinding?.imgInnerCircle?.startAnimation(rotateAnimation)
    }

    private fun loadTabData() {

        viewDataBinding?.tabLayout?.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                viewDataBinding?.viewpager?.currentItem = tab?.position!!
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewDataBinding!!.viewpager.currentItem = tab!!.position
            }

        })
        val titleTab = arrayListOf(getString(R.string.assets),getString(R.string.nfts))
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


        viewDataBinding?.txtCurrentBalanceValue?.text =
            Wallet.walletObject.w_wallet_last_balance

        val list = arrayListOf<Fragment>()
        list.add(asset)
        list.add(NFTs())
        viewDataBinding?.viewpager?.adapter = ViewPagerAdapter(list, requireActivity())

        viewDataBinding?.viewpager?.isUserInputEnabled = false;
        //TabLayout
        TabLayoutMediator(
            viewDataBinding?.tabLayout!!,
            viewDataBinding?.viewpager!!
        ) { tab, position ->
            tab.text = titleTab[position]
            viewDataBinding?.viewpager?.setCurrentItem(tab.position, true)

        }.attach()
    }

    override fun setupUI() {
        setUpListener()
        viewDataBinding?.txtCurrentBalanceTitle?.text = Wallet.walletObject.w_wallet_name
    }

    private fun setUpListener() {

        viewDataBinding?.imgNotification?.setOnClickListener {
            findNavController().safeNavigate(DashboardDirections.actionDashboardToNotification())
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                currencyViewModel.getCurrencyResponse.collect {
                    when (it) {
                        is NetworkState.Success -> {
                            if (it.data?.isNotEmpty() == true) {
                                currencyViewModel.insertCurrency(it.data)
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
        loge("onResume", "in resume")
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
            loge("Dashboard", "updateBalance : $str")
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


}