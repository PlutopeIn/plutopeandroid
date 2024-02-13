package com.app.plutope.ui.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowCoinListSearchBinding
import com.app.plutope.model.Tokens
import com.bumptech.glide.Glide


class SearchCoinListAdapter(
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

        if (position == currentList.lastIndex) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 300
            holder.itemView.layoutParams = params
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 15
            holder.itemView.layoutParams = params
        }

    }


}