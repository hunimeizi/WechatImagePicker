package com.haolin.android.imagepickerlibrary.imagepicker.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hacknife.immersive.Immersive
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.widget.Toast
import com.haolin.android.imagepickerlibrary.imagepicker.ImagePicker.Companion.instance

abstract class ImageBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Immersive.setContentView(this,
            attachLayoutRes(),
            attachImmersiveColorRes(),
            attachImmersiveColorRes(),
            attachStatusEmbed(),
            attachNavigationEmbed())
    }

    protected abstract fun attachNavigationEmbed(): Boolean
    protected abstract fun attachStatusEmbed(): Boolean

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        instance.saveInstanceState(outState)
    }

    protected abstract fun attachImmersiveColorRes(): Int
    protected abstract fun attachTopBarRes(): Int
    protected abstract fun attachImmersiveLightMode(): Boolean
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