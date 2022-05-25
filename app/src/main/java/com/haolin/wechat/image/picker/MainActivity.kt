package com.haolin.wechat.image.picker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.haolin.activityresultlauncher.launcher.StartActivityLauncher
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.view.CropImageView
import com.haolin.wechat.image.picker.utils.DensityUtil


class MainActivity : AppCompatActivity() {

    private val launcher = StartActivityLauncher(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .add(R.id.frameLayout, ImageSelectFragment.newInstance()).commit()
    }

    private fun chooseImage() {
        ImagePicker.instance
            .activityResultCaller(launcher)
            .multiMode(true) //多选
            .showCamera(true) //
            // .selectLimit(9)//最多选几张
            .crop(false) // 是否裁剪
            .justTakePictures(true) //是否直接拍照
            .outPutY((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片宽
            .outPutX((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片高
            .focusWidth((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) //裁剪框 宽
            .focusHeight((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪框 高
            .style(CropImageView.Style.RECTANGLE) //裁剪样式 圆形 矩形
            .selectedListener(object : ImagePicker.OnSelectedListener {
                override fun onImageSelected(items: List<ImageItem?>?) {
                    if(items == null) return
                    Toast.makeText(this@MainActivity,
                        "图片地址：${items[0]?.imageUrl}",
                        Toast.LENGTH_SHORT).show()
                }
            })
            .startImagePicker()
    }

}