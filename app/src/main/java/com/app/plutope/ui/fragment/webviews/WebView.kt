package com.app.plutope.ui.fragment.webviews

import PermissionManager
import android.Manifest
import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
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
import com.app.plutope.databinding.FragmentWebViewBinding
import com.app.plutope.model.CoinCode
import com.app.plutope.ui.base.BaseFragment
import com.app.plutope.utils.redirectToChrome
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebView : BaseFragment<FragmentWebViewBinding, WebViewViewModel>() {

    private val webViewViewModel: WebViewViewModel by viewModels()
    private val args: WebViewArgs by navArgs()
    override fun getViewModel(): WebViewViewModel {
        return webViewViewModel
    }


    override fun getBindingVariable(): Int {
        return BR.webViewViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_web_view
    }

    override fun setupToolbarText(): String {
        return ""
    }

    override fun setupUI() {
        viewDataBinding?.imgBack?.setOnClickListener {
            findNavController().navigateUp()
        }

        // viewDataBinding!!.progressBar.visibility = VISIBLE
        viewDataBinding!!.txtToolbarTitle.text = args.title
        val webSettings: WebSettings = viewDataBinding!!.webView.settings
        webSettings.loadsImagesAutomatically = true
        webSettings.javaScriptEnabled = true
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.mixedContentMode =
            WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE or WebSettings.MIXED_CONTENT_NEVER_ALLOW or WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.allowFileAccess = true


        // Load the HTML code into the WebView
        //<script src="https://platform.onmeta.in/onmeta-sdk.js"></script>
        viewDataBinding!!.webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Your implementation
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)

                request?.grant(request.resources)
            }

            override fun onPermissionRequestCanceled(request: PermissionRequest?) {
                super.onPermissionRequestCanceled(request)
            }
        }

        if (args.provider == CoinCode.ONMETA.name) {
            /*  viewDataBinding!!.webView.loadDataWithBaseURL(
                  null,
                  htmlCode,
                  "text/html",
                  "UTF-8",
                  null
              )*/
            requestPermission()
        } else {
            viewDataBinding!!.webView.loadUrl(args.url)
        }

        // Set a WebChromeClient to display JavaScript alerts, console messages, etc.
        viewDataBinding!!.webView.webChromeClient = WebChromeClient()

        viewDataBinding!!.webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // viewDataBinding!!.progressBar.visibility = GONE
                if (args.provider == CoinCode.ONMETA.name) {
                    viewDataBinding!!.webView.evaluateJavascript(args.url, null)
                }


            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if(request?.url.toString().startsWith("https://platform.onmeta.in/kyc")){
                    redirectToChrome(requireContext(),request?.url.toString())
                    return true
                }
                return false
            }


        }


    }

    override fun setupObserver() {

    }

    private fun requestPermission() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.CAMERA,
            /*Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS*/
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