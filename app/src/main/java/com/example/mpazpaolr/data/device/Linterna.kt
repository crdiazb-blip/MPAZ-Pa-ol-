package com.example.mpazpaolr.data.device

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

fun tieneLinterna(context: Context): Boolean =
    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

fun cambiarLinterna(context: Context, encender: Boolean): Boolean {
    return try {
        val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val id = cm.cameraIdList.firstOrNull {
            cm.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return false
        cm.setTorchMode(id, encender)
        true
    } catch (e: Exception) {
        false
    }
}
