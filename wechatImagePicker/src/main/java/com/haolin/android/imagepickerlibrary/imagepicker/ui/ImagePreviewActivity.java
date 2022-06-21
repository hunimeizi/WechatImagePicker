package com.haolin.android.imagepickerlibrary.imagepicker.ui;


import com.haolin.android.imagepickerlibrary.R;

public class ImagePreviewActivity extends AbstractImagePreviewActivity {
    @Override
    protected int attachLayoutRes() {
        return R.layout.imagepicker_activity_image_preview;
    }

    @Override
    protected int attachViewPagerRes() {
        return R.id.viewpager;
    }

    @Override
    protected int attachBottomViewRes() {
        return R.id.view_bottom;
    }

    @Override
    protected int attachCheckOriginRes() {
        return R.id.cb_origin;
    }

    @Override
    protected int attachCheckRes() {
        return R.id.cb_check;
    }

    @Override
    protected int attachTitleRes() {
        return R.id.tv_title;
    }

    @Override
    protected int attachBottomBarRes() {
        return R.id.bottom_bar;
    }

    @Override
    protected int attachButtonOkRes() {
        return R.id.btn_ok;
    }

    @Override
    protected int attachButtonBackRes() {
        return R.id.iv_back;
    }

}
