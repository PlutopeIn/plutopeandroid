package com.app.plutope.ui.fragment.dashboard.nfts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowNftListItemBinding
import com.app.plutope.model.NFTModel
import com.bumptech.glide.Glide


class NftsListAdapter(
    var providerClick: ((NFTModel)) -> Unit
) :
    ListAdapter<NFTModel, NftsListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowNftListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: NFTModel) {

            binding.model = model

            Glide.with(binding.imgNft.context).load(model.parsedMetadata?.image)
                .into(binding.imgNft)
            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<NFTModel>() {
            override fun areItemsTheSame(
                oldModel: NFTModel, newModel: NFTModel
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: NFTModel,
                newModel: NFTModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowNftListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

        if (position == currentList.lastIndex || position == (currentList.lastIndex - 1)) {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 50
            holder.itemView.layoutParams = params
        } else {
            val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
            params.bottomMargin = 45
            holder.itemView.layoutParams = params
        }
    }


}