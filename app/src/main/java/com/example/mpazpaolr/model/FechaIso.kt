package com.example.mpazpaolr.model

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Convierte las fechas ISO-8601 en UTC que entrega el backend (2026-09-01T12:00:00.123Z) a milisegundos. */
object FechaIso {

    fun aMillis(iso: String?): Long? {
        if (iso.isNullOrBlank() || iso.length < 19) return null
        return try {
            val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            formato.timeZone = TimeZone.getTimeZone("UTC")
            formato.isLenient = false
            formato.parse(iso.substring(0, 19))?.time
        } catch (e: Exception) {
            null
        }
    }
}
