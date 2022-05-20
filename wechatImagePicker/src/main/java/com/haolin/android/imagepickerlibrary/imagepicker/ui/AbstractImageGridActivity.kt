package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haolin.android.imagepickerlibrary.*
import com.haolin.android.imagepickerlibrary.imagepicker.DataHolder
import com.haolin.android.imagepickerlibrary.imagepicker.ImageDataSource
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.MediaType
import com.haolin.android.imagepickerlibrary.imagepicker.adapter.ImageFolderAdapter
import com.haolin.android.imagepickerlibrary.imagepicker.adapter.ImageRecyclerAdapter
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageFolder
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.util.Utils
import com.haolin.android.imagepickerlibrary.imagepicker.view.FolderPopUpWindow
import com.haolin.android.imagepickerlibrary.imagepicker.view.GridSpacingItemDecoration

abstract class AbstractImageGridActivity : ImageBaseActivity(),
    ImageDataSource.OnImagesLoadedListener,
    ImageRecyclerAdapter.OnImageItemClickListener, ImagePicker.OnPictureSelectedListener, View.OnClickListener {
    lateinit var imagePicker: ImagePicker
    var isOrigin = false //是否选中原图
    var footer_bar //底部栏
            : View? = null
    var btn_ok //确定按钮
            : Button? = null
    var ll_dir //文件夹切换按钮
            : View? = null
    var tv_dir //显示当前文件夹
            : TextView? = null
    var tv_preview //预览按钮
            : TextView? = null
    var mImageFolderAdapter //图片文件夹的适配器
            : ImageFolderAdapter? = null
    var mFolderPopupWindow //ImageSet的PopupWindow
            : FolderPopUpWindow? = null
    private var mImageFolders //所有的图片文件夹
            : MutableList<ImageFolder>? = null
    var directPhoto = false // 默认不是直接调取相机
    var rc_view: RecyclerView? = null
    var mRecyclerAdapter: ImageRecyclerAdapter? = null
    var iv_back: View? = null
    var tv_title: TextView? = null
    protected abstract fun attachRecyclerViewRes(): Int
    protected abstract fun attachButtonBackRes(): Int
    protected abstract fun attachButtonOkRes(): Int
    protected abstract fun attachButtonPreviewRes(): Int
    protected abstract fun attachFooterBarRes(): Int
    protected abstract fun attachDirectoryRes(): Int
    protected abstract fun attachDirectoryNameRes(): Int
    protected abstract fun attachTitleRes(): Int
    protected abstract fun attachPreviewActivityClass(): Class<*>?
    protected abstract fun attachCropActivityClass(): Class<*>?
    protected fun attachDirectoryName(isImage: Boolean): Int {
        return if(isImage) R.string.ip_all_images else R.string.all_video
    }

    protected fun attachTitleName(isImage: Boolean): Int {
        return if(isImage) R.string.image else R.string.video
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePicker = ImagePicker.instance
        imagePicker.clear()
        imagePicker.addOnPictureSelectedListener(this)
        detectPhoto()
        initView()
        initEvent()
        initRecycler()
    }

    override fun attachNavigationEmbed(): Boolean {
        return false
    }

    override fun attachStatusEmbed(): Boolean {
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSaveInstanceState: ")
        outState.putBoolean(EXTRAS_TAKE_PICKERS, directPhoto)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i(TAG, "onRestoreInstanceState: ")
        directPhoto = savedInstanceState.getBoolean(EXTRAS_TAKE_PICKERS, false)
    }

    private fun initRecycler() {
        Log.i(TAG, "initRecycler: ")
        mImageFolderAdapter = ImageFolderAdapter(this, null)
        mRecyclerAdapter = ImageRecyclerAdapter(this, MediaType.IMAGE)
        mRecyclerAdapter!!.setOnImageItemClickListener(this)
        rc_view!!.layoutManager = GridLayoutManager(this, 3)
        rc_view!!.addItemDecoration(GridSpacingItemDecoration(3, Utils.dip2px(this, 2f), false))
        rc_view!!.adapter = mRecyclerAdapter
        onImageSelected(0, null, false)
        if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ImageDataSource(this, null, imagePicker.loadType, this)
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_STORAGE)
        }
    }

    private fun detectPhoto() {
        val data = intent
        if(data != null && data.extras != null) {
            directPhoto = data.getBooleanExtra(EXTRAS_TAKE_PICKERS, false) // 默认不是直接打开相机
            if(directPhoto) {
                if(!checkPermission(Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_PERMISSION_CAMERA)
                } else {
                    imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE)
                }
            }
            val images: ArrayList<ImageItem>? = data.getParcelableArrayListExtra(EXTRAS_IMAGES)
            imagePicker.selectedImages(images)
        }
    }

    private fun initEvent() {
        iv_back!!.setOnClickListener(this)
        btn_ok!!.setOnClickListener(this)
        tv_preview!!.setOnClickListener(this)
        ll_dir!!.setOnClickListener(this)
    }

    private fun initView() {
        rc_view = findViewById(attachRecyclerViewRes())
        btn_ok = findViewById(attachButtonOkRes())
        tv_preview = findViewById(attachButtonPreviewRes())
        footer_bar = findViewById(attachFooterBarRes())
        ll_dir = findViewById(attachDirectoryRes())
        tv_dir = findViewById(attachDirectoryNameRes())
        iv_back = findViewById(attachButtonBackRes())
        tv_title = findViewById(attachTitleRes())
        if(imagePicker.isMultiMode) {
            btn_ok!!.visibility = View.VISIBLE
            tv_preview!!.visibility = View.VISIBLE
        } else {
            btn_ok!!.visibility = View.GONE
            tv_preview!!.visibility = View.GONE
        }
        tv_dir!!.setText(attachDirectoryName(imagePicker.loadType === MediaType.IMAGE))
        tv_title!!.setText(attachTitleName(imagePicker.loadType === MediaType.IMAGE))
    }

    override fun onDestroy() {
        imagePicker.removeOnPictureSelectedListener(this)
        imagePicker.selectedListener(null)
        super.onDestroy()
    }

    override fun onClick(v: View) {
        val id = v.id
        if(id == attachButtonOkRes()) {
            val intent = Intent()
            intent.putParcelableArrayListExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                imagePicker.selectedImages)
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //多选不允许裁剪裁剪，返回数据
            finish()
        } else if(id == attachDirectoryRes()) {
            if(mImageFolders == null || mImageFolders!!.isEmpty()) {
                Log.i("ImageGridActivity", "您的手机没有图片")
                Log.i(TAG,
                    "onClick: mImageFolders.size" + if(mImageFolders == null) "null" else mImageFolders!!.size)
                Log.i(TAG,
                    "onClick: mAdapter.size" + if(mRecyclerAdapter == null) "null" else mRecyclerAdapter!!.itemCount)
                return
            }
            //点击文件夹按钮
            createPopupFolderList()
            mImageFolderAdapter!!.refreshData(mImageFolders) //刷新数据
            if(mFolderPopupWindow!!.isShowing) {
                mFolderPopupWindow!!.dismiss()
            } else {
                mFolderPopupWindow!!.showAtLocation(footer_bar, Gravity.NO_GRAVITY, 0, 0)
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                var index = mImageFolderAdapter!!.selectIndex
                index = if(index == 0) index else index - 1
                mFolderPopupWindow!!.setSelection(index)
            }
        } else if(id == attachButtonPreviewRes()) {
            val intent = Intent(this, attachPreviewActivityClass())
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0)
            intent.putParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_ITEMS,
                imagePicker.selectedImages)
            intent.putExtra(AbstractImagePreviewActivity.ISORIGIN, isOrigin)
            intent.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true)
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW)
        } else if(id == attachButtonBackRes()) {
            finish()
        }
    }

    private fun createPopupFolderList() {
        mFolderPopupWindow = FolderPopUpWindow(this, mImageFolderAdapter)
        mFolderPopupWindow!!.setOnItemClickListener { adapterView, view, position, l ->
            mImageFolderAdapter!!.selectIndex = position
            imagePicker.currentImageFolderPosition(position)
            mFolderPopupWindow!!.dismiss()
            val imageFolder = adapterView.adapter.getItem(position) as ImageFolder
            if(null != imageFolder) {
                mRecyclerAdapter!!.bindData(imageFolder.images)
                tv_dir!!.text = imageFolder.name
            }
        }
        mFolderPopupWindow!!.setMargin(footer_bar!!.height)
    }

    override fun onImagesLoaded(imageFolders: MutableList<ImageFolder>?) {
        mImageFolders = imageFolders
        imagePicker.imageFolders(imageFolders)
        if(imageFolders!!.size == 0) {
            mRecyclerAdapter!!.clearData()
        } else {
            mRecyclerAdapter!!.bindData(imageFolders[0].images)
        }
        rc_view!!.layoutManager = GridLayoutManager(this, 3)
        rc_view!!.addItemDecoration(GridSpacingItemDecoration(3, Utils.dip2px(this, 2f), false))
        rc_view!!.adapter = mRecyclerAdapter
        mImageFolderAdapter!!.refreshData(imageFolders)
    }

    override fun onImageItemClick(view: View?, imageItem: ImageItem?, position: Int) {
        //根据是否有相机按钮确定位置
        var position = position
        position = if(imagePicker.isShowCamera) position - 1 else position
        if(imagePicker.isMultiMode) {
            val intent = Intent(this, attachPreviewActivityClass())
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position)
            DataHolder.instance.save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS,
                imagePicker.currentImageFolderItems)
            intent.putExtra(AbstractImagePreviewActivity.ISORIGIN, isOrigin)
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW) //如果是多选，点击图片进入预览界面
        } else {
            imagePicker.clearSelectedImages()
            imagePicker.addSelectedImageItem(position,
                imagePicker.currentImageFolderItems[position],
                true)
            if(imagePicker.isCrop) {
                val intent = Intent(this, attachCropActivityClass())
                startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP) //单选需要裁剪，进入裁剪界面
            } else {
                val intent = Intent()
                intent.putParcelableArrayListExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                    imagePicker.selectedImages)
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //单选不需要裁剪，返回数据
                finish()
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    override fun onImageSelected(position: Int, item: ImageItem?, isAdd: Boolean) {
        if(imagePicker.selectImageCount > 0) {
            btn_ok!!.text = getString(R.string.ip_select_complete,
                imagePicker.selectImageCount,
                imagePicker.selectLimit)
            btn_ok!!.isEnabled = true
            tv_preview!!.isEnabled = true
            tv_preview!!.text =
                resources.getString(R.string.ip_preview_count, imagePicker.selectImageCount)
            //            tv_preview.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted));
            btn_ok!!.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted))
        } else {
            btn_ok!!.text = getString(R.string.ip_complete)
            btn_ok!!.isEnabled = false
            tv_preview!!.isEnabled = false
            tv_preview!!.text = resources.getString(R.string.ip_preview)
            //            tv_preview.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted));
            btn_ok!!.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted))
        }
        for(i in (if(imagePicker.isShowCamera) 1 else 0) until mRecyclerAdapter!!.itemCount) {
            if(mRecyclerAdapter!!.getItem(i)!!.path != null && mRecyclerAdapter!!.getItem(i)!!.path.equals(
                    item!!.path)
            ) {
                mRecyclerAdapter!!.notifyItemChanged(i)
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data != null && data.extras != null) {
            if(resultCode == ImagePicker.RESULT_CODE_BACK) {
                isOrigin = data.getBooleanExtra(AbstractImagePreviewActivity.ISORIGIN, false)
            } else {
                //从拍照界面返回
                //点击 X , 没有选择照片
                if(data.getParcelableArrayListExtra<Parcelable>(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
                    //什么都不做 直接调起相机
                } else {
                    //说明是从裁剪页面过来的数据，直接返回就可以
                    setResult(ImagePicker.RESULT_CODE_ITEMS, data)
                }
                finish()
            }
        } else {
            //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
            if(resultCode == RESULT_OK) {
                //发送广播通知图片增加了
                ImagePicker.galleryAddPic(this, imagePicker.takeImageFile)
                val path = imagePicker.takeImageFile!!.absolutePath
                val imageItem = ImageItem()
                imageItem.path = path
                imagePicker.clearSelectedImages()
                imagePicker.addSelectedImageItem(0, imageItem, true)
                if(imagePicker.isCrop) {
                    val intent = Intent(this, attachCropActivityClass())
                    startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP) //单选需要裁剪，进入裁剪界面
                } else {
                    val intent = Intent()
                    intent.putParcelableArrayListExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                        imagePicker.selectedImages)
                    setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //单选不需要裁剪，返回数据
                    finish()
                }
            } else if(directPhoto) {
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_PERMISSION_STORAGE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: ")
                ImageDataSource(this, null, imagePicker.loadType, this)
            } else {
                showToast("权限被禁止，无法选择本地图片")
            }
        } else if(requestCode == REQUEST_PERMISSION_CAMERA) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE)
            } else {
                showToast("权限被禁止，无法打开相机")
            }
        }
    }

    companion object {
        val TAG = AbstractImageGridActivity::class.java.name
        const val REQUEST_PERMISSION_STORAGE = 0x01
        const val REQUEST_PERMISSION_CAMERA = 0x02
        const val EXTRAS_TAKE_PICKERS = "TAKE"
        const val EXTRAS_IMAGES = "IMAGES"
    }
}