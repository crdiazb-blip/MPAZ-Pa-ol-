package com.example.mpazpaolr.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpaolr.data.device.vibrar
import com.example.mpazpaolr.ui.components.CampoFormulario
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.model.Perfil
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.LoginViewModel

@Composable
fun Login(
    onLoginSuccess: (Perfil) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    Scaffold(
        topBar = { Header("MPAZ PAÑOL") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Reserva de equipos tecnológicos",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            CampoFormulario(
                value = state.rut,
                onValueChange = viewModel::onRutChanged,
                label = "RUT",
                icono = Icons.Default.Person,
                error = state.rutError,
                placeholder = "12.345.678-5",
                keyboardType = KeyboardType.Ascii
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampoFormulario(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                label = "Contraseña",
                icono = Icons.Default.Lock,
                error = state.passwordError,
                keyboardType = KeyboardType.Password,
                esPassword = true
            )

            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.onLoginClick(
                        onSuccess = { perfil, sinConexion ->
                            val mensaje = if (sinConexion) "Sesión sin conexión (datos locales)" else "¡Bienvenido/a!"
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                            onLoginSuccess(perfil)
                        },
                        onError = { vibrar(context) }
                    )
                },
                enabled = !state.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("  Ingresando…")
                } else {
                    Text("Ingresar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Cuentas de prueba (datos ficticios)",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "Solicitante: 12.345.678-5 / 1234",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Administrador: 11.111.111-1 / admin123",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
