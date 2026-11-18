package com.example.mpazpaolr.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpaolr.data.repository.AuthRepository
import com.example.mpazpaolr.data.repository.ResultadoLogin
import com.example.mpazpaolr.model.LoginModel
import com.example.mpazpaolr.model.Perfil
import com.example.mpazpaolr.model.Validaciones
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository.getRepository(application)

    var uiState by mutableStateOf(LoginModel())
        private set

    fun onRutChanged(rut: String) {
        uiState = uiState.copy(rut = rut, rutError = null, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(password = password, passwordError = null, errorMessage = null)
    }

    /** Valida los campos y, si son correctos, inicia sesión contra el servidor (o los datos locales). */
    fun onLoginClick(onSuccess: (Perfil, Boolean) -> Unit, onError: () -> Unit) {
        if (uiState.cargando) return
        val rutError = Validaciones.rut(uiState.rut)
        val passwordError = Validaciones.password(uiState.password)
        if (rutError != null || passwordError != null) {
            uiState = uiState.copy(rutError = rutError, passwordError = passwordError)
            onError()
            return
        }
        uiState = uiState.copy(cargando = true, errorMessage = null)
        viewModelScope.launch {
            when (val resultado = repository.iniciarSesion(uiState.rut, uiState.password)) {
                is ResultadoLogin.Exito -> {
                    uiState = LoginModel()
                    onSuccess(resultado.perfil, resultado.sinConexion)
                }
                is ResultadoLogin.CredencialesInvalidas -> {
                    uiState = uiState.copy(cargando = false, errorMessage = resultado.mensaje)
                    onError()
                }
                is ResultadoLogin.Error -> {
                    uiState = uiState.copy(cargando = false, errorMessage = resultado.mensaje)
                    onError()
                }
            }
        }
    }

    /** Recupera la sesión guardada en SQLite al abrir la app. Devuelve null si no hay sesión vigente. */
    suspend fun restaurarSesion(): Perfil? = repository.restaurarSesion()

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            repository.cerrarSesion()
            uiState = LoginModel()
            onListo()
        }
    }
}
