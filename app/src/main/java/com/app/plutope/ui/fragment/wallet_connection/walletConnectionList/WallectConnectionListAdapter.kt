package com.app.plutope.ui.fragment.wallet_connection.walletConnectionList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowWalletConnectionListItemBinding
import com.app.plutope.utils.walletConnection.compose_ui.connections.ConnectionUI
import com.bumptech.glide.Glide

class WalletConnectionListAdapter(
    var providerClick: ((ConnectionUI)) -> Unit
) :
    ListAdapter<ConnectionUI, WalletConnectionListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowWalletConnectionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ConnectionUI) {
            binding.model = model

            Glide.with(binding.imgConnection.context)
                .load(model.icon).into(binding.imgConnection)

            binding.root.setOnClickListener {
                providerClick.invoke(model)
            }

            binding.executePendingBindings()

        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ConnectionUI>() {
            override fun areItemsTheSame(
                oldModel: ConnectionUI, newModel: ConnectionUI
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: ConnectionUI,
                newModel: ConnectionUI
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowWalletConnectionListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])

    }


}