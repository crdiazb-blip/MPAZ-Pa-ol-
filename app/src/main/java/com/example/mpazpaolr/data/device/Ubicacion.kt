package com.example.mpazpaolr.data.device

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

fun tienePermisoUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
fun obtenerUbicacion(context: Context, onResultado: (Location?) -> Unit) {
    if (!tienePermisoUbicacion(context)) {
        onResultado(null)
        return
    }
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val proveedores = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .filter { lm.isProviderEnabled(it) }
    if (proveedores.isEmpty()) {
        onResultado(null)
        return
    }

    val handler = Handler(Looper.getMainLooper())
    var entregado = false
    val listeners = mutableListOf<LocationListener>()

    fun terminar(ubicacion: Location?) {
        if (entregado) return
        entregado = true
        listeners.forEach { lm.removeUpdates(it) }
        handler.removeCallbacksAndMessages(null)
        onResultado(ubicacion)
    }

    proveedores.forEach { proveedor ->
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) = terminar(location)
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        listeners.add(listener)
        lm.requestLocationUpdates(proveedor, 0L, 0f, listener, Looper.getMainLooper())
    }

    handler.postDelayed({
        val ultima = proveedores.mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time }
        terminar(ultima)
    }, 15_000L)
}
