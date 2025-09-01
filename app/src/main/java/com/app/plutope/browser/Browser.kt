package com.app.plutope.browser

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.WebBackForwardList
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.browser.browserModel.DApp
import com.app.plutope.browser.custom.AddressBarListener
import com.app.plutope.browser.custom.URLLoadInterface
import com.app.plutope.browser.utils.addToHistory
import com.app.plutope.browser.utils.defaultDapp
import com.app.plutope.databinding.FragmentBrowserBinding
import com.app.plutope.networkConfig.Chains
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.loge
import com.app.plutope.utils.walletConnection.Web3WalletViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.Serializable

@AndroidEntryPoint
class Browser : BaseFragment<FragmentBrowserBinding, BrowserViewModel>(), URLLoadInterface
/* DAppBrowserSwipeInterface */ {

    val navArgs: BrowserArgs by navArgs()
    private val browserViewModel: BrowserViewModel by viewModels()

    private val web3walletViewModel: Web3WalletViewModel by viewModels()

    private var currentFragment: String? = null
    private var homePressed = false

    private lateinit var dApp: DApp

    private var lastLoadUrl = ""

    companion object {
        private const val DAPP_BROWSER = "DAPP_BROWSER"
        private const val MAGIC_BUNDLE_VAL: Long = 0xACED00D
        private const val BUNDLE_FILE = "awbrowse"
    }

    override fun onResume() {
        super.onResume()
        homePressed = false
        if (currentFragment == null) currentFragment = DAPP_BROWSER

        //browserViewModel.track(Analytics.Navigation.BROWSER)
        viewDataBinding!!.web3view.setWebLoadCallback(this)

    }


    override fun getViewModel(): BrowserViewModel {
        return browserViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.browserViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_browser
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        lastLoadUrl = navArgs.url
        dApp = DApp("Rampable", navArgs.url)
        addToHistory(context, dApp)

        initView()
        viewDataBinding!!.swapRefresh.setOnRefreshListener {
            viewDataBinding!!.web3view.loadUrl(lastLoadUrl)
            viewDataBinding!!.swapRefresh.isRefreshing = false
        }

        viewDataBinding!!.addressBarWidget.setup(
            browserViewModel.getDAppsMasterList(context)!!,
            object :
                AddressBarListener {
                override fun onLoad(urlText: String): Boolean {
                    // addToBackStack(DAPP_BROWSER)
                    val handled = loadUrl(urlText)
                    // detachFragments()
                    cancelSearchSession()
                    return handled
                }

                override fun onClear() {
                    cancelSearchSession()
                }

                override fun loadNext(): WebBackForwardList {
                    goToNextPage()
                    return viewDataBinding!!.web3view.copyBackForwardList()
                }

                override fun loadPrevious(): WebBackForwardList {
                    backPressed()
                    return viewDataBinding!!.web3view.copyBackForwardList()
                }

                override fun onHomePagePressed(): WebBackForwardList {
                    homePressed()
                    return viewDataBinding!!.web3view.copyBackForwardList()
                }
            })

        viewDataBinding!!.web3view.isVerticalScrollBarEnabled = true
        viewDataBinding!!.web3view.isHorizontalScrollBarEnabled = true

        viewDataBinding!!.web3view.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {

                loge("shouldOverrideUrlLoading", "==> $url")

                val prefixCheck =
                    url.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if (prefixCheck.size > 1) {
                    loge("UrlPrefix", "${prefixCheck[0]}")
                    val intent: Intent
                    when (prefixCheck[0]) {
                        C.DAPP_PREFIX_TELEPHONE -> {
                            intent = Intent(Intent.ACTION_DIAL)
                            intent.setData(Uri.parse(url))
                            startActivity(Intent.createChooser(intent, "Call " + prefixCheck[1]))
                            return true
                        }

                        C.DAPP_PREFIX_MAILTO -> {
                            intent = Intent(Intent.ACTION_SENDTO)
                            intent.setData(Uri.parse(url))
                            startActivity(Intent.createChooser(intent, "Email: " + prefixCheck[1]))
                            return true
                        }

                        C.DAPP_PREFIX_ALPHAWALLET -> if (prefixCheck[1] == C.DAPP_SUFFIX_RECEIVE) {
                            // viewModel.showMyAddress(context)
                            return true
                        }

                        C.DAPP_PREFIX_WALLETCONNECT -> {
                            loge("DAPP_PREFIX_WALLETCONNECT", url)
                            web3walletViewModel.pair(url)
                            return true
                        }

                        else -> {
                            if (prefixCheck[0] != "https") {


                                intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(url)
                                startActivity(intent)

                                /*
                                val parts = url.split("://wc?uri=")
                                if(parts.size == 2) {
                                      loge("ShouldOver", "Broswer==> ${parts[1]}")
                                      web3walletViewModel.pair(URLDecoder.decode(parts[1], "UTF-8"))
                                  }*/
                            } else {
                                lastLoadUrl = url
                            }


                        }
                    }
                }
                /*
                    if (fromWalletConnectModal(url)) {
                        val encodedURL = url.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[1]
                        try {
                            val decodedURL =
                                URLDecoder.decode(encodedURL, Charset.defaultCharset().name())
                            viewModel.handleWalletConnect(context, decodedURL, activeNetwork)
                            return true
                        } catch (e: UnsupportedEncodingException) {
                            Timber.d("Decode URL failed: $e")
                        }
                    }
    */
                setUrlText(url)
                return false
            }
        }


    }

    override fun setupObserver() {

    }

    private val defaultDappUrl: String
        get() {
            val customHome = browserViewModel.getHomePage(context)
            return customHome ?: defaultDapp(Chains.ETHEREUM.chainReference.toLong())
        }

    override fun backPressed() {
        if (currentFragment != DAPP_BROWSER) {
            // detachFragment(currentFragment)
        } else if (viewDataBinding!!.web3view.canGoBack()) {
            setUrlText(getSessionUrl(-1))
            viewDataBinding!!.web3view.goBack()
            //  detachFragments()
        } else if (!viewDataBinding!!.web3view.url.equals(defaultDappUrl, ignoreCase = true)) {
            homePressed()
            viewDataBinding!!.addressBarWidget.updateNavigationButtons(viewDataBinding!!.web3view.copyBackForwardList())
        }
    }


    private fun goToNextPage() {
        if (viewDataBinding!!.web3view.canGoForward()) {
            setUrlText(getSessionUrl(1))
            viewDataBinding!!.web3view.goForward()
        }
    }

    private fun setUrlText(newUrl: String?) {
        viewDataBinding!!.addressBarWidget.url = newUrl
        viewDataBinding!!.addressBarWidget.updateNavigationButtons(viewDataBinding!!.web3view.copyBackForwardList())
    }

    private fun cancelSearchSession() {
        // detachFragment(SEARCH)
        viewDataBinding!!.addressBarWidget.updateNavigationButtons(viewDataBinding!!.web3view.copyBackForwardList())
    }

    private fun getSessionUrl(relative: Int): String {
        val sessionHistory = viewDataBinding!!.web3view.copyBackForwardList()
        val newIndex = sessionHistory.currentIndex + relative
        if (newIndex < sessionHistory.size) {
            val newItem = sessionHistory.getItemAtIndex(newIndex)
            if (newItem != null) {
                return newItem.url
            }
        }
        return ""
    }

    private fun homePressed() {
        homePressed = true
        // detachFragments()
        currentFragment = DAPP_BROWSER
        viewDataBinding!!.addressBarWidget.clear()
        resetDappBrowser()
    }

    private fun initView() {

        viewDataBinding!!.web3view.setWebLoadCallback(this)
        viewDataBinding!!.layoutToolbar.refresh.setOnClickListener {
            reloadPage()
        }

        viewDataBinding!!.web3view.clearCache(false) //on restart with stored app, we usually need this
        viewDataBinding!!.web3view.resetView()
        viewDataBinding!!.web3view.loadUrl(navArgs.url)

    }

    private fun reloadPage() {

        // viewDataBinding!!.swipeRefresh.isEnabled = false
        viewDataBinding!!.web3view.resetView()
        viewDataBinding!!.web3view.reload()


    }

    private fun resetDappBrowser() {
        viewDataBinding!!.web3view.clearHistory()
        viewDataBinding!!.web3view.stopLoading()
        viewDataBinding!!.web3view.resetView()
        viewDataBinding!!.web3view.loadUrl(defaultDappUrl)
        setUrlText(defaultDappUrl)
    }


    private fun readBundleFromLocal(): Bundle? {
        val file = File(requireContext().filesDir, BUNDLE_FILE)
        try {
            FileInputStream(file).use { fis ->
                val oos = ObjectInputStream(fis)
                val check = oos.readObject()
                if (MAGIC_BUNDLE_VAL != check) {
                    return null
                }
                val bundle = Bundle()
                while (fis.available() > 0) {
                    val key = oos.readObject() as String
                    val `val` = oos.readObject()
                    if (`val` is Serializable && 0 != `val`) {
                        bundle.putSerializable(key, `val`)
                    }
                }
                return bundle
            }
        } catch (e: Exception) {
            //
        }
        return null
    }

    override fun onWebpageLoaded(url: String?, title: String?) {

    }

    override fun onWebpageLoadComplete() {
        viewDataBinding!!.swapRefresh.isRefreshing = false

    }

    private val resizeListener =
        View.OnApplyWindowInsetsListener { v: View?, insets: WindowInsets? ->
            if (v == null || activity == null) {
                return@OnApplyWindowInsetsListener insets!!
            }
            val r = Rect()
            v.getWindowVisibleDisplayFrame(r)
            val heightDifference = v.rootView.height - (r.bottom - r.top)
            // val navBarHeight = (activity as HomeActivity?)!!.navBarHeight
            val layoutParams = viewDataBinding!!.frame.layoutParams as ViewGroup.MarginLayoutParams
            if (heightDifference > 0 && layoutParams.bottomMargin != heightDifference) {
                //go into 'shrink' mode so no web site data is hidden
                layoutParams.bottomMargin = heightDifference
                viewDataBinding!!.frame.layoutParams = layoutParams
            } else if (heightDifference == 0) {
                layoutParams.bottomMargin = 0
                viewDataBinding!!.frame.layoutParams = layoutParams

            }
            insets!!
        }

    /*  override fun refreshEvent() {
          if (viewDataBinding!!.web3view.scrollY == 0) {
              loadUrl(viewDataBinding!!.web3view.url)
          }
      }*/

    //  override val currentScrollPosition: Int = viewDataBinding!!.web3view.scrollY

    private fun loadUrl(urlText: String?): Boolean {

        viewDataBinding!!.web3view.resetView()
        viewDataBinding!!.web3view.loadUrl(navArgs.url)
        viewDataBinding!!.web3view.requestFocus()
        return true
    }

    /**
     * Used to expand or collapse the view
     */
    private fun addToBackStack(nextFragment: String) {
        if (currentFragment != null && currentFragment != DAPP_BROWSER) {
            detachFragment(currentFragment)
        }
        currentFragment = nextFragment
    }

    private fun detachFragment(tag: String?) {
        if (!isAdded) return  //the dappBrowserFragment itself may not yet be attached.
        val fragment = childFragmentManager.findFragmentByTag(tag)
        if (fragment != null && fragment.isVisible && !fragment.isDetached) {
            fragment.onDetach()
            childFragmentManager.beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss()
        }

        //fragments can only be 1 deep
        currentFragment = DAPP_BROWSER
    }

}