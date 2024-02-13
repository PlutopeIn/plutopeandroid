package com.app.plutope.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowDialogButtonListBinding
import com.app.plutope.model.ButtonModel

class BottomSheetButtonListAdapter(
    var list: MutableList<ButtonModel>,
    var providerClick: ((ButtonModel)) -> Unit
) :
    ListAdapter<ButtonModel, BottomSheetButtonListAdapter.ViewHolder>(DIFF_CALLBACK) {


    inner class ViewHolder(var binding: RowDialogButtonListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ButtonModel) {

            binding.buttonModel = model
            binding.imgButton.setImageDrawable(
                ResourcesCompat.getDrawable(
                    binding.imgButton.resources,
                    model.image,
                    null
                )
            )
            binding.executePendingBindings()

            binding.layoutMain.setOnClickListener {
                providerClick(list[layoutPosition])
            }
        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<ButtonModel>() {
            override fun areItemsTheSame(
                oldModel: ButtonModel, newModel: ButtonModel
            ) = oldModel.id == newModel.id

            override fun areContentsTheSame(
                oldModel: ButtonModel,
                newModel: ButtonModel
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowDialogButtonListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(list[position])

    override fun getItemCount(): Int {
        return list.size
    }


}