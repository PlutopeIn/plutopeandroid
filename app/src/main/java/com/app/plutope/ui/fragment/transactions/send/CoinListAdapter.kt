package com.app.plutope.ui.fragment.transactions.send

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.RowCryptoCoinListItemBinding
import com.app.plutope.model.Tokens
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition


class CoinListAdapter(
    var providerClick: ((Tokens)) -> Unit
) :
    ListAdapter<Tokens, CoinListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowCryptoCoinListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Tokens) {
            binding.model = model
            binding.executePendingBindings()

            Glide.with(binding.imgCryptoCoin.context)
                .asBitmap()
                .load(model.t_logouri)
                .placeholder(R.drawable.img_pluto_pe_logo_with_bg)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        // Set the loaded image to the ImageView
                        binding.imgCryptoCoin.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Clear the ImageView or perform any other necessary operations
                    }
                })

            itemView.setOnClickListener {
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
            RowCryptoCoinListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

        /*if (position == currentList.lastIndex) {
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