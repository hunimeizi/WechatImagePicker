package com.haolin.android.imagepickerlibrary.imagepicker

import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

internal fun FragmentActivity.PermissionRequest(
    permissions: List<String>,
    permissionSuccess: () -> Unit,
    onFailed: () -> Unit,
) {
    PermissionX.init(this)
        .permissions(permissions)
        .onForwardToSettings { scope, deniedList -> //拒绝且不再询问
            scope.showRequestReasonDialog(deniedList, "请在设置中允许以下权限", "去允许", "取消")
        }.request { allGranted, _, _ ->
            if(allGranted) {
                permissionSuccess()
            } else {
                onFailed()
                //记得再次显示升级框
            }
        }
}
