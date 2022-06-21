package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.annotation.SuppressLint
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

    override fun attachLayoutRes(): Int {
        return R.layout.imagepicker_activity_image_viewer
    }

    private fun initViewPager() {
        mAdapter = ImagePageAdapter(this, mImages!!, mPosition)
        ImagePicker.instance.viewerItem(null)
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