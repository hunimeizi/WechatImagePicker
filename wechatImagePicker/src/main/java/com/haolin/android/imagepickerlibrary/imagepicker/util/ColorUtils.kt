package com.haolin.android.imagepickerlibrary.imagepicker.util

import android.graphics.Color
import android.text.TextUtils

/**
 * des 颜色处理工具类
 *
 * @date 2020/03/07
 * @author: zs
 */
internal object ColorUtils {
    /**
     * 解析颜色
     *
     * @param colorStr
     * @param defaultColor
     * @return
     */
    fun parseColor(colorStr: String, defaultColor: Int): Int {
        var colorStr1 = colorStr
        return if (TextUtils.isEmpty(colorStr1)) {
            defaultColor
        } else try {
            if (!colorStr1.startsWith("#")) {
                colorStr1 = "#$colorStr1"
            }
            Color.parseColor(colorStr1)
        } catch (e: Exception) {
            defaultColor
        }
    }

    fun parseColor(colorStr: String): Int {
        var colorStr1 = colorStr
        return if (TextUtils.isEmpty(colorStr1)) {
            0
        } else try {
            if (!colorStr1.startsWith("#")) {
                colorStr1 = "#$colorStr1"
            }
            Color.parseColor(colorStr1)
        } catch (e: Exception) {
            0
        }
    }


    /**
     * 设置html字体色值
     *
     * @param text
     * @param color
     * @return
     */
    fun setTextColor(text: String, color: String): String {
        return "<font color=#$color>$text</font>"
    }
}