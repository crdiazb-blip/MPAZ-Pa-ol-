package com.example.mpazpaolr.data.remote

import com.example.mpazpaolr.data.remote.dto.ErrorDto
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/** Resultado de una llamada a la API: éxito, error del servidor o falta de conexión. */
sealed class ApiResult<out T> {
    data class Ok<T>(val valor: T) : ApiResult<T>()
    data class Error(val mensaje: String, val codigo: Int) : ApiResult<Nothing>()
    data class SinConexion(val mensaje: String) : ApiResult<Nothing>()

    val esOk: Boolean get() = this is Ok

    fun valorONull(): T? = (this as? Ok)?.valor

    fun mensajeError(): String? = when (this) {
        is Error -> mensaje
        is SinConexion -> mensaje
        is Ok -> null
    }
}

object ApiErrores {

    const val MENSAJE_SIN_CONEXION = "No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo."

    /** Extrae el campo "message" del cuerpo de error que devuelven los microservicios. */
    fun mensajeDe(cuerpo: String?, porDefecto: String): String {
        if (cuerpo.isNullOrBlank()) return porDefecto
        return try {
            Gson().fromJson(cuerpo, ErrorDto::class.java)?.message?.takeIf { it.isNotBlank() } ?: porDefecto
        } catch (e: Exception) {
            porDefecto
        }
    }
}

/** Ejecuta una llamada Retrofit y traduce las excepciones a un [ApiResult]. */
suspend fun <T> llamarApi(bloque: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Ok(bloque())
    } catch (e: HttpException) {
        val cuerpo = try {
            e.response()?.errorBody()?.string()
        } catch (ex: Exception) {
            null
        }
        ApiResult.Error(ApiErrores.mensajeDe(cuerpo, "Error del servidor (${e.code()})."), e.code())
    } catch (e: IOException) {
        ApiResult.SinConexion(ApiErrores.MENSAJE_SIN_CONEXION)
    }
}
