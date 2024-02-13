package com.app.plutope.browser.custom

import android.widget.Filter
import com.app.plutope.browser.browserModel.DApp
import java.util.Locale

class SuggestionsFilter(
    private val adapter: DappBrowserSuggestionsAdapter,
    originalList: List<DApp>
) : Filter() {
    private val originalList: List<DApp>
    private val filteredList: MutableList<DApp>

    init {
        this.originalList = originalList
        filteredList = ArrayList<DApp>()
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        filteredList.clear()
        val results = FilterResults()
        if (constraint.isEmpty()) {
            filteredList.addAll(originalList)
        } else {
            val filterPattern =
                constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
            for (dapp in originalList) {
                if (dapp.name?.lowercase()!!.contains(filterPattern)) {
                    filteredList.add(dapp)
                }
            }
        }
        results.values = filteredList
        results.count = filteredList.size
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapter.filteredSuggestions.clear()
        if (results.values != null) adapter.filteredSuggestions.addAll(results.values as MutableList<DApp>)
        adapter.notifyDataSetChanged()
    }
}
