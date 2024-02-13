package com.app.plutope.ui.fragment.wallet_connection.walletConnectionList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowNetworkListItemBinding
import com.app.plutope.networkConfig.Chains
import com.bumptech.glide.Glide

class NetworkListAdapter(
    var providerClick: ((Chains)) -> Unit
) :
    ListAdapter<Chains, NetworkListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowNetworkListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Chains) {
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
            DiffUtil.ItemCallback<Chains>() {
            override fun areItemsTheSame(
                oldModel: Chains, newModel: Chains
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: Chains,
                newModel: Chains
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowNetworkListItemBinding.inflate(
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