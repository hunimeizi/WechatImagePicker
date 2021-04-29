package com.haolin.android.imagepickerlibrary.imagepicker

import android.net.Uri
import android.widget.ImageView
import coil.load
import com.haolin.android.imagepickerlibrary.imagepicker.loader.ImageLoader
import java.io.File

class CoilIVLoader : ImageLoader {
    override fun displayFileImage(imageView: ImageView, path: String) {
        imageView.load(Uri.fromFile(File(path)))
    }

    override fun displayUserImage(imageView: ImageView, path: String) {
        imageView.load(path)
    }

    override fun displayFileVideo(path: String) {}

    override fun displayFullImageClass(): Class<*>? {
        return null
    }
}