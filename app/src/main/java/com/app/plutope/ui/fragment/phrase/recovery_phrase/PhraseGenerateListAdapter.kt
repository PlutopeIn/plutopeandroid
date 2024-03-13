package com.app.plutope.ui.fragment.phrase.recovery_phrase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowSecretPhraseBinding
import com.app.plutope.utils.extras.buttonClickedWithEffect


class PhraseGenerateListAdapter(
    var providerClick: ((String), Int) -> Unit
) : ListAdapter<String, PhraseGenerateListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowSecretPhraseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: String) {
            binding.txtCount.text = (layoutPosition + 1).toString()+"."
            binding.txtPhrases.text = model

            itemView.buttonClickedWithEffect {
                providerClick.invoke(model, layoutPosition)
            }

            binding.executePendingBindings()
        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(
                oldModel: String, newModel: String
            ) = oldModel == newModel

            override fun areContentsTheSame(
                oldModel: String,
                newModel: String
            ) = oldModel == newModel
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            RowSecretPhraseBinding.inflate(
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