package com.app.plutope.ui.fragment.transactions.swap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowSwapProviderListItemBinding
import com.app.plutope.ui.fragment.providers.ProviderModel

class SwapProviderListAdapter(
    var providerClick: ((ProviderModel)) -> Unit
) :
    ListAdapter<ProviderModel, SwapProviderListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowSwapProviderListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ProviderModel) {
            binding.model = model
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                providerClick.invoke(model)
            }
        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ProviderModel>() {
            override fun areItemsTheSame(
                oldModel: ProviderModel, newModel: ProviderModel
            ) = oldModel.providerName == newModel.providerName

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
            RowSwapProviderListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))


}