package com.haolin.android.imagepickerlibrary.imagepicker

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.haolin.activityresultlauncher.launcher.StartActivityLauncher
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageFolder
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.loader.ImageLoader
import com.haolin.android.imagepickerlibrary.imagepicker.ui.AbstractImageGridActivity
import com.haolin.android.imagepickerlibrary.imagepicker.ui.AbstractImageGridActivity.Companion.EXTRAS_TAKE_PICKERS
import com.haolin.android.imagepickerlibrary.imagepicker.ui.ImageGridActivity
import com.haolin.android.imagepickerlibrary.imagepicker.view.CropImageView
import java.io.File
import java.io.IOException

class ImagePicker private constructor() {

    var isMultiMode = true //图片选择模式
        private set
    var activityResultCaller: StartActivityLauncher? = null

    var loadType = MediaType.IMAGE
        private set
    var selectLimit = 9 //最大选择图片数量
        private set
    var isCrop = true //裁剪
        private set
    var isShowCamera = true //显示相机
        private set
    var isSaveRectangle = false //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
        private set
    var outPutX = 800 //裁剪保存宽度
        private set
    var outPutY = 800 //裁剪保存高度
        private set
    var focusWidth = 280 //焦点框的宽度
        private set
    var focusHeight = 280 //焦点框的高度
        private set
    val imageLoader: ImageLoader by lazy { CoilIVLoader() }//图片加载器
    var style: CropImageView.Style? = CropImageView.Style.RECTANGLE //裁剪框的形状
        private set
    private var cropCacheFolder: File? = null
    var takeImageFile: File? = null
        private set
    var selectedImages: ArrayList<ImageItem>? = ArrayList() //选中的图片集合
        private set
    private var mImageFolders //所有的图片文件夹
            : MutableList<ImageFolder>? = null
    var currentImageFolderPosition = 0 //当前选中的文件夹位置 0表示所有图片
        private set
    private var mImageSelectedListeners // 图片选中的监听回调
            : MutableList<OnPictureSelectedListener>? = null
    private var onImageSelectedListener: OnSelectedListener? = null
    var isShareView = true
        private set
    var justTakePictures = false // 是否只是调用照相机拍照
        private set
    var viewerItem: List<String>? = null
        private set

    fun shareView(shareView: Boolean) {
        isShareView = shareView
    }

    fun justTakePictures(justTakePictures: Boolean): ImagePicker {
        this.justTakePictures = justTakePictures
        return this
    }

    fun activityResultCaller(caller: StartActivityLauncher): ImagePicker {
        activityResultCaller = caller
        return this
    }

    fun multiMode(multiMode: Boolean): ImagePicker {
        isMultiMode = multiMode
        return this
    }

    fun selectLimit(selectLimit: Int): ImagePicker {
        this.selectLimit = selectLimit
        return this
    }

    fun crop(crop: Boolean): ImagePicker {
        isCrop = crop
        return this
    }

    fun showCamera(showCamera: Boolean): ImagePicker {
        isShowCamera = showCamera
        return this
    }

    fun saveRectangle(isSaveRectangle: Boolean): ImagePicker {
        this.isSaveRectangle = isSaveRectangle
        return this
    }

    fun outPutX(outPutX: Int): ImagePicker {
        this.outPutX = outPutX
        return this
    }

    fun outPutY(outPutY: Int): ImagePicker {
        this.outPutY = outPutY
        return this
    }

    fun focusWidth(focusWidth: Int): ImagePicker {
        this.focusWidth = focusWidth
        return this
    }

    fun focusHeight(focusHeight: Int): ImagePicker {
        this.focusHeight = focusHeight
        return this
    }

    fun viewerItem(data: List<String>?) {
        viewerItem = data
    }

    fun getCropCacheFolder(context: Context): File {
        if(cropCacheFolder == null) {
            cropCacheFolder =
                File(context.externalCacheDir?.absolutePath!! + "/cropTemp/")
            cropCacheFolder!!.mkdirs()
        }
        return cropCacheFolder!!
    }

    fun cropCacheFolder(cropCacheFolder: File?): ImagePicker {
        this.cropCacheFolder = cropCacheFolder
        return this
    }

    fun style(style: CropImageView.Style?): ImagePicker {
        this.style = style
        return this
    }

