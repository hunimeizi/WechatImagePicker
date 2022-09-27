package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haolin.android.imagepickerlibrary.R
import com.haolin.android.imagepickerlibrary.imagepicker.*
import com.haolin.android.imagepickerlibrary.imagepicker.adapter.ImageFolderAdapter
import com.haolin.android.imagepickerlibrary.imagepicker.adapter.ImageRecyclerAdapter
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageFolder
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.util.Utils
import com.haolin.android.imagepickerlibrary.imagepicker.view.FolderPopUpWindow
import com.haolin.android.imagepickerlibrary.imagepicker.view.GridSpacingItemDecoration

abstract class AbstractImageGridActivity : ImageBaseActivity(),
    ImageDataSource.OnImagesLoadedListener,
    ImageRecyclerAdapter.OnImageItemClickListener, ImagePicker.OnPictureSelectedListener,
    View.OnClickListener {
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
    private var launcher: ActivityResultLauncher<Intent>? = null
    private var toCropAct: ActivityResultLauncher<Intent>? = null
    private var toPreviewAct: ActivityResultLauncher<Intent>? = null
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
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()
            ) {
                if(it.resultCode == RESULT_OK) {
                    picCorp()
                }
            }

        toCropAct =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()
            ) {
                setResult(ImagePicker.RESULT_CODE_ITEMS, it.data) //单选不需要裁剪，返回数据
                finish()
            }
        toPreviewAct =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()
            ) {
//                setResult(ImagePicker.RESULT_CODE_ITEMS, it.data) //单选不需要裁剪，返回数据
//                finish()
            }
        detectPhoto()
        initView()
        initEvent()
        initRecycler()
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
        mRecyclerAdapter = ImageRecyclerAdapter(this, MediaType.IMAGE, launcher)
        mRecyclerAdapter!!.setOnImageItemClickListener(this)
        rc_view!!.layoutManager = GridLayoutManager(this, 3)
        rc_view!!.addItemDecoration(GridSpacingItemDecoration(3, Utils.dip2px(this, 2f), false))
        rc_view!!.adapter = mRecyclerAdapter
        onImageSelected(0, null, false)
        val requestList = ArrayList<String>()
        requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestList.add(Manifest.permission.READ_MEDIA_IMAGES)
            requestList.add(Manifest.permission.READ_MEDIA_VIDEO)
        }else{
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        PermissionRequest(requestList,
            permissionSuccess = {
                ImageDataSource(this, null, imagePicker.loadType, this)
            }
        ) {
            showToast("权限被禁止，无法获取本地图片")
        }
    }

    private fun detectPhoto() {
        directPhoto = intent.getBooleanExtra(EXTRAS_TAKE_PICKERS, false) // 默认不是直接打开相机
        if(directPhoto) {
            PermissionRequest(listOf(Manifest.permission.CAMERA), permissionSuccess = {
                imagePicker.takePicture(this, launcher)
            }) {
                showToast("权限被禁止，无法打开相机")
            }
        }
        val images: ArrayList<ImageItem>? = intent.getParcelableArrayListExtra(EXTRAS_IMAGES)
        imagePicker.selectedImages(images)
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
            toPreviewAct?.launch(intent)
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
            mRecyclerAdapter!!.bindData(imageFolder.images)
            tv_dir!!.text = imageFolder.name
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
        var mPosition = position
        mPosition = if(imagePicker.isShowCamera) mPosition - 1 else mPosition
        if(imagePicker.isMultiMode) {
            val intent = Intent(this, attachPreviewActivityClass())
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, mPosition)
            DataHolder.instance.save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS,
                imagePicker.currentImageFolderItems)
            intent.putExtra(AbstractImagePreviewActivity.ISORIGIN, isOrigin)
            toPreviewAct?.launch(intent)
        } else {
            imagePicker.clearSelectedImages()
            imagePicker.addSelectedImageItem(mPosition,
                imagePicker.currentImageFolderItems[mPosition],
                true)
            if(imagePicker.isCrop) {
                toCropAct?.launch(Intent(this, attachCropActivityClass()))
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

    private fun picCorp() {
        //发送广播通知图片增加了
        ImagePicker.galleryAddPic(this, imagePicker.takeImageFile)
        val path = imagePicker.takeImageFile!!.absolutePath
        val imageItem = ImageItem()
        imageItem.path = path
        imagePicker.clearSelectedImages()
        imagePicker.addSelectedImageItem(0, imageItem, true)
        if(imagePicker.isCrop) {
            toCropAct?.launch(Intent(this, attachCropActivityClass()))
        } else {
            val intent = Intent()
            intent.putParcelableArrayListExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                imagePicker.selectedImages)
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //单选不需要裁剪，返回数据
            finish()
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