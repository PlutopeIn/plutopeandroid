package com.app.plutope.ui.fragment.dashboard.assets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.app.plutope.R
import com.app.plutope.databinding.RowAssetsListBinding
import com.app.plutope.model.Tokens
import com.app.plutope.utils.extras.PreferenceHelper
import com.app.plutope.utils.getImageResource
import com.bumptech.glide.Glide
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

class AssetsAdapter(var list: MutableList<Tokens>, var listener: (Tokens) -> Unit) :
    RecyclerView.Adapter<AssetsAdapter.AssetViewHolder>(), Filterable {
    private var context: Context? = null
    var filteredList: MutableList<Tokens> = list

    inner class AssetViewHolder(val binding: RowAssetsListBinding) : ViewHolder(binding.root) {
        fun bind(model: Tokens) {
            binding.model = model
            binding.currencySymbol = PreferenceHelper.getInstance().getSelectedCurrency()?.symbol ?: ""
            if (model.isCustomTokens == true) {
                val img =getImageResource(model.t_type)
                Glide.with(context!!).load(img).into(binding.imgCryptoCoin)
            } else {
                Glide.with(context!!).load(model.t_logouri).into(binding.imgCryptoCoin)
            }

            val percentChange = model.t_last_price_change_impact?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val pricePercent = percentChange.setScale(2, RoundingMode.DOWN).toString()
            val color = if (pricePercent < "0") context?.resources!!.getColor(
                R.color.red,
                null
            ) else context?.resources!!.getColor(R.color.green_099817, null)
            binding.txtCryptoDiffrencePercentage.text =
                if (pricePercent < "0") "$pricePercent%" else "+$pricePercent%"
            binding.txtCryptoDiffrencePercentage.setTextColor(color)


            val price = model.t_balance.toBigDecimal() * model.t_price!!.toBigDecimal()
            val formattedPrice = price.setScale(2, RoundingMode.DOWN).toString()
            binding.txtUsdPrice.text = if (price.toDouble() > 0.0) "${
                PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
            }$formattedPrice" else {
                ""
            }


            /*
                        CoroutineScope(Dispatchers.Main).launch{
                            model.callFunction.getBalance {
                                    binding.model?.t_balance = it.toString()
                            }
                        }
            */


            binding.executePendingBindings()
        }

        init {
            binding.root.setOnClickListener {
                listener.invoke(filteredList[layoutPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(parent.context)
        PreferenceHelper.getInstance().getSelectedCurrency()?.symbol
        val binding = RowAssetsListBinding.inflate(inflater, parent, false)
        return AssetViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(filteredList[position])
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredDataList = mutableListOf<Tokens>()

                if (constraint.isNullOrEmpty()) {
                    filteredDataList.addAll(list)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    for (item in list) {
                        // Filter based on your specific condition
                        if (item.t_symbol?.lowercase(Locale.getDefault())
                                ?.contains(filterPattern) == true
                        ) {
                            filteredDataList.add(item)
                        }
                    }
                }

                results.values = filteredDataList
                results.count = filteredDataList.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList =
                    (results?.values as? List<Tokens> ?: emptyList()) as MutableList<Tokens>
                notifyDataSetChanged()
            }
        }

    }

    fun removeItem(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(item: Tokens, position: Int) {
        list.add(position, item)
        notifyItemInserted(position)
    }

    fun sortListByPrice() {
        filteredList = list.distinct().sortedWith(compareByDescending { item ->
            val balance = item.t_balance.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val price = item.t_price?.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val result = balance * price
            result
        }) as MutableList<Tokens>
        notifyDataSetChanged()
    }
}