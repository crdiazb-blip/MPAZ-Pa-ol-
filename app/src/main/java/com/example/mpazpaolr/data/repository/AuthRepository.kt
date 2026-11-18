package com.example.mpazpaolr.data.repository

import android.content.Context
import com.example.mpazpaolr.data.database.AppDatabase
import com.example.mpazpaolr.data.local.dao.AuthDao
import com.example.mpazpaolr.data.local.entity.SesionEntity
import com.example.mpazpaolr.data.local.entity.UsuarioLocalEntity
import com.example.mpazpaolr.data.remote.ApiResult
import com.example.mpazpaolr.data.remote.RetrofitClient
import com.example.mpazpaolr.data.remote.SesionToken
import com.example.mpazpaolr.data.remote.dto.LoginRequestDto
import com.example.mpazpaolr.data.remote.llamarApi
import com.example.mpazpaolr.data.security.PasswordHasher
import com.example.mpazpaolr.model.FechaIso
import com.example.mpazpaolr.model.Perfil
import com.example.mpazpaolr.model.Rol
import com.example.mpazpaolr.model.Validaciones

/** Resultado de intentar iniciar sesión. */
sealed class ResultadoLogin {
    data class Exito(val perfil: Perfil, val sinConexion: Boolean) : ResultadoLogin()
    data class CredencialesInvalidas(val mensaje: String) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}

/**
 * Autenticación de ambos perfiles. La sesión y los usuarios se guardan en SQLite (Room);
 * el login se valida contra el microservicio auth-service (vía api-gateway) y, si no hay red,
 * contra el hash guardado en el teléfono.
 */
class AuthRepository private constructor(private val dao: AuthDao) {

    suspend fun iniciarSesion(rut: String, clave: String): ResultadoLogin {
        val rutNormalizado = Validaciones.normalizarRut(rut)
        return when (val respuesta = llamarApi { RetrofitClient.authApi.login(LoginRequestDto(rutNormalizado, clave)) }) {
            is ApiResult.Ok -> {
                val usuario = respuesta.valor.usuario
                val sal = PasswordHasher.nuevaSal()
                val local = UsuarioLocalEntity(
                    rut = Validaciones.normalizarRut(usuario.rut),
                    nombre = usuario.nombre,
                    cargo = usuario.cargo,
                    correo = usuario.correo,
                    rol = Rol.desdeApi(usuario.rol),
                    passwordHash = PasswordHasher.hash(clave, sal),
                    sal = sal
                )
                dao.guardarUsuario(local)
                dao.guardarSesion(
                    SesionEntity(
                        rut = local.rut,
                        token = respuesta.valor.token,
                        expiraEn = FechaIso.aMillis(respuesta.valor.expiraEn) ?: (System.currentTimeMillis() + OCHO_HORAS)
                    )
                )
                SesionToken.token = respuesta.valor.token
                perfilActivo = local.aPerfil()
                ResultadoLogin.Exito(local.aPerfil(), sinConexion = false)
            }
            is ApiResult.Error ->
                if (respuesta.codigo == 401 || respuesta.codigo == 400) {
                    ResultadoLogin.CredencialesInvalidas(respuesta.mensaje)
                } else {
                    ResultadoLogin.Error(respuesta.mensaje)
                }
            is ApiResult.SinConexion -> iniciarSesionLocal(rutNormalizado, clave, respuesta.mensaje)
        }
    }

    /** Sin red: solo entra quien ya inició sesión antes en este teléfono y su clave coincide con el hash. */
    private suspend fun iniciarSesionLocal(rutNormalizado: String, clave: String, mensajeRed: String): ResultadoLogin {
        val usuario = dao.buscarUsuario(rutNormalizado)
            ?: return ResultadoLogin.Error(mensajeRed)
        if (!PasswordHasher.coincide(clave, usuario.sal, usuario.passwordHash)) {
            return ResultadoLogin.CredencialesInvalidas("RUT o contraseña incorrectos.")
        }
        dao.guardarSesion(
            SesionEntity(rut = usuario.rut, token = null, expiraEn = System.currentTimeMillis() + OCHO_HORAS)
        )
        SesionToken.token = null
        perfilActivo = usuario.aPerfil()
        return ResultadoLogin.Exito(usuario.aPerfil(), sinConexion = true)
    }

    /** Al abrir la app: recupera la sesión guardada si no ha expirado. */
    suspend fun restaurarSesion(): Perfil? {
        val sesion = dao.obtenerSesion() ?: return null
        if (sesion.expiraEn <= System.currentTimeMillis()) {
            dao.borrarSesion()
            return null
        }
        val usuario = dao.buscarUsuario(sesion.rut)
        if (usuario == null) {
            dao.borrarSesion()
            return null
        }
        SesionToken.token = sesion.token
        perfilActivo = usuario.aPerfil()
        return perfilActivo
    }

    /** Cierra la sesión: avisa al servidor (invalida el token) y borra la sesión local. */
    suspend fun cerrarSesion() {
        if (SesionToken.token != null) {
            llamarApi { RetrofitClient.authApi.logout() }
        }
        SesionToken.token = null
        perfilActivo = null
        dao.borrarSesion()
    }

    companion object {
        private const val OCHO_HORAS = 8L * 60L * 60L * 1000L

        /** Perfil de la sesión activa (lo leen las pantallas para mostrar nombre y cargo). */
        @Volatile
        var perfilActivo: Perfil? = null
            private set

        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getRepository(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                AuthRepository(AppDatabase.getDatabase(context).authDao()).also { INSTANCE = it }
            }
        }
    }
}
