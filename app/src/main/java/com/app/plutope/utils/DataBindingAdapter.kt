package com.app.plutope.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.app.plutope.R
import com.app.plutope.ui.base.BaseAdapter
import com.app.plutope.ui.base.ListAdapterItem
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.util.*

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


@BindingAdapter("profileImage")
fun loadProfileImage(view: ImageView, imageUrl: String?) {
    //create progress drawable
    val progress = CircularProgressDrawable(view.context)
    progress.strokeWidth = 5f
    progress.centerRadius = 40f
    progress.start()

    if (!imageUrl.isNullOrEmpty()) {

        val imageToLoad = "$imageUrl"
        val reqOption = RequestOptions().let {
            it.placeholder(progress)
        }

        //set image
        Glide.with(view.context)
            .load(imageToLoad)
            .apply(reqOption)
            .into(view)
    } else {
        //set image resource directly
        Glide.with(view.context)
            .load(R.drawable.img_logo_circle).into(view)

    }
}





