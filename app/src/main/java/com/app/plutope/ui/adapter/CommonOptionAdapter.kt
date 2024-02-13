package com.app.plutope.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowTextviewBinding
import com.app.plutope.model.CommonOptionModel

class CommonOptionAdapter(val list: MutableList<CommonOptionModel>,val onClickListner:(CommonOptionModel)->Unit) :
    RecyclerView.Adapter<CommonOptionAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): CommonOptionAdapter.MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RowTextviewBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CommonOptionAdapter.MyViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class MyViewHolder(var binding: RowTextviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommonOptionModel) {
            binding.model = item
            binding.txtLockMethodValue.text = item.name

            binding.imgTick.isVisible = item.isSelected
            binding.executePendingBindings()
        }

        init {

            binding.root.setOnClickListener {
                onClickListner.invoke(list[layoutPosition])

            }

        }
    }

}
