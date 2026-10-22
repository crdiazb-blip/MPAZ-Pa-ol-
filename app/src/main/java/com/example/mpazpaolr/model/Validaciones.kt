package com.example.mpazpaolr.model

import java.text.SimpleDateFormat
import java.util.Locale

// Reglas de validación puras (sin Android), usadas por los ViewModel.
object Validaciones {

    const val ANTICIPACION_HORAS = 3

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val FECHA = Regex("^\\d{2}/\\d{2}/\\d{4}$")
    private val HORA = Regex("^\\d{2}:\\d{2}$")

    fun email(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu correo."
        !EMAIL.matches(valor.trim()) -> "El formato del correo no es válido."
        else -> null
    }

    fun password(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu contraseña."
        valor.length < 4 -> "Debe tener al menos 4 caracteres."
        else -> null
    }

    fun sala(valor: String): String? = when {
        valor.isBlank() -> "Indica la sala o espacio de uso."
        valor.trim().length < 2 -> "El nombre de la sala es muy corto."
        else -> null
    }

    fun fecha(valor: String): String? = when {
        valor.isBlank() -> "Ingresa la fecha de uso."
        !FECHA.matches(valor.trim()) -> "Usa el formato dd/MM/aaaa."
        parsear(valor.trim(), "00:00") == null -> "La fecha no existe."
        else -> null
    }

    fun hora(valor: String): String? = when {
        valor.isBlank() -> "Ingresa la hora."
        !HORA.matches(valor.trim()) -> "Usa el formato HH:mm."
        parsear("01/01/2030", valor.trim()) == null -> "La hora no es válida."
        else -> null
    }

    // La hora de devolución debe ser posterior a la hora de inicio (formato HH:mm).
    fun rangoHoras(inicio: String, devolucion: String): String? {
        val ini = parsear("01/01/2030", inicio.trim()) ?: return null
        val fin = parsear("01/01/2030", devolucion.trim()) ?: return null
        return if (fin <= ini) "Debe ser posterior a la hora de inicio." else null
    }

    // Regla crítica: la solicitud se hace con al menos 3 horas de anticipación.
    fun anticipacion(fecha: String, horaInicio: String, ahoraMillis: Long): String? {
        val inicio = parsear(fecha.trim(), horaInicio.trim()) ?: return null
        val minimo = ahoraMillis + ANTICIPACION_HORAS * 60L * 60L * 1000L
        return if (inicio < minimo) {
            "Debes reservar con al menos $ANTICIPACION_HORAS horas de anticipación."
        } else null
    }

    fun parsear(fecha: String, hora: String): Long? {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))
        formato.isLenient = false
        return try {
            formato.parse("$fecha $hora")?.time
        } catch (e: Exception) {
            null
        }
    }
}
