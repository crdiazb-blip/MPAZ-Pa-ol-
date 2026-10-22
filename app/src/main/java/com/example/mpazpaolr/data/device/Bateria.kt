package com.example.mpazpaolr.data.device

import android.content.Context
import android.os.BatteryManager

// Recurso nativo 2: nivel de batería del dispositivo (no requiere permisos).
fun leerBateria(context: Context): Int {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}
