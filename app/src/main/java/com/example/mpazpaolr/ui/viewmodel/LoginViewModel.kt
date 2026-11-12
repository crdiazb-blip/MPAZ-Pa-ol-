package com.example.mpazpaolr.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mpazpaolr.data.repository.AuthRepository
import com.example.mpazpaolr.model.LoginModel
import com.example.mpazpaolr.model.Validaciones

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginModel())
        private set

    fun onRutChanged(rut: String) {
        uiState = uiState.copy(rut = rut, rutError = null, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(password = password, passwordError = null, errorMessage = null)
    }

    fun onLoginClick(): Boolean {
        val rutError = Validaciones.rut(uiState.rut)
        val passwordError = Validaciones.password(uiState.password)
        if (rutError != null || passwordError != null) {
            uiState = uiState.copy(rutError = rutError, passwordError = passwordError)
            return false
        }
        if (!AuthRepository.iniciarSesion(uiState.rut, uiState.password)) {
            uiState = uiState.copy(errorMessage = "RUT o contraseña incorrectos.")
            return false
        }
        return true
    }
}
