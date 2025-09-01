package com.app.plutope.ui.fragment.providers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowProviderListItemBinding
import com.app.plutope.utils.loge
import com.bumptech.glide.Glide


class ProviderListAdapter(
    var providerClick: ((ProviderModel)) -> Unit
) :
    ListAdapter<ProviderModel, ProviderListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowProviderListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ProviderModel) {
            binding.model = model

            binding.txtCryptoName.text = model.name
            binding.txtUsdPrice.text = if(model.isFromSell) model.currencyCode+model.bestPrice else model.bestPrice +" "+model.symbol

            loge("ImageUrl"," when set=> ${model.providerIcon}")
            Glide.with(binding.imgCryptoCoin.context).load(model.providerIcon).into(binding.imgCryptoCoin)
            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ProviderModel>() {
            override fun areItemsTheSame(
                oldModel: ProviderModel, newModel: ProviderModel
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: ProviderModel,
                newModel: ProviderModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowProviderListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

        /* if (position == currentList.lastIndex) {
             val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
             params.bottomMargin = 300
             holder.itemView.layoutParams = params
         } else {
             val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
             params.bottomMargin = 15
             holder.itemView.layoutParams = params
         }*/

    }


}