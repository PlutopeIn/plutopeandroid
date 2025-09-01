package com.app.plutope.browser.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Vibrator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.app.plutope.R
import com.app.plutope.browser.browserModel.DApp
import com.app.plutope.browser.utils.getBrowserHistory
import com.app.plutope.browser.utils.getDomainName
import com.app.plutope.browser.utils.getIconUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

class DappBrowserSuggestionsAdapter(
    context: Context,
    private val suggestions: MutableList<DApp>,
    private val listener: ItemClickListener
) : ArrayAdapter<DApp?>(context, 0, suggestions as List<DApp?>), Filterable {

    var filteredSuggestions: MutableList<DApp> = mutableListOf()
    private val vibrate: Vibrator
    fun addSuggestion(dapp: DApp) {
        if (!suggestions.contains(dapp)) {
            suggestions.add(dapp)
            notifyDataSetChanged()
        }
    }

    fun addSuggestions(dapps: List<DApp?>) {
        for (d in dapps) {
            if (!suggestions.contains(d)) {
                suggestions.add(d!!)
            }
        }
        notifyDataSetChanged()
    }

    fun removeSuggestion(dappUrl: String) {
        filterList(suggestions, dappUrl)
        filterList(filteredSuggestions, dappUrl)
    }

    private fun filterList(dappList: List<DApp>, urlToRemove: String) {
        val removeList: MutableList<Int> = ArrayList()
        for (i in dappList.indices) {
            if (dappList[i].url == urlToRemove) {
                removeList.add(i)
            }
        }
        removeList.sortWith { d1: Int?, d2: Int? ->
            d2!!.compareTo(d1!!)
        }

        //remove in reverse order
        for (i in removeList) {
            (dappList as MutableList).removeAt(i)
        }
    }

    override fun getCount(): Int {
        return filteredSuggestions.size
    }

    override fun getItem(position: Int): DApp {
        return filteredSuggestions[position]
    }

    override fun getFilter(): Filter {
        return SuggestionsFilter(this, suggestions)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val dapp = filteredSuggestions[position]
        val inflater = LayoutInflater.from(context)
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_browser_suggestion, parent, false)
        }
        val layout = convertView!!.findViewById<RelativeLayout>(R.id.layout)
        layout.setOnClickListener { listener.onItemClick(dapp.url) }
        layout.setOnLongClickListener { v: View? ->
            // vibrate.vibrate(100)
            listener.onItemLongClick(dapp.url)
            true
        }
        val icon = convertView.findViewById<ImageView>(R.id.icon)
        val visibleUrl = getDomainName(dapp.url)
        val favicon: String
        if (!TextUtils.isEmpty(visibleUrl)) {
            favicon = getIconUrl(visibleUrl)
            Glide.with(icon.context)
                .load(favicon) //.load(favicon)
                .apply(RequestOptions().circleCrop())
                .apply(RequestOptions().placeholder(R.drawable.img_logo_circle_black))
                .listener(requestListener)
                .into(icon)
        }
        val name = convertView.findViewById<TextView>(R.id.name)
        val description = convertView.findViewById<TextView>(R.id.description)
        name.text = dapp.name
        if (!TextUtils.isEmpty(dapp.description)) {
            description.text = dapp.description
        } else {
            description.text = dapp.url
        }
        return convertView
    }

    /**
     * Prevent glide dumping log errors - it is expected that load will fail
     */
    private val requestListener: RequestListener<Drawable?> = object : RequestListener<Drawable?> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Drawable?>,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any,
            target: Target<Drawable?>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            return false
        }
    }

    init {
        vibrate = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        //this.text = "";

        // Append browser history to known DApps list during initialisation
        addSuggestions(getBrowserHistory(context))
    }
}
