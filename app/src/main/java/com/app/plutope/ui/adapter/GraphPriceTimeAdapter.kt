package com.app.plutope.ui.adapter

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.R
import com.app.plutope.databinding.LayoutTimeSelectBinding
import com.app.plutope.model.TimeSelection


class GraphPriceTimeAdapter(
    var providerClick: ((TimeSelection)) -> Unit
) :
    ListAdapter<TimeSelection, GraphPriceTimeAdapter.ViewHolder>(DIFF_CALLBACK) {



    inner class ViewHolder(var binding: LayoutTimeSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: TimeSelection) {
            binding.model = model
            binding.txtTime.text = model.time
            if(model.isSelected){
                setBackgroundTextView(binding.txtTime,true)
            }else{
                setBackgroundTextView(binding.txtTime, false)
            }
            binding.executePendingBindings()

            binding.root.setOnClickListener {
                if(!model.isSelected){
                    currentList.filter { it.isSelected }.forEach { it.isSelected=false }
                    model.isSelected= true
                }else{
                    currentList.filter { it.isSelected }.forEach { it.isSelected=false }
                }
                notifyDataSetChanged()
                providerClick.invoke(model)
            }
        }


    }

    private fun setBackgroundTextView(textView: TextView, isAddBackground: Boolean) {
        if(isAddBackground) {

            val newBackgroundDrawable: Drawable = textView.resources.getDrawable(R.drawable.rounded_white)
            textView.background = newBackgroundDrawable

            val newBackgroundTintColor: ColorStateList? = ContextCompat.getColorStateList(textView.context, R.color.white_text)
            textView.backgroundTintList = newBackgroundTintColor
        }else{
            // Remove the background
            textView.background = null

            // Remove the background tint
            textView.backgroundTintList = null
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<TimeSelection>() {
            override fun areItemsTheSame(
                oldModel: TimeSelection, newModel: TimeSelection
            ) = oldModel.time == newModel.time

            override fun areContentsTheSame(
                oldModel: TimeSelection,
                newModel: TimeSelection
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutTimeSelectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(currentList[position])


}