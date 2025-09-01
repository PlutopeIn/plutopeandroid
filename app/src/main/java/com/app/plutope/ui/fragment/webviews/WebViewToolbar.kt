package com.app.plutope.ui.fragment.webviews

import PermissionManager
import android.Manifest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.app.plutope.BR
import com.app.plutope.R
import com.app.plutope.databinding.FragmentWebViewToolbarBinding
import com.app.plutope.model.CoinCode
import com.app.plutope.ui.base.BaseFragment

class WebViewToolbar : BaseFragment<FragmentWebViewToolbarBinding, WebViewViewModel>() {
    private val webViewViewModel: WebViewViewModel by viewModels()
    private val args: WebViewToolbarArgs by navArgs()
    override fun getViewModel(): WebViewViewModel {
        return webViewViewModel
    }

    override fun getBindingVariable(): Int {
        return BR.webViewViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_web_view_toolbar
    }

    override fun setupToolbarText(): String {
        return args.title
    }

    override fun setupUI() {
        // requireContext().showLoader()

        viewDataBinding!!.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        viewDataBinding!!.txtToolbarTitle.text = args.title

        val webSettings: WebSettings = viewDataBinding!!.webView.settings
         webSettings.javaScriptEnabled = true
        webSettings.loadsImagesAutomatically = true
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.mixedContentMode =
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE // or WebSettings.MIXED_CONTENT_NEVER_ALLOW, WebSettings.MIXED_CONTENT_ALWAYS_ALLOW, etc.
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false




        if (args.provider == CoinCode.ONMETA.name) {
            requestPermission()


        } else {
            viewDataBinding!!.webView.loadUrl(args.url)
        }

        // Set a WebChromeClient to display JavaScript alerts, console messages, etc.
        viewDataBinding!!.webView.webChromeClient = WebChromeClient()


        // Set a WebChromeClient to display JavaScript alerts, console messages, etc.
        viewDataBinding!!.webView.webChromeClient = WebChromeClient()


        viewDataBinding!!.webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //  hideLoader()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return super.shouldOverrideUrlLoading(view, request)
            }


        }
    }

    override fun setupObserver() {

    }

    private fun requestPermission() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.CAMERA
        )

        PermissionManager.requestPermissions(
            requireActivity(),
            permissionsToRequest,
            object : PermissionManager.PermissionCallback {
                override fun onPermissionsGranted(permissions: List<String>) {
                    val htmlCode = """
            <script src="https://platform.onmeta.in/onmeta-sdk.js"></script>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <div id="widget"></div>
        """.trimIndent()
                    viewDataBinding!!.webView.loadDataWithBaseURL(
                        null,
                        htmlCode,
                        "text/html",
                        "UTF-8",
                        null
                    )
                    // openCameraTakePictureIntent(resultCaptureImageLauncher)
                }

                override fun onPermissionsDenied(permissions: List<String>) {
                    //showToast("Camera and storage access permission required")
                }
            })
    }


}