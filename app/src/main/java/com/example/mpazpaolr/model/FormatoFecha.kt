package com.example.mpazpaolr.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Conversión entre el formato de la app (dd/MM/yyyy) y el del servidor (yyyy-MM-dd). */
object FormatoFecha {

    /** "02/09/2026" -> "2026-09-02". Si no tiene el formato esperado, devuelve el mismo texto. */
    fun displayAIso(fecha: String): String {
        val partes = fecha.trim().split("/")
        if (partes.size != 3) return fecha.trim()
        return "${partes[2]}-${partes[1]}-${partes[0]}"
    }

    /** "2026-09-02" -> "02/09/2026". */
    fun isoADisplay(iso: String): String {
        val partes = iso.trim().split("-")
        if (partes.size != 3) return iso.trim()
        return "${partes[2]}/${partes[1]}/${partes[0]}"
    }

    /** Milisegundos -> "02/09/2026 15:30" en la hora local del teléfono. */
    fun millisADisplay(millis: Long?): String {
        if (millis == null) return "-"
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(millis))
    }
}
