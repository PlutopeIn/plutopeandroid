package com.app.plutope.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowSecretPhrasePointsBinding
import com.app.plutope.model.PointModel


class PointListAdapter(
    var providerClick: ((PointModel)) -> Unit
) :
    ListAdapter<PointModel, PointListAdapter.ViewHolder>(DIFF_CALLBACK) {

    var selectedIndex: Int? = null

    inner class ViewHolder(var binding: RowSecretPhrasePointsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: PointModel) {

            binding.model = model
            binding.executePendingBindings()
        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<PointModel>() {
            override fun areItemsTheSame(
                oldModel: PointModel, newModel: PointModel
            ) = oldModel.id == newModel.id

            override fun areContentsTheSame(
                oldModel: PointModel,
                newModel: PointModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowSecretPhrasePointsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(currentList[position])


}