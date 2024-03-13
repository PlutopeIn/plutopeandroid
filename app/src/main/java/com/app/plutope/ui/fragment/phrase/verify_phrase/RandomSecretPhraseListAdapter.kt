package com.app.plutope.ui.fragment.phrase.verify_phrase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.databinding.RowSecretPhrasePickedBinding
import com.app.plutope.utils.extras.buttonClickedWithEffect


class RandomSecretPhraseListAdapter(
    var providerClick: ((String), Int) -> Unit
) :
    ListAdapter<String, RandomSecretPhraseListAdapter.ViewHolder>(DIFF_CALLBACK) {

    inner class ViewHolder(var binding: RowSecretPhrasePickedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: String) {
            binding.txtPhrases.text = model
            binding.executePendingBindings()

            itemView.buttonClickedWithEffect {
                providerClick.invoke(model, layoutPosition)
            }
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
            RowSecretPhrasePickedBinding.inflate(
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