package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.haolin.android.imagepickerlibrary.R
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.util.BitmapUtil
import com.haolin.android.imagepickerlibrary.imagepicker.view.CropImageView
import java.io.File

abstract class AbstractImageCropActivity : ImageBaseActivity(), View.OnClickListener,
    CropImageView.OnBitmapSaveCompleteListener {
    private lateinit var mCropImageView: CropImageView
    private var mBitmap: Bitmap? = null
    private var mIsSaveRectangle = false
    private var mOutputX = 0
    private var mOutputY = 0
    private var mImageItems: ArrayList<ImageItem>? = null
    private var imagePicker: ImagePicker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePicker = ImagePicker.instance

        //初始化View
        findViewById<View>(attachButtonBackRes()).setOnClickListener(this)
        val btn_ok = findViewById<Button>(attachButtonOkRes())
        btn_ok.text = getString(R.string.ip_complete)
        btn_ok.setOnClickListener(this)
        val tv_des = findViewById<TextView>(attachTitleRes())
        tv_des.text = getString(R.string.ip_photo_crop)
        mCropImageView = findViewById(attachCropImageRes())
        mCropImageView.setOnBitmapSaveCompleteListener(this)

        //获取需要的参数
        mOutputX = imagePicker!!.outPutX
        mOutputY = imagePicker!!.outPutY
        mIsSaveRectangle = imagePicker!!.isSaveRectangle
        mImageItems = imagePicker!!.selectedImages
        val imagePath = mImageItems!![0].path
        mCropImageView.focusStyle = imagePicker!!.style
        mCropImageView.focusWidth = imagePicker!!.focusWidth
        mCropImageView.focusHeight = imagePicker!!.focusHeight

        //缩放图片
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        val displayMetrics = resources.displayMetrics
        options.inSampleSize =
            calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels)
        options.inJustDecodeBounds = false
        mBitmap = BitmapFactory.decodeFile(imagePath, options)
        mCropImageView.setImageBitmap(mCropImageView.rotate(mBitmap,
            BitmapUtil.getBitmapDegree(imagePath)))
    }

    protected abstract fun attachCropImageRes(): Int
    protected abstract fun attachTitleRes(): Int
    protected abstract fun attachButtonBackRes(): Int
    protected abstract fun attachButtonOkRes(): Int


    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if(height > reqHeight || width > reqWidth) {
            inSampleSize = if(width > height) {
                width / reqWidth
            } else {
                height / reqHeight
            }
        }
        return inSampleSize
    }

    override fun onClick(v: View) {
        val id = v.id
        if(id == attachButtonBackRes()) {
            setResult(RESULT_CANCELED)
            finish()
        } else if(id == attachButtonOkRes()) {
            mCropImageView.saveBitmapToFile(imagePicker!!.getCropCacheFolder(this),
                mOutputX,
                mOutputY,
                mIsSaveRectangle)
        }
    }

    override fun onBitmapSaveSuccess(file: File) {
        mImageItems!!.removeAt(0)
        val imageItem = ImageItem()
        imageItem.path = file.absolutePath
        mImageItems!!.add(imageItem)
        val intent = Intent()
        intent.putParcelableArrayListExtra(ImagePicker.EXTRA_RESULT_ITEMS, mImageItems)
        setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //单选不需要裁剪，返回数据
        finish()
    }

    override fun onBitmapSaveError(file: File) {}
    override fun onDestroy() {
        super.onDestroy()
        mCropImageView.setOnBitmapSaveCompleteListener(null)
        if(null != mBitmap && !mBitmap!!.isRecycled) {
            mBitmap!!.recycle()
            mBitmap = null
        }
    }
}