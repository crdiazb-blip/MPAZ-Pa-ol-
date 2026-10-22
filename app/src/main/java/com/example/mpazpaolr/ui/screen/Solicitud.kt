package com.example.mpazpaolr.ui.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mpazpaolr.data.device.vibrar
import com.example.mpazpaolr.model.Validaciones
import com.example.mpazpaolr.ui.components.CampoFormulario
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.PanolViewModel
import com.example.mpazpaolr.ui.viewmodel.SolicitudViewModel

@Composable
fun Solicitud(
    panolViewModel: PanolViewModel,
    onVolver: () -> Unit,
    onSolicitudEnviada: () -> Unit,
    viewModel: SolicitudViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    Scaffold(
        topBar = { Header("NUEVA SOLICITUD", onBack = onVolver) },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Equipos solicitados: ${panolViewModel.carrito.sumOf { it.cantidad }}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            CampoFormulario(
                value = state.sala,
                onValueChange = viewModel::onSalaChanged,
                label = "Sala o espacio de uso",
                icono = Icons.Default.Place,
                error = state.salaError
            )
            CampoFormulario(
                value = state.fecha,
                onValueChange = viewModel::onFechaChanged,
                label = "Fecha de uso",
                icono = Icons.Default.DateRange,
                error = state.fechaError,
                placeholder = "dd/MM/aaaa",
                keyboardType = KeyboardType.Number
            )
            CampoFormulario(
                value = state.horaInicio,
                onValueChange = viewModel::onHoraInicioChanged,
                label = "Hora de inicio",
                icono = Icons.Default.Notifications,
                error = state.horaInicioError,
                placeholder = "HH:mm"
            )
            CampoFormulario(
                value = state.horaDevolucion,
                onValueChange = viewModel::onHoraDevolucionChanged,
                label = "Hora estimada de devolución",
                icono = Icons.Default.Notifications,
                error = state.horaDevolucionError,
                placeholder = "HH:mm"
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
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                "Regla: la reserva debe hacerse con al menos ${Validaciones.ANTICIPACION_HORAS} horas de anticipación.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.onEnviarClick(
                        carrito = panolViewModel.carrito,
                        onSuccess = {
                            panolViewModel.vaciarCarrito()
                            vibrar(context, 500L)
                            Toast.makeText(context, "Solicitud enviada al pañol", Toast.LENGTH_SHORT).show()
                            onSolicitudEnviada()
                        },
                        onError = { vibrar(context) }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar solicitud")
            }
        }
    }
}
