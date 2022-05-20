package com.haolin.android.imagepickerlibrary.imagepicker

import com.haolin.android.imagepickerlibrary.imagepicker.bean.ImageItem
import java.lang.RuntimeException
import java.util.HashMap

class DataHolder private constructor() {
    private val data: MutableMap<String, List<ImageItem>>?
    fun save(id: String, `object`: List<ImageItem>) {
        if(data != null) {
            data[id] = `object`
        }
    }

    fun retrieve(id: String): Any {
        if(data == null || mInstance == null) {
            throw RuntimeException("你必须先初始化")
        }
        return data[id]!!
    }

    companion object {
        const val DH_CURRENT_IMAGE_FOLDER_ITEMS = "dh_current_image_folder_items"
        private var mInstance: DataHolder? = null
        val instance: DataHolder
            get() {
                return mInstance ?: synchronized(DataHolder::class.java) {
                    mInstance ?: DataHolder()
                }.also {
                    mInstance = it
                }
            }
    }

    init {
        data = HashMap()
    }
}