    val imageFolders: List<ImageFolder>?
        get() = mImageFolders

    fun imageFolders(imageFolders: MutableList<ImageFolder>?): ImagePicker {
        mImageFolders = imageFolders
        return this
    }

    fun currentImageFolderPosition(mCurrentSelectedImageSetPosition: Int): ImagePicker {
        currentImageFolderPosition = mCurrentSelectedImageSetPosition
        return this
    }

    val currentImageFolderItems: ArrayList<ImageItem>
        get() = mImageFolders!![currentImageFolderPosition].images

    fun isSelect(item: ImageItem): Boolean {
        return selectedImages!!.contains(item)
    }

    val selectImageCount: Int
        get() = if(selectedImages == null) {
            0
        } else selectedImages!!.size

    fun selectedImages(selectedImages: ArrayList<ImageItem>?): ImagePicker? {
        if(selectedImages == null) {
            return null
        }
        this.selectedImages = selectedImages
        return this
    }

    fun clearSelectedImages() {
        if(selectedImages != null) selectedImages!!.clear()
    }


    fun selectedListener(listener: OnSelectedListener?): ImagePicker {
        onImageSelectedListener = listener
        return this
    }

    fun clear() {
        if(mImageSelectedListeners != null) {
            mImageSelectedListeners!!.clear()
            mImageSelectedListeners = null
        }
        if(mImageFolders != null) {
            mImageFolders!!.clear()
            mImageFolders = null
        }
        if(selectedImages != null) {
            selectedImages!!.clear()
        }
        currentImageFolderPosition = 0
    }

    /**
     * 图片选择
     */
    fun startImagePicker() {
        if(onImageSelectedListener == null) {
            Log.e(TAG, "\n\n\nOnImageSelectedListener is null , will not return any data\n\n\n")
        }
        activityResultCaller?.launch<ImageGridActivity>(
            EXTRAS_TAKE_PICKERS to justTakePictures
        ) { resultCode, data ->
            onActivityResult(resultCode, 100, data)
        }
    }

    private fun onActivityResult(resultCode: Int, requestCode: Int, data: Intent?) {
        if(resultCode == RESULT_CODE_ITEMS) {
            if(requestCode == 100) {
                val images: ArrayList<ImageItem?>? = data?.getParcelableArrayListExtra(
                    EXTRA_RESULT_ITEMS
                )
                if(onImageSelectedListener != null) {
                    onImageSelectedListener!!.onImageSelected(images)
                }
            } else {
                if(onImageSelectedListener != null) {
                    onImageSelectedListener!!.onImageSelected(null)
                }
            }
            onImageSelectedListener = null
        }
    }

    /**
     * 用于手机内存不足，进程被系统回收，重启时的状态恢复
     */
    fun restoreInstanceState(savedInstanceState: Bundle) {
        cropCacheFolder = savedInstanceState.getSerializable("cropCacheFolder") as File?
        takeImageFile = savedInstanceState.getSerializable("takeImageFile") as File?
        style = savedInstanceState.getSerializable("style") as CropImageView.Style?
        isMultiMode = savedInstanceState.getBoolean("multiMode")
        isCrop = savedInstanceState.getBoolean("crop")
        isShowCamera = savedInstanceState.getBoolean("showCamera")
        isSaveRectangle = savedInstanceState.getBoolean("isSaveRectangle")
        selectLimit = savedInstanceState.getInt("selectLimit")
        outPutX = savedInstanceState.getInt("outPutX")
        outPutY = savedInstanceState.getInt("outPutY")
        focusWidth = savedInstanceState.getInt("focusWidth")
        focusHeight = savedInstanceState.getInt("focusHeight")
    }

    /**
     * 拍照的方法
     */
    internal fun takePicture(activity: AppCompatActivity,launcher: ActivityResultLauncher<Intent>?) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(cameraIntent.resolveActivity(activity.packageManager) != null ||
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        ) {
            createCameraTempImageFile(activity)
            if(takeImageFile != null && takeImageFile!!.isFile) {
                val imageUri: Uri = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    FileProvider.getUriForFile(
                        activity,
                        activity.packageName + ".fileprovider",
                        takeImageFile!!
                    )
                else Uri.fromFile(takeImageFile)
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //对目标应用临时授权该Uri所代表的文件
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) //对目标应用临时授权该Uri所代表的文件
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri) //将拍取的照片保存到指定URI
                launcher?.launch(cameraIntent)
            } else {
                Toast.makeText(
                    activity, "图片错误",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                activity,
                "无法启动相机！",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createCameraTempImageFile(activity: Activity) {
        var dir = activity.externalCacheDir!!
        if(!dir.isDirectory) {
            if(!dir.mkdirs()) {
                dir = activity.getExternalFilesDir(null)!!
                if(!dir.exists()) {
                    dir = activity.filesDir
                    if(null == dir || !dir.exists()) {
                        dir = activity.filesDir
                        if(null == dir || !dir.exists()) {
                            val cacheDirPath =
                                File.separator + "data" + File.separator + "data" + File.separator + activity.packageName + File.separator + "cache" + File.separator
                            dir = File(cacheDirPath)
                            if(!dir.exists()) {
                                dir.mkdirs()
                            }
                        }
                    }
                }
            }
        }
        takeImageFile = try {
            File.createTempFile("IMG", ".jpg", dir)
        } catch(e: IOException) {
            e.printStackTrace()
            null
        }
    }

    interface OnPictureSelectedListener {
        fun onImageSelected(position: Int, item: ImageItem?, isAdd: Boolean)
    }

    interface OnSelectedListener {
        fun onImageSelected(items: List<ImageItem?>?)
    }


    companion object {
        val TAG = ImagePicker::class.java.simpleName
        const val REQUEST_CODE_TAKE = 1001
        const val REQUEST_CODE_CROP = 1002
        const val REQUEST_CODE_PREVIEW = 1003
        const val RESULT_CODE_ITEMS = 1004
        const val RESULT_CODE_BACK = 1005
        const val EXTRA_RESULT_ITEMS = "extra_result_items"
        const val EXTRA_SELECTED_IMAGE_POSITION = "selected_image_position"
        const val EXTRA_IMAGE_ITEMS = "extra_image_items"
        const val EXTRA_FROM_ITEMS = "extra_from_items"
        const val EXTRA_EXIT_POSITION = "extra_exit_position"
        private var mInstance: ImagePicker? = null
        val instance: ImagePicker
            get() {
                return mInstance ?: synchronized(ImagePicker::class.java) {
                    mInstance ?: ImagePicker()
                }.also { mInstance = it }
            }

        /**
         * 扫描图片
         */
        @JvmStatic
        fun galleryAddPic(context: Context, file: File?) {
            if(file == null) return
            MediaScannerConnection.scanFile(
                context, arrayOf(file.absolutePath),
                arrayOf(file.name), null
            )
        }
    }

    /**
     * 用于手机内存不足，进程被系统回收时的状态保存
     */
    fun saveInstanceState(outState: Bundle) {
        outState.putSerializable("cropCacheFolder", cropCacheFolder)
        outState.putSerializable("takeImageFile", takeImageFile)
        outState.putSerializable("style", style)
        outState.putBoolean("multiMode", isMultiMode)
        outState.putBoolean("crop", isCrop)
        outState.putBoolean("showCamera", isShowCamera)
        outState.putBoolean("isSaveRectangle", isSaveRectangle)
        outState.putInt("selectLimit", selectLimit)
        outState.putInt("outPutX", outPutX)
        outState.putInt("outPutY", outPutY)
        outState.putInt("focusWidth", focusWidth)
        outState.putInt("focusHeight", focusHeight)
    }

    fun addOnPictureSelectedListener(l: OnPictureSelectedListener) {
        if(mImageSelectedListeners == null) mImageSelectedListeners = ArrayList()
        mImageSelectedListeners!!.add(l)
    }

    fun removeOnPictureSelectedListener(l: OnPictureSelectedListener) {
        if(mImageSelectedListeners == null) return
        mImageSelectedListeners!!.remove(l)
    }

    fun addSelectedImageItem(position: Int, item: ImageItem, isAdd: Boolean) {
        if(isAdd) selectedImages!!.add(item) else selectedImages!!.remove(item)
        notifyImageSelectedChanged(position, item, isAdd)
    }

    private fun notifyImageSelectedChanged(position: Int, item: ImageItem, isAdd: Boolean) {
        if(mImageSelectedListeners == null) return
        for(l in mImageSelectedListeners!!) {
            l.onImageSelected(position, item, isAdd)
        }
    }
}