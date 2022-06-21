package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.view.Window
import android.widget.Toast
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker.Companion.instance
import com.haolin.android.imagepickerlibrary.imagepicker.util.ColorUtils
import com.haolin.android.imagepickerlibrary.imagepicker.util.StatusUtils

abstract class ImageBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setStatusColor()
        setSystemInvadeBlack()
        setContentView(attachLayoutRes())
    }

    /**
     * 设置状态栏背景颜色
     */
    open fun setStatusColor() {
        StatusUtils.setUseStatusBarColor(this, ColorUtils.parseColor("#393A3F"))
    }
    /**
     * 沉浸式状态
     */
    open fun setSystemInvadeBlack() {
        //第二个参数是是否沉浸,第三个参数是状态栏字体是否为黑色。
        StatusUtils.setSystemStatus(this, isTransparent = true, isBlack = false)
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        instance.saveInstanceState(outState)
    }

    protected abstract fun attachLayoutRes(): Int
    fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this,
            permission) == PackageManager.PERMISSION_GRANTED
    }

    fun showToast(toastText: String?) {
        Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT).show()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        instance.restoreInstanceState(savedInstanceState)
    }
}