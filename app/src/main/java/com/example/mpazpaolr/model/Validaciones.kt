package com.example.mpazpaolr.model

import java.text.SimpleDateFormat
import java.util.Locale

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

    fun normalizarRut(valor: String): String =
        valor.filter { it.isDigit() || it == 'k' || it == 'K' }.uppercase()

    fun rut(valor: String): String? {
        val limpio = normalizarRut(valor)
        if (valor.isBlank()) return "Ingresa tu RUT."
        if (limpio.length < 2) return "El RUT es muy corto."
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() } || cuerpo.length > 8) return "El formato del RUT no es válido."
        var suma = 0
        var factor = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resto = 11 - suma % 11
        val esperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> '0' + resto
        }
        return if (dv == esperado) null else "El RUT no es válido (dígito verificador)."
    }

    fun password(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu contraseña."
        valor.length < 4 -> "Debe tener al menos 4 caracteres."
        else -> null
    }

    fun textoObligatorio(valor: String, campo: String, maximo: Int = 120): String? = when {
        valor.isBlank() -> "Ingresa $campo."
        valor.trim().length > maximo -> "Máximo $maximo caracteres."
        else -> null
    }

    const val STOCK_MAXIMO = 9999

    fun stock(valor: String): String? {
        if (valor.isBlank()) return "Ingresa el stock total."
        val numero = valor.trim().toIntOrNull() ?: return "El stock debe ser un número entero."
        return when {
            numero < 0 -> "El stock no puede ser negativo."
            numero > STOCK_MAXIMO -> "El stock máximo es $STOCK_MAXIMO."
            else -> null
        }
    }

    fun categoria(valor: String): String? =
        if (Categoria.entries.any { it.label == valor }) null else "Selecciona una categoría."

    fun justificacionRechazo(valor: String): String? = when {
        valor.isBlank() -> "La justificación del rechazo es obligatoria."
        valor.trim().length < 5 -> "Explica el motivo con al menos 5 caracteres."
        valor.trim().length > 500 -> "Máximo 500 caracteres."
        else -> null
    }

    fun sala(valor: String): String? = when {
        valor.isBlank() -> "Selecciona la sala o espacio de uso."
        valor !in SALAS_DISPONIBLES -> "Selecciona una sala de la lista."
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

    fun rangoHoras(inicio: String, devolucion: String): String? {
        val ini = parsear("01/01/2030", inicio.trim()) ?: return null
        val fin = parsear("01/01/2030", devolucion.trim()) ?: return null
        return if (fin <= ini) "Debe ser posterior a la hora de inicio." else null
    }

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
