package com.app.plutope.utils

import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener

class SvgSoftwareLayerSetter : RequestListener<PictureDrawable> {

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<PictureDrawable>?,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }

    override fun onResourceReady(
        resource: PictureDrawable?,
        model: Any?,
        target: com.bumptech.glide.request.target.Target<PictureDrawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        target?.onResourceReady(resource!!, null)
        return true
    }

}