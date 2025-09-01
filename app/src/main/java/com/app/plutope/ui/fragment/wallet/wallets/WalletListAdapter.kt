package com.app.plutope.ui.fragment.wallet.wallets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowWalletListItemBinding
import com.app.plutope.utils.extras.setSafeOnClickListener


class WalletListAdapter(
    var providerClick: ((com.app.plutope.model.Wallets)) -> Unit,
    var menuClick: ((com.app.plutope.model.Wallets)) -> Unit,
) :
    ListAdapter<com.app.plutope.model.Wallets, WalletListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowWalletListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: com.app.plutope.model.Wallets) {
            binding.model = model
            if (model.w_isprimary == 1) {
                binding.imgPrimary.visibility = RecyclerView.VISIBLE
            } else {
                binding.imgPrimary.visibility = RecyclerView.GONE
            }
            binding.executePendingBindings()

            itemView.setOnClickListener {
                providerClick.invoke(model)
            }
            binding.imgMenu.setSafeOnClickListener {
                menuClick.invoke(model)
            }
          /*  binding.imgShare.setSafeOnClickListener {
                shareClick.invoke(model)
            }*/
        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<com.app.plutope.model.Wallets>() {
            override fun areItemsTheSame(
                oldModel: com.app.plutope.model.Wallets, newModel: com.app.plutope.model.Wallets
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: com.app.plutope.model.Wallets,
                newModel: com.app.plutope.model.Wallets
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowWalletListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

        /*  if (position == currentList.lastIndex) {
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