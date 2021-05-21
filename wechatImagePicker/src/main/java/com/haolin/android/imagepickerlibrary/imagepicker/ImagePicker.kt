package com.haolin.android.imagepickerlibrary.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.haolin.android.imagepickerlibrary.R
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageFolder
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.loader.ImageLoader
import com.haolin.android.imagepickerlibrary.imagepicker.ui.ImageGridActivity
import com.haolin.android.imagepickerlibrary.imagepicker.ui.ImageViewerActivity
import com.haolin.android.imagepickerlibrary.imagepicker.util.ProviderUtil
import com.haolin.android.imagepickerlibrary.imagepicker.util.Utils
import com.haolin.android.imagepickerlibrary.imagepicker.view.CropImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImagePicker private constructor() {
    var isMultiMode = true //图片选择模式
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
    var imageLoader //图片加载器
            : ImageLoader? = null
        private set
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
    var loadType = MediaType.IMAGE
        private set
    var viewerItem: List<String?>? = null
        private set

    fun shareView(shareView: Boolean) {
        isShareView = shareView
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

    fun loadType(loadType: MediaType): ImagePicker {
        this.loadType = loadType
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

    fun getCropCacheFolder(context: Context): File {
        if (cropCacheFolder == null) {
            cropCacheFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath!! + "/ImagePicker/cropTemp/")
            cropCacheFolder!!.mkdirs()
        }
        return cropCacheFolder!!
    }

    fun cropCacheFolder(cropCacheFolder: File?): ImagePicker {
        this.cropCacheFolder = cropCacheFolder
        return this
    }

    fun imageLoader(imageLoader: ImageLoader?): ImagePicker {
        this.imageLoader = imageLoader
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
        get() = if (selectedImages == null) {
            0
        } else selectedImages!!.size

    fun selectedImages(selectedImages: ArrayList<ImageItem>?): ImagePicker? {
        if (selectedImages == null) {
            return null
        }
        this.selectedImages = selectedImages
        return this
    }

    fun clearSelectedImages() {
        if (selectedImages != null) selectedImages!!.clear()
    }

    fun clear() {
        if (mImageSelectedListeners != null) {
            mImageSelectedListeners!!.clear()
            mImageSelectedListeners = null
        }
        if (mImageFolders != null) {
            mImageFolders!!.clear()
            mImageFolders = null
        }
        if (selectedImages != null) {
            selectedImages!!.clear()
        }
        currentImageFolderPosition = 0
    }

    /**
     * 拍照的方法
     */
    @Deprecated("")
    fun takePicture(activity: Activity, requestCode: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            takeImageFile = if (Utils.existSDCard()) File(Environment.getExternalStorageDirectory(), "/DCIM/camera/") else Environment.getDataDirectory()
            takeImageFile = createFile(takeImageFile, "IMG_", ".jpg")
            // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
            // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
            // 如果没有指定uri，则data就返回有数据！
            val uri: Uri
            if (VERSION.SDK_INT <= VERSION_CODES.M) {
                uri = Uri.fromFile(takeImageFile)
            } else {
                /**
                 * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
                 * 并且这样可以解决MIUI系统上拍照返回size为0的情况
                 */
                uri = FileProvider.getUriForFile(activity, ProviderUtil.getFileProviderName(activity), takeImageFile!!)
                //加入uri权限 要不三星手机不能拍照
                val resInfoList = activity.packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            Log.e("nanchen", ProviderUtil.getFileProviderName(activity))
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        activity.startActivityForResult(takePictureIntent, requestCode)
    }

    fun addOnPictureSelectedListener(l: OnPictureSelectedListener) {
        if (mImageSelectedListeners == null) mImageSelectedListeners = ArrayList()
        mImageSelectedListeners!!.add(l)
    }

    fun removeOnPictureSelectedListener(l: OnPictureSelectedListener) {
        if (mImageSelectedListeners == null) return
        mImageSelectedListeners!!.remove(l)
    }

    fun addSelectedImageItem(position: Int, item: ImageItem, isAdd: Boolean) {
        if (isAdd) selectedImages!!.add(item) else selectedImages!!.remove(item)
        notifyImageSelectedChanged(position, item, isAdd)
    }

    private fun notifyImageSelectedChanged(position: Int, item: ImageItem, isAdd: Boolean) {
        if (mImageSelectedListeners == null) return
        for (l in mImageSelectedListeners!!) {
            l.onImageSelected(position, item, isAdd)
        }
    }

    /**
     * 用于手机内存不足，进程被系统回收，重启时的状态恢复
     */
    fun restoreInstanceState(savedInstanceState: Bundle) {
        cropCacheFolder = savedInstanceState.getSerializable("cropCacheFolder") as File?
        takeImageFile = savedInstanceState.getSerializable("takeImageFile") as File?
        imageLoader = savedInstanceState.getParcelable("imageLoader")
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

    fun selectedListener(listener: OnSelectedListener?): ImagePicker {
        onImageSelectedListener = listener
        return this
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 100) {
                val images: ArrayList<ImageItem?> = data.getParcelableArrayListExtra(EXTRA_RESULT_ITEMS)!!
                if (onImageSelectedListener != null) {
                    onImageSelectedListener!!.onImageSelected(images)
                }
            } else {
                if (onImageSelectedListener != null) {
                    onImageSelectedListener!!.onImageSelected(null)
                }
            }
            Log.v("TAG", "onActivityResult")
            onImageSelectedListener = null
        }
    }

    @JvmOverloads
    fun startPhotoPicker(activity: Activity, clazz: Class<*>? = ImageGridActivity::class.java) {
        if (onImageSelectedListener == null) {
            Log.e(TAG, "\n\n\nOnImageSelectedListener is null , will not return any data\n\n\n")
        }
        multiMode(false)
        instance!!.selectLimit(1)
        val intent = Intent(activity, clazz)
        intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true) // 是否是直接打开相机
        activity.startActivityForResult(intent, 100)
    }

    fun startImagePicker(activity: Activity, clazz: Class<*>?, images: ArrayList<ImageItem>?) {
        if (onImageSelectedListener == null) {
            Log.e(TAG, "\n\n\nOnImageSelectedListener is null , will not return any data\n\n\n")
        }
        val intent = Intent(activity, clazz)
        if (images != null) {
            intent.putParcelableArrayListExtra(ImageGridActivity.EXTRAS_IMAGES, images)
            instance!!.selectedImages(images)
        }
        loadType(MediaType.IMAGE)
        activity.startActivityForResult(intent, 100)
    }

    fun startImagePicker(fragment: Fragment, clazz: Class<*>?, images: ArrayList<ImageItem>?) {
        if (onImageSelectedListener == null) {
            Log.e(TAG, "\n\n\nOnImageSelectedListener is null , will not return any data\n\n\n")
        }
        val intent = Intent(fragment.context, clazz)
        if (images != null) {
            intent.putParcelableArrayListExtra(ImageGridActivity.EXTRAS_IMAGES, images)
            instance!!.selectedImages(images)
        }
        loadType(MediaType.IMAGE)
        fragment.startActivityForResult(intent, 100)
    }

    @JvmOverloads
    fun startImagePicker(activity: Activity, images: ArrayList<ImageItem>? = null) {
        startImagePicker(activity, ImageGridActivity::class.java, images)
    }

    @JvmOverloads
    fun startImagePicker(fragment: Fragment, images: ArrayList<ImageItem>? = null) {
        startImagePicker(fragment, ImageGridActivity::class.java, images)
    }

    @JvmOverloads
    fun startVideoPicker(activity: Activity, clazz: Class<*>? = ImageGridActivity::class.java) {
        if (onImageSelectedListener == null) {
            Log.e(TAG, "\n\n\nOnImageSelectedListener is null , will not return any data\n\n\n")
        }
        crop(false)
        showCamera(false)
        selectLimit(1)
        multiMode(false)
        loadType(MediaType.VIDEO)
        val intent = Intent(activity, clazz)
        activity.startActivityForResult(intent, 100)
    }

    @JvmOverloads
    fun startImageViewer(activity: Activity, images: List<String?>?, view: View? = null, position: Int = 0) {
        if (images == null || images.size == 0) return
        instance!!.viewerItem(images)
        val intent = Intent(activity, ImageViewerActivity::class.java)
        intent.putExtra(EXTRA_SELECTED_IMAGE_POSITION, position)
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && instance!!.isShareView && view != null) {
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, Pair.create(view, activity.getString(R.string.share_view_photo) + position))
            ActivityCompat.startActivity(activity, intent, option.toBundle())
        } else {
            activity.startActivity(intent)
        }
    }

    fun startImageViewer(activity: Activity, images: List<String?>?, position: Int) {
        startImageViewer(activity, images, null, position)
    }

    fun viewerItem(data: List<String?>?) {
        viewerItem = data
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
        @JvmStatic
        val instance: ImagePicker?
            get() {
                if (mInstance == null) {
                    synchronized(ImagePicker::class.java) {
                        if (mInstance == null) {
                            mInstance = ImagePicker()
                        }
                    }
                }
                return mInstance
            }

        /**
         * 根据系统时间、前缀、后缀产生一个文件
         */
        fun createFile(folder: File?, prefix: String, suffix: String): File {
            if (!folder!!.exists() || !folder.isDirectory) folder.mkdirs()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
            val filename = prefix + dateFormat.format(Date(System.currentTimeMillis())) + suffix
            return File(folder, filename)
        }

        /**
         * 扫描图片
         */
        @JvmStatic
        fun galleryAddPic(context: Context, file: File?) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            context.sendBroadcast(mediaScanIntent)
        }
    }
}