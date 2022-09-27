package com.haolin.android.imagepickerlibrary.imagepicker.adapter

import android.Manifest
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.haolin.android.imagepickerlibrary.R
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker.Companion.instance
import com.haolin.android.imagepickerlibrary.imagepicker.MediaType
import com.haolin.android.imagepickerlibrary.imagepicker.PermissionRequest
import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import com.haolin.android.imagepickerlibrary.imagepicker.ui.AbstractImageGridActivity.Companion.REQUEST_PERMISSION_CAMERA
import com.haolin.android.imagepickerlibrary.imagepicker.ui.ImageBaseActivity
import com.haolin.android.imagepickerlibrary.imagepicker.util.Utils
import com.haolin.android.imagepickerlibrary.imagepicker.view.SuperCheckBox

class ImageRecyclerAdapter(activity: AppCompatActivity, loadType: MediaType,launcher: ActivityResultLauncher<Intent>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val loadType: MediaType = loadType
    private val imagePicker: ImagePicker = instance
    private val mActivity: AppCompatActivity = activity
    private var launcher: ActivityResultLauncher<Intent>? = launcher
    private val images //当前需要显示的所有的图片数据
            : ArrayList<ImageItem> = ArrayList()
    private var mSelectedImages //全局保存的已经选中的图片数据
            : ArrayList<ImageItem>? = imagePicker.selectedImages
    private val isShowCamera //是否显示拍照按钮
            : Boolean = imagePicker.isShowCamera
    private val mImageSize //每个条目的大小
            : Int = Utils.getImageItemWidth(mActivity)
    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var listener //图片被点击的监听
            : OnImageItemClickListener? = null

    fun setOnImageItemClickListener(listener: OnImageItemClickListener?) {
        this.listener = listener
    }

    fun bindData(images: ArrayList<ImageItem>?) {
        if(images == null) return
        this.images.clear()
        this.images.addAll(images)
        Log.i(TAG, "bindData: " + images.size)
        notifyDataSetChanged()
    }

    fun clearData() {
        images.clear()
        Log.i(TAG, "clearData: ")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM_TYPE_CAMERA) {
            CameraViewHolder(mInflater.inflate(R.layout.imagepicker_item_camera, parent, false))
        } else ImageViewHolder(mInflater.inflate(R.layout.imagepicker_item_image, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is CameraViewHolder) {
            holder.bindCamera()
        } else if(holder is ImageViewHolder) {
            holder.bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(isShowCamera) if(position == 0) ITEM_TYPE_CAMERA else ITEM_TYPE_NORMAL else ITEM_TYPE_NORMAL
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return if(isShowCamera) images.size + 1 else images.size
    }

    fun getItem(position: Int): ImageItem? {
        return if(isShowCamera) {
            if(position == 0) null else images[position - 1]
        } else {
            images[position]
        }
    }

    interface OnImageItemClickListener {
        fun onImageItemClick(view: View?, imageItem: ImageItem?, position: Int)
    }

    private inner class ImageViewHolder(var rootView: View) :
        RecyclerView.ViewHolder(
            rootView) {
        var ivThumb: ImageView
        var mask: View
        var checkView: View
        var cbCheck: SuperCheckBox
        var ivPlay: ImageView
        fun bind(position: Int) {
            val imageItem = getItem(position)
            ivThumb.setOnClickListener {
                if(listener != null) listener!!.onImageItemClick(rootView,
                    imageItem,
                    position)
            }
            checkView.setOnClickListener {
                cbCheck.isChecked = !cbCheck.isChecked
                val selectLimit = imagePicker.selectLimit
                if(cbCheck.isChecked && mSelectedImages!!.size >= selectLimit) {
                    Toast.makeText(mActivity.applicationContext,
                        mActivity.getString(R.string.ip_select_limit, selectLimit),
                        Toast.LENGTH_SHORT).show()
                    cbCheck.isChecked = false
                    mask.visibility = View.GONE
                } else {
                    imagePicker.addSelectedImageItem(position, imageItem!!, cbCheck.isChecked)
                    mask.visibility = View.VISIBLE
                }
            }
            //根据是否多选，显示或隐藏checkbox
            if(imagePicker.isMultiMode) {
                cbCheck.visibility = View.VISIBLE
                val checked = mSelectedImages!!.contains(imageItem)
                if(checked) {
                    mask.visibility = View.VISIBLE
                    cbCheck.isChecked = true
                } else {
                    mask.visibility = View.GONE
                    cbCheck.isChecked = false
                }
            } else {
                cbCheck.visibility = View.GONE
            }
            imagePicker.imageLoader.displayFileImage(ivThumb, imageItem!!.path) //显示图片
        }

        init {
            ivThumb = itemView.findViewById<View>(R.id.iv_thumb) as ImageView
            mask = itemView.findViewById(R.id.mask)
            checkView = itemView.findViewById(R.id.checkView)
            cbCheck = itemView.findViewById<View>(R.id.cb_check) as SuperCheckBox
            ivPlay = itemView.findViewById(R.id.iv_play)
            if(loadType === MediaType.VIDEO) ivPlay.visibility = View.VISIBLE
            itemView.layoutParams =
                AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize) //让图片是个正方形
        }
    }

    private inner class CameraViewHolder(var mItemView: View) :
        RecyclerView.ViewHolder(
            mItemView) {
        fun bindCamera() {
            mItemView.layoutParams =
                AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize) //让图片是个正方形
            mItemView.tag = null
            mItemView.setOnClickListener {
                (mActivity as ImageBaseActivity).PermissionRequest(listOf(Manifest.permission.CAMERA), permissionSuccess = {
                    imagePicker.takePicture(mActivity,launcher)
                }) {
                    Toast.makeText(mActivity, "权限被禁止，无法打开相机", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        val TAG = ImageRecyclerAdapter::class.java.name
        private const val ITEM_TYPE_CAMERA = 0 //第一个条目是相机
        private const val ITEM_TYPE_NORMAL = 1 //第一个条目不是相机
    }

}