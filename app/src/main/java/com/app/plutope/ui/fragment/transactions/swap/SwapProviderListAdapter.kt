package com.app.plutope.ui.fragment.transactions.swap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.RowSwapProviderListItemBinding
import com.app.plutope.ui.fragment.providers.ProviderModel
import com.bumptech.glide.Glide

class SwapProviderListAdapter(
    var providerClick: ((ProviderModel) -> Unit)
) : ListAdapter<ProviderModel, SwapProviderListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(private val binding: RowSwapProviderListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ProviderModel, position: Int) {
            binding.model = model

            Glide.with(binding.imgProvider.context)
                .load(model.providerIcon)
                .placeholder(R.drawable.img_pluto_pe_logo_with_bg)
                .into(binding.imgProvider)

            binding.txtBest.visibility = if (position == 0) View.VISIBLE else View.GONE

            binding.root.setOnClickListener {
                providerClick.invoke(model)
            }
            binding.executePendingBindings()
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProviderModel>() {
            override fun areItemsTheSame(oldModel: ProviderModel, newModel: ProviderModel) =
                oldModel.providerName == newModel.providerName

            override fun areContentsTheSame(oldModel: ProviderModel, newModel: ProviderModel) =
                oldModel == newModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RowSwapProviderListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position)
}

