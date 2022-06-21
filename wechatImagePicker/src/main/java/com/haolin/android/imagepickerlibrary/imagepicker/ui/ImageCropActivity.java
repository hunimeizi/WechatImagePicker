package com.haolin.android.imagepickerlibrary.imagepicker.ui;

import com.haolin.android.imagepickerlibrary.R;

public class ImageCropActivity extends AbstractImageCropActivity {
    @Override
    protected int attachButtonBackRes() {
        return R.id.iv_back;
    }

    @Override
    protected int attachButtonOkRes() {
        return R.id.btn_ok;
    }

    @Override
    protected int attachCropImageRes() {
        return R.id.cv_crop_image;
    }

    @Override
    protected int attachTitleRes() {
        return R.id.tv_title;
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.imagepicker_activity_image_crop;
    }
}
