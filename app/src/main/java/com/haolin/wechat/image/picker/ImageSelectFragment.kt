package com.haolin.wechat.image.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.haolin.activityresultlauncher.launcher.StartActivityLauncher
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.view.CropImageView
import com.haolin.wechat.image.picker.utils.DensityUtil

class ImageSelectFragment : Fragment() {

    private val startActivityLauncher = StartActivityLauncher(this)

    companion object {
        fun newInstance() = ImageSelectFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_image_select, container, false)
        val btnSelect = rootView.findViewById<Button>(R.id.btnSelect)
        btnSelect.setOnClickListener {
            chooseImage()
        }
        return rootView
    }

    private fun chooseImage() {
        ImagePicker.instance
            .activityResultCaller(startActivityLauncher)
            .multiMode(true) //多选
            .showCamera(true) //                .selectLimit(9)//最多选几张
            .crop(false) // 是否裁剪
            .outPutY((DensityUtil.getScreenWidth(requireActivity()) * 0.8f).toInt()) // 裁剪图片宽
            .outPutX((DensityUtil.getScreenWidth(requireActivity()) * 0.8f).toInt()) // 裁剪图片高
            .focusWidth((DensityUtil.getScreenWidth(requireActivity()) * 0.8f).toInt()) //裁剪框 宽
            .focusHeight((DensityUtil.getScreenWidth(requireActivity()) * 0.8f).toInt()) // 裁剪框 高
            .style(CropImageView.Style.RECTANGLE) //裁剪样式 圆形 矩形
            .selectedListener(object : ImagePicker.OnSelectedListener {
                override fun onImageSelected(items: List<ImageItem?>?) {
                    if(items == null) return
                    Toast.makeText(requireActivity(),
                        "图片地址：${items[0]?.imageUrl}",
                        Toast.LENGTH_SHORT).show()
                }
            })
            .startImagePicker()
    }
}