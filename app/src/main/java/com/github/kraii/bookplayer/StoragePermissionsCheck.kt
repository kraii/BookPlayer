package com.github.kraii.bookplayer

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat


/**
 * Checks if the app has permission to write to device storage

 * If the app does not has permission then the user will be prompted to grant permissions

 * @param activity
 */
fun verifyStoragePermissions(activity: Activity) {
    // Check if we have write permission
    val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)

    if (permission != PackageManager.PERMISSION_GRANTED) {
        // We don't have permission so prompt the user
        ActivityCompat.requestPermissions(
                activity,
                arrayOf(READ_EXTERNAL_STORAGE),
                1 // ?
        )
    }
}