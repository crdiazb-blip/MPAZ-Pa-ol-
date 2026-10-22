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

    fun onEmailChanged(email: String) {
        uiState = uiState.copy(email = email, emailError = null, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(password = password, passwordError = null, errorMessage = null)
    }

    // Devuelve true si el inicio de sesión fue exitoso.
    fun onLoginClick(): Boolean {
        val emailError = Validaciones.email(uiState.email)
        val passwordError = Validaciones.password(uiState.password)
        if (emailError != null || passwordError != null) {
            uiState = uiState.copy(emailError = emailError, passwordError = passwordError)
            return false
        }
        if (!AuthRepository.iniciarSesion(uiState.email, uiState.password)) {
            uiState = uiState.copy(errorMessage = "Correo o contraseña incorrectos.")
            return false
        }
        return true
    }
}
