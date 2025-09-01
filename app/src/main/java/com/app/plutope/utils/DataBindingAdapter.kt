package com.app.plutope.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.app.plutope.ui.base.BaseAdapter
import com.app.plutope.ui.base.ListAdapterItem
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("imageResource")
fun setImageViewResource(imageView: ImageView, resource: Int) {
    imageView.setImageResource(resource)
}

@BindingAdapter("setAdapter")
fun setAdapter(
    recyclerView: RecyclerView,
    adapter: BaseAdapter<ViewDataBinding, ListAdapterItem>?
) {
    adapter?.let {
        recyclerView.adapter = it
    }
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter("submitList")
fun submitList(recyclerView: RecyclerView, list: List<Any>?) {
    val adapter = recyclerView.adapter as BaseAdapter<ViewDataBinding, Any>?
    adapter?.updateData(list ?: listOf())
}

@BindingAdapter("bannerImage")
fun loadBannerImage(view: ImageView, imageUrl: String?) {
    val requestOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(true)
        //.placeholder(R.drawable.icon_profile_default)
        //.error(R.drawable.error)
        .priority(Priority.HIGH)

    Glide.with(view.context)
        .asBitmap()
        .load(imageUrl)
        .apply(requestOptions)
        .into(view)
}








