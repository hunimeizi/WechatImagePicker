package com.haolin.wechat.image.picker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.haolin.android.imagepickerlibrary.imagepicker.CoilIVLoader
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.view.CropImageView
import com.haolin.wechat.image.picker.utils.DensityUtil


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ImagePicker.getInstance().imageLoader(CoilIVLoader())
        findViewById<Button>(R.id.btnChooseImage).setOnClickListener {
            myRequetPermission()
        }
    }

    private fun myRequetPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        } else {
            chooseImage()
        }
    }

    private fun chooseImage(){
        ImagePicker.getInstance()
            .multiMode(false) //多选
            .showCamera(true) //                .selectLimit(9)//最多选几张
            .crop(true) // 是否裁剪
            .outPutY((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片宽
            .outPutX((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪图片高
            .focusWidth((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) //裁剪框 宽
            .focusHeight((DensityUtil.getScreenWidth(this) * 0.8f).toInt()) // 裁剪框 高
            .style(CropImageView.Style.RECTANGLE) //裁剪样式 圆形 矩形
            .selectedListener { items: List<ImageItem> ->
                Toast.makeText(this, "图片地址：${items[0].imageUrl}", Toast.LENGTH_SHORT).show()
            }
            .startImagePicker(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            for (i in permissions.indices) {
                if (grantResults[i] == PERMISSION_GRANTED) { //选择了“始终允许”

                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permissions[i]
                        )
                    ) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri =
                            Uri.fromParts("package", packageName, null) //注意就是"package",不用改成自己的包名
                        intent.data = uri
                        startActivityForResult(intent, 2)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePicker.getInstance().onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            myRequetPermission()//由于不知道是否选择了允许所以需要再次判断
        }
    }
}