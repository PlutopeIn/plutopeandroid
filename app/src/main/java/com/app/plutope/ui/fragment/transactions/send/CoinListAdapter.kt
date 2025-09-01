package com.app.plutope.ui.fragment.transactions.send

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.RowCryptoCoinListItemBinding
import com.app.plutope.model.Tokens
import com.bumptech.glide.Glide


class CoinListAdapter(
    var providerClick: ((Tokens) -> Unit)
) : ListAdapter<Tokens, CoinListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowCryptoCoinListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Tokens) {
            binding.model = model
            binding.executePendingBindings()

            // Load image directly into the ImageView
            val imageUrl =
                if (model.t_logouri != "" || model.t_logouri.isNotEmpty()) model.t_logouri else model.chain?.icon

            val img = when (model.t_type.lowercase()) {
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


            /* Glide.with(binding.imgCryptoCoin.context)
                 .load(model.t_logouri)
                 .placeholder(R.drawable.img_no_image)
                 .into(binding.imgCryptoCoin)*/

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Tokens>() {
            override fun areItemsTheSame(oldModel: Tokens, newModel: Tokens) =
                oldModel.t_pk == newModel.t_pk // Compare IDs

            override fun areContentsTheSame(oldModel: Tokens, newModel: Tokens) =
                oldModel == newModel // Optional: Full content comparison
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowCryptoCoinListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)) // Use getItem for better efficiency
    }
}


/*
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
                        binding.imgCryptoCoin.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
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
    }


}*/
