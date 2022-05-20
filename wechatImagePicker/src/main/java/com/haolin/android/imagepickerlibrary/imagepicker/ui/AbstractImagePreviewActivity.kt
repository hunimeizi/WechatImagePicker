package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.hacknife.immersive.Immersive
import com.haolin.android.imagepickerlibrary.R
import com.haolin.android.imagepickerlibrary.imagepicker.DataHolder
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.adapter.ImagePageAdapter
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.util.CollectionHelper
import com.haolin.android.imagepickerlibrary.imagepicker.util.NavigationBarChangeListener
import com.haolin.android.imagepickerlibrary.imagepicker.util.Utils
import com.haolin.android.imagepickerlibrary.imagepicker.view.SuperCheckBox
import com.haolin.android.imagepickerlibrary.imagepicker.view.ViewPagerFixed

abstract class AbstractImagePreviewActivity : ImageBaseActivity(),
    ImagePicker.OnPictureSelectedListener,
    View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    protected var imagePicker: ImagePicker? = null
    protected var mImageItems //跳转进ImagePreviewFragment的图片文件夹
            : ArrayList<ImageItem>? = null
    protected var mCurrentPosition = 0 //跳转进ImagePreviewFragment时的序号，第几个图片
    protected var tv_title //显示当前图片的位置  例如  5/31
            : TextView? = null
    protected var selectedImages //所有已经选中的图片
            : ArrayList<ImageItem>? = null
    protected var top_bar: View? = null
    protected var viewpager: ViewPagerFixed? = null
    protected var mAdapter: ImagePageAdapter? = null
    private var isOrigin //是否选中原图
            = false
    private var cb_check //是否选中当前图片的CheckBox
            : SuperCheckBox? = null
    private var cb_origin //原图
            : SuperCheckBox? = null
    private var btn_ok //确认图片的选择
            : Button? = null
    private var iv_back: ImageView? = null
    private var bottom_bar: View? = null
    private var view_bottom: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
        initEvent()
        initViewPager()
        initListener()
    }

    private fun initView() {
        top_bar = findViewById(attachTopBarRes())
        btn_ok = findViewById(attachButtonOkRes())
        iv_back = findViewById(attachButtonBackRes())
        tv_title = findViewById(attachTitleRes())
        viewpager = findViewById(attachViewPagerRes())
        bottom_bar = findViewById(attachBottomBarRes())
        cb_check = findViewById(attachCheckRes())
        cb_origin = findViewById(attachCheckOriginRes())
        view_bottom = findViewById(attachBottomViewRes())
    }

    private fun initData() {
        mCurrentPosition = intent.getIntExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0)
        isOrigin = intent.getBooleanExtra(ISORIGIN, false)
        val isFromItems = intent.getBooleanExtra(ImagePicker.EXTRA_FROM_ITEMS, false)
        mImageItems =
            if(isFromItems) intent.getParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_ITEMS) else DataHolder.instance.retrieve(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS) as java.util.ArrayList<ImageItem>
        if(mImageItems == null) {
            Log.i("TAG", "initData: null")
        } else {
            Log.i("TAG", "initData: " + mImageItems!!.size)
        }
        Log.i("TAG", "initData: isFromItems--->>$isFromItems")
        imagePicker = ImagePicker.instance
        selectedImages = imagePicker!!.selectedImages
    }

    private fun initEvent() {
        btn_ok!!.visibility = View.GONE
        iv_back!!.setOnClickListener { v: View? -> finish() }
        imagePicker!!.addOnPictureSelectedListener(this)
        btn_ok!!.visibility = View.VISIBLE
        btn_ok!!.setOnClickListener(this)
        bottom_bar!!.visibility = View.VISIBLE
        cb_origin!!.text = getString(R.string.ip_origin)
        cb_origin!!.setOnCheckedChangeListener(this)
        cb_origin!!.isChecked = isOrigin
        //初始化当前页面的状态
        tv_title!!.text =
            getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems!!.size)
        onImageSelected(0, null, false)
        val item = mImageItems!![mCurrentPosition]
        val isSelected = imagePicker!!.isSelect(item)
        cb_check!!.isChecked = isSelected
    }

    private fun initViewPager() {
        mAdapter = ImagePageAdapter(this, CollectionHelper.imageItem2String(mImageItems))
        mAdapter!!.setPhotoViewClickListener( object : ImagePageAdapter.PhotoViewClickListener {
            override fun OnPhotoTapListener(view: View?, v: Float, v1: Float) {
                onImageSingleTap()
            }
        })
        viewpager!!.adapter = mAdapter
        viewpager!!.setCurrentItem(mCurrentPosition, false)
    }

    private fun initListener() {
        viewpager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mCurrentPosition = position
                val item = mImageItems!![mCurrentPosition]
                val isSelected = imagePicker!!.isSelect(item)
                cb_check!!.isChecked = isSelected
                tv_title!!.text = getString(R.string.ip_preview_image_count,
                    mCurrentPosition + 1,
                    mImageItems!!.size)
            }
        })
        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
        cb_check!!.setOnClickListener { v: View? ->
            val imageItem = mImageItems!![mCurrentPosition]
            val selectLimit = imagePicker!!.selectLimit
            if(cb_check!!.isChecked && selectedImages!!.size >= selectLimit) {
                Toast.makeText(this@AbstractImagePreviewActivity,
                    getString(R.string.ip_select_limit, selectLimit),
                    Toast.LENGTH_SHORT).show()
                cb_check!!.isChecked = false
            } else {
                imagePicker!!.addSelectedImageItem(mCurrentPosition,
                    imageItem,
                    cb_check!!.isChecked)
            }
        }
        NavigationBarChangeListener.with(this).setListener(object :
            NavigationBarChangeListener.OnSoftInputStateChangeListener {
            override fun onNavigationBarShow(orientation: Int, height: Int) {
                view_bottom!!.visibility = View.VISIBLE
                val layoutParams = view_bottom!!.layoutParams
                if(layoutParams.height == 0) {
                    layoutParams.height =
                        Utils.getNavigationBarHeight(this@AbstractImagePreviewActivity)
                    view_bottom!!.requestLayout()
                }
            }

            override fun onNavigationBarHide(orientation: Int) {
                view_bottom!!.visibility = View.GONE
            }
        })
        NavigationBarChangeListener.with(this, NavigationBarChangeListener.ORIENTATION_HORIZONTAL)
            .setListener(object : NavigationBarChangeListener.OnSoftInputStateChangeListener {
                override fun onNavigationBarShow(orientation: Int, height: Int) {
                    top_bar!!.setPadding(0, 0, height, 0)
                    bottom_bar!!.setPadding(0, 0, height, 0)
                }

                override fun onNavigationBarHide(orientation: Int) {
                    top_bar!!.setPadding(0, 0, 0, 0)
                    bottom_bar!!.setPadding(0, 0, 0, 0)
                }
            })
    }

    protected abstract fun attachViewPagerRes(): Int
    protected abstract fun attachBottomViewRes(): Int
    protected abstract fun attachCheckOriginRes(): Int
    protected abstract fun attachCheckRes(): Int
    protected abstract fun attachTitleRes(): Int
    protected abstract fun attachBottomBarRes(): Int
    protected abstract fun attachButtonOkRes(): Int
    protected abstract fun attachButtonBackRes(): Int

    /**
     * 图片添加成功后，修改当前图片的选中数量
     * 当调用 addSelectedImageItem 或 deleteSelectedImageItem 都会触发当前回调
     */
    override fun onImageSelected(position: Int, item: ImageItem?, isAdd: Boolean) {
        if(imagePicker!!.selectImageCount > 0) {
            btn_ok!!.text = getString(R.string.ip_select_complete,
                imagePicker!!.selectImageCount,
                imagePicker!!.selectLimit)
        } else {
            btn_ok!!.text = getString(R.string.ip_complete)
        }
        if(cb_origin!!.isChecked) {
            var size: Long = 0
            for(imageItem in selectedImages!!) size += imageItem.size
            val fileSize = Formatter.formatFileSize(this, size)
            cb_origin!!.text = getString(R.string.ip_origin_size, fileSize)
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra(ISORIGIN, isOrigin)
        setResult(ImagePicker.RESULT_CODE_BACK, intent)
        finish()
        super.onBackPressed()
    }

    override fun onClick(v: View) {
        val id = v.id
        if(id == attachButtonOkRes()) {
            if(imagePicker!!.selectedImages!!.size == 0) {
                cb_check!!.isChecked = true
                val imageItem = mImageItems!![mCurrentPosition]
                imagePicker!!.addSelectedImageItem(mCurrentPosition,
                    imageItem,
                    cb_check!!.isChecked)
            }
            val intent = Intent()
            intent.putParcelableArrayListExtra(ImagePicker.EXTRA_RESULT_ITEMS,
                imagePicker!!.selectedImages)
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent)
            finish()
        } else if(id == attachButtonBackRes()) {
            val intent = Intent()
            intent.putExtra(ISORIGIN, isOrigin)
            setResult(ImagePicker.RESULT_CODE_BACK, intent)
            finish()
        }
    }

    override fun attachNavigationEmbed(): Boolean {
        return true
    }

    override fun attachStatusEmbed(): Boolean {
        return true
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val id = buttonView.id
        if(id == attachCheckOriginRes()) {
            if(isChecked) {
                var size: Long = 0
                for(item in selectedImages!!) size += item.size
                val fileSize = Formatter.formatFileSize(this, size)
                isOrigin = true
                cb_origin!!.text = getString(R.string.ip_origin_size, fileSize)
            } else {
                isOrigin = false
                cb_origin!!.text = getString(R.string.ip_origin)
            }
        }
    }

    override fun onDestroy() {
        imagePicker!!.removeOnPictureSelectedListener(this)
        super.onDestroy()
    }

    /**
     * 单击时，隐藏头和尾
     */
    fun onImageSingleTap() {
        if(top_bar!!.visibility == View.VISIBLE) {
            top_bar!!.animation =
                AnimationUtils.loadAnimation(this, R.anim.imagepicker_top_out)
            bottom_bar!!.animation =
                AnimationUtils.loadAnimation(this, R.anim.imagepicker_fade_out)
            top_bar!!.visibility = View.GONE
            bottom_bar!!.visibility = View.GONE
            Immersive.setNavigationBarColorRes(this, attachImmersiveColorRes(false))
            Immersive.setStatusBarColorRes(this, attachImmersiveColorRes(false))
        } else {
            top_bar!!.animation =
                AnimationUtils.loadAnimation(this, R.anim.imagepicker_top_in)
            bottom_bar!!.animation =
                AnimationUtils.loadAnimation(this, R.anim.imagepicker_fade_in)
            top_bar!!.visibility = View.VISIBLE
            bottom_bar!!.visibility = View.VISIBLE
            Immersive.setNavigationBarColorRes(this, attachImmersiveColorRes(true))
            Immersive.setStatusBarColorRes(this, attachImmersiveColorRes(true))
        }
    }

    protected abstract fun attachImmersiveColorRes(show: Boolean): Int

    companion object {
        const val ISORIGIN = "isOrigin"
    }
}