package com.haolin.android.imagepickerlibrary.imagepicker.ui

import com.haolin.android.imagepickerlibrary.R

class ImageGridActivity : AbstractImageGridActivity() {
    override fun attachImmersiveColorRes(): Int {
        return R.color.ip_color_primary_dark
    }

    override fun attachTopBarRes(): Int {
        return R.id.top_bar
    }

    override fun attachImmersiveLightMode(): Boolean {
        return false
    }

    override fun attachLayoutRes(): Int {
        return R.layout.imagepicker_activity_image_grid
    }

    override fun attachRecyclerViewRes(): Int {
        return R.id.rc_view
    }

    override fun attachButtonBackRes(): Int {
        return R.id.iv_back
    }

    override fun attachButtonOkRes(): Int {
        return R.id.btn_ok
    }

    override fun attachButtonPreviewRes(): Int {
        return R.id.tv_preview
    }

    override fun attachFooterBarRes(): Int {
        return R.id.footer_bar
    }

    override fun attachDirectoryRes(): Int {
        return R.id.ll_dir
    }

    override fun attachDirectoryNameRes(): Int {
        return R.id.tv_dir
    }

    override fun attachTitleRes(): Int {
        return R.id.tv_title
    }

    override fun attachPreviewActivityClass(): Class<*> {
        return ImagePreviewActivity::class.java
    }

    override fun attachCropActivityClass(): Class<*> {
        return ImageCropActivity::class.java
    }
}