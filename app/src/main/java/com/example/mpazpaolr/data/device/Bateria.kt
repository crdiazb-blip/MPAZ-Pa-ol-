package com.example.mpazpaolr.data.device

import android.content.Context
import android.os.BatteryManager

fun leerBateria(context: Context): Int {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}
