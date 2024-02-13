package com.app.plutope.browser.custom

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.webkit.WebBackForwardList
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import com.app.plutope.R
import com.app.plutope.browser.browserModel.DApp
import com.app.plutope.browser.utils.KeyboardUtils
import com.app.plutope.browser.utils.isDefaultDapp
import com.app.plutope.browser.utils.isValidUrl
import com.app.plutope.browser.utils.removeFromHistory
import com.google.android.material.appbar.MaterialToolbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class AddressBar(context: Context?, attributeSet: AttributeSet?) : MaterialToolbar(
    context!!, attributeSet
), ItemClickListener {
    private val ANIMATION_DURATION = 100
    private var urlTv: AutoCompleteTextView? = null
    private var adapter: DappBrowserSuggestionsAdapter? = null
    private var listener: AddressBarListener? = null
    private var btnClear: ImageView? = null
    private var layoutNavigation: View? = null
    private var back: ImageView? = null
    private var next: ImageView? = null
    private var home: ImageView? = null
    private var disposable: Disposable? = null
    private var focused = false

    init {
        inflate(context, R.layout.layout_url_bar_full, this)
        initView()
    }

    fun setup(list: MutableList<DApp>, listener: AddressBarListener?) {
        adapter = DappBrowserSuggestionsAdapter(
            context,
            list,
            this
        )
        this.listener = listener
        urlTv!!.setAdapter(null)
        urlTv!!.setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                load(urlTv!!.text.toString())
            }
            false
        }

        // Both these are required, the onFocus listener is required to respond to the first click.
        urlTv!!.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            //see if we have focus flag
            if (hasFocus && focused) openURLInputView()
        }
        urlTv!!.setOnClickListener { v: View? -> openURLInputView() }
        urlTv!!.showSoftInputOnFocus = true
        urlTv!!.setOnLongClickListener { v: View? ->
            urlTv!!.dismissDropDown()
            false
        }
    }

    private fun load(url: String) {
        listener!!.onLoad(url)
        expandCollapseView(layoutNavigation!!, true)
        leaveEditMode()
    }

    private fun initView() {
        urlTv = findViewById(R.id.url_tv)
        home = findViewById(R.id.home)
        if (home != null) home!!.setOnClickListener { v: View? ->
            disableNavigationButtons()
            val backForwardList = listener!!.onHomePagePressed()
            updateNavigationButtons(backForwardList)
        }
        btnClear = findViewById(R.id.clear_url)
        btnClear?.setOnClickListener(OnClickListener { v: View? -> clearAddressBar() })
        layoutNavigation = findViewById(R.id.layout_navigator)
        back = findViewById(R.id.back)
        back?.setOnClickListener(OnClickListener { v: View? ->
            disableNavigationButtons()
            val backForwardList = listener!!.loadPrevious()
            updateNavigationButtons(backForwardList)
        })
        next = findViewById(R.id.next)
        next?.setOnClickListener(OnClickListener { v: View? ->
            disableNavigationButtons()
            val backForwardList = listener!!.loadNext()
            updateNavigationButtons(backForwardList)
        })
    }

    private fun clearAddressBar() {
        if (urlTv!!.text.toString().isEmpty()) {
            KeyboardUtils.hideKeyboard(urlTv)
            listener!!.onClear()
        } else {
            urlTv!!.text.clear()
            openURLInputView()
            KeyboardUtils.showKeyboard(urlTv) //ensure keyboard shows here so we can listen for it being cancelled
        }
    }

    private fun openURLInputView() {
        urlTv!!.setAdapter(null)
        expandCollapseView(layoutNavigation!!, false)
        disposable = Observable.zip(
            Observable.interval(600, TimeUnit.MILLISECONDS).take(1),
            Observable.fromArray(btnClear)
        ) { _: Long?, item: ImageView? -> item!! }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { item: ImageView -> postBeginSearchSession(item) }
    }

    private fun postBeginSearchSession(item: ImageView) {
        urlTv!!.setAdapter(adapter)
        urlTv!!.showDropDown()
        if (item.visibility == GONE) {
            expandCollapseView(item, true)
            KeyboardUtils.showKeyboard(urlTv)
        }
    }

    @Synchronized
    private fun expandCollapseView(view: View, expandView: Boolean) {
        //detect if view is expanded or collapsed
        val isViewExpanded = view.visibility == VISIBLE

        //Collapse view
        if (isViewExpanded && !expandView) {
            val finalWidth = view.width
            val valueAnimator = slideAnimator(finalWidth, 0, view)
            valueAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    view.visibility = GONE
                }

                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
            })
            valueAnimator.start()
        } else if (!isViewExpanded && expandView) {
            view.visibility = VISIBLE
            val widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            val width = view.measuredWidth
            val valueAnimator = slideAnimator(0, width, view)
            valueAnimator.start()
        }
    }

    private fun slideAnimator(start: Int, end: Int, view: View): ValueAnimator {
        val animator = ValueAnimator.ofInt(start, end)
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            // Update Height
            val value = valueAnimator.animatedValue as Int
            val layoutParams = view.layoutParams
            layoutParams.width = value
            view.layoutParams = layoutParams
        }
        animator.setDuration(ANIMATION_DURATION.toLong())
        return animator
    }

    fun removeSuggestion(dApp: DApp) {
        dApp.url?.let { adapter!!.removeSuggestion(it) }
    }

    fun addSuggestion(dapp: DApp?) {
        dapp?.let { adapter!!.addSuggestion(it) }
    }

    fun shrinkSearchBar() {
        expandCollapseView(layoutNavigation!!, true)
        btnClear!!.visibility = GONE
        urlTv!!.dismissDropDown()
    }

    fun destroy() {
        if (disposable != null && !disposable!!.isDisposed) disposable!!.dispose()
    }

    fun clear() {
        if (urlTv != null) urlTv!!.text.clear()
    }

    fun leaveEditMode() {
        if (urlTv != null) {
            urlTv!!.clearFocus()
            KeyboardUtils.hideKeyboard(urlTv)
            btnClear!!.visibility = GONE
        }
        focused = true
    }

    fun leaveFocus() {
        if (urlTv != null) urlTv!!.clearFocus()
        focused = false
    }

    fun updateNavigationButtons(backForwardList: WebBackForwardList) {
        val isLast = backForwardList.currentIndex + 1 > backForwardList.size - 1
        if (isLast) {
            disableButton(next)
        } else {
            enableButton(next)
        }
        val isFirst = backForwardList.currentIndex == 0
        if (isFirst) {
            disableButton(back)
        } else {
            enableButton(back)
        }
    }

    val isOnHomePage: Boolean
        get() = isDefaultDapp(urlTv!!.text.toString())
    var url: String?
        get() = urlTv!!.text.toString()
        set(newUrl) {
            if (urlTv != null) urlTv!!.setText(newUrl)
        }

    private fun disableNavigationButtons() {
        disableButton(back)
        disableButton(next)
    }

    private fun enableButton(button: ImageView?) {
        button!!.isEnabled = true
        button.alpha = 1.0f
    }

    private fun disableButton(button: ImageView?) {
        button!!.isEnabled = false
        button.alpha = 0.3f
    }

    override fun onItemLongClick(url: String?) {
        //erase entry
        url?.let { adapter!!.removeSuggestion(it) }
        removeFromHistory(context, url!!)
        adapter!!.notifyDataSetChanged()
    }

    override fun onItemClick(url: String?) {
        if (isValidUrl(url!!)) {
            load(url)
        }
    }
}
