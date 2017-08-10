package com.github.kraii.bookplayer

import android.Manifest.permission.*
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.util.Log

fun verifyPermissions(activity: Activity) {
    verifyPermission(activity, READ_EXTERNAL_STORAGE)
    verifyPermission(activity, WRITE_EXTERNAL_STORAGE)
    verifyPermission(activity, INTERNET)
}

private fun verifyPermission(activity: Activity, permissionRequired: String) {
    val permission = checkSelfPermission(activity, permissionRequired)
    Log.i("Permissions check", "$permissionRequired - $permission")
    if (permission != PackageManager.PERMISSION_GRANTED) {
        // We don't have permission so prompt the user
        ActivityCompat.requestPermissions(
                activity,
                arrayOf(permissionRequired),
                1 // ?
        )
    }
}
