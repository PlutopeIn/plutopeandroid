package com.app.plutope.ui.adapter

import android.view.View.GONE
import android.view.View.VISIBLE
import com.app.plutope.R
import com.app.plutope.databinding.RowCoinListSearchBinding
import com.app.plutope.model.Tokens
import com.app.plutope.ui.base.BaseAdapter
import com.bumptech.glide.Glide


/*class SearchCoinListAdapter(
    var isFromSwap:Boolean=true,
    var providerClick: ((Tokens)) -> Unit
) :
    ListAdapter<Tokens, SearchCoinListAdapter.ViewHolder>(DIFF_CALLBACK) {
    inner class ViewHolder(var binding: RowCoinListSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Tokens) {
            binding.model = model
            if(isFromSwap) binding.txtBtcPrice.visibility=VISIBLE else binding.txtBtcPrice.visibility= GONE

            Glide.with(binding.imgCryptoCoin.context).load(model.t_logouri)
                .into(binding.imgCryptoCoin)
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Tokens>() {
            override fun areItemsTheSame(
                oldModel: Tokens, newModel: Tokens
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: Tokens,
                newModel: Tokens
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            RowCoinListSearchBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

}*/



class SearchCoinListAdapter(
    var list: MutableList<Tokens>,
    var isFromSwap: Boolean = true,
    var providerClick: ((Tokens)) -> Unit
) : BaseAdapter<RowCoinListSearchBinding, Tokens>(list) {

    override val layoutId: Int = R.layout.row_coin_list_search

    override fun bind(binding: RowCoinListSearchBinding, item: Tokens) {
        binding.apply {
            // imgCountryFlag = ImageCategory.PRODUCT
            model = item
            // providerClick.invoke(item)

            if (isFromSwap) binding.txtBtcPrice.visibility =
                VISIBLE else binding.txtBtcPrice.visibility = GONE

            val imageUrl =
                if (item.t_logouri != "" || item.t_logouri.isNotEmpty()) item.t_logouri else item.chain?.icon

            val img = when (item.t_type.lowercase()) {
                "erc20" -> R.drawable.img_eth_logo
                "bep20" -> R.drawable.ic_bep
                "polygon" -> R.drawable.ic_polygon
                "kip20" -> R.drawable.ic_kip
                else -> {
                    R.drawable.img_eth_logo
                }
            }


            Glide.with(binding.imgCryptoCoin.context).load(imageUrl)
                .placeholder(img)
                .error(img)
                .into(binding.imgCryptoCoin)



            binding.executePendingBindings()

            binding.root.setOnClickListener {
                providerClick.invoke(item)
            }

            // txtCurrencyName.m = item.name
            executePendingBindings()
        }
    }


}