package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.SharedElementCallback
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.haolin.android.imagepickerlibrary.R
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker
import com.haolin.android.imagepickerlibrary.imagepicker.adapter.ImagePageAdapter

class ImageViewerActivity : ImageBaseActivity() {
    var viewpager: ViewPager? = null
    var mImages: List<String>? = null
    var mPosition = 0
    var mAdapter: ImagePageAdapter? = null
    var isMultiPhoto = false
    lateinit var indicator: TextView
    lateinit var back: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initViewPager()
    }

    override fun attachStatusEmbed(): Boolean {
        return true
    }

    override fun attachNavigationEmbed(): Boolean {
        return true
    }

    private fun initData() {
        mImages = ImagePicker.instance.viewerItem
        mPosition = intent.getIntExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0)
        if(mImages!!.size > 1) isMultiPhoto = true
        viewpager = findViewById(R.id.viewpager)
        indicator = findViewById(R.id.indicator)
        back = findViewById(R.id.iv_back)
        if(!isMultiPhoto) indicator.visibility = View.GONE
        back.setOnClickListener({ onBackPressed() })
    }

    override fun attachImmersiveColorRes(): Int {
        return R.color.black
    }

    override fun attachTopBarRes(): Int {
        return 0
    }

    override fun attachImmersiveLightMode(): Boolean {
        return false
    }

    override fun attachLayoutRes(): Int {
        postponeEnterTransition()
        return R.layout.imagepicker_activity_image_viewer
    }

    private fun initViewPager() {
        viewpager!!.setBackgroundResource(attachImmersiveColorRes())
        mAdapter = ImagePageAdapter(this, mImages!!, mPosition)
        ImagePicker.instance.viewerItem(null)
        mAdapter!!.setPhotoViewClickListener { view, v, v1 ->
            if(isMultiPhoto) {
                if(view!!.visibility == View.VISIBLE) {
                    indicator.visibility = View.GONE
                } else {
                    indicator.visibility = View.VISIBLE
                }
            }
        }
        viewpager!!.adapter = mAdapter
        viewpager!!.currentItem = mPosition
        viewpager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                indicator.text = getString(R.string.indicator, position + 1, mImages!!.size)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        indicator.text = getString(R.string.indicator, mPosition + 1, mImages!!.size)
    }

    @SuppressLint("NewApi")
    override fun finishAfterTransition() {
        val current = viewpager!!.currentItem
        val intent = Intent()
        intent.putExtra(ImagePicker.EXTRA_EXIT_POSITION, current)
        setResult(RESULT_OK, intent)
        if(current != mPosition) {
            val view = (viewpager!!.adapter as ImagePageAdapter?)!!.currentView
            setSharedElementCallback(view)
        }
        super.finishAfterTransition()
    }

    @TargetApi(21)
    private fun setSharedElementCallback(view: View?) {
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                names.clear()
                sharedElements.clear()
                names.add(view!!.transitionName)
                sharedElements[view.transitionName] = view
            }
        })
    }
}