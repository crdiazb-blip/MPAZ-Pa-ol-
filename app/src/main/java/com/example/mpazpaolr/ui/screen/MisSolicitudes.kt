package com.example.mpazpaolr.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.model.EstadoSolicitud
import com.example.mpazpaolr.model.FormatoFecha
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.PanolViewModel

@Composable
fun MisSolicitudes(viewModel: PanolViewModel, onVolver: () -> Unit) {
    val solicitudes by viewModel.solicitudes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sincronizar()
        viewModel.cargarNotificaciones()
    }

    Scaffold(
        topBar = { Header("MIS SOLICITUDES", onBack = onVolver) },
        bottomBar = { Footer() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (viewModel.cargando) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            viewModel.mensajeError?.let { mensaje ->
                item { Text(mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
            val sinLeer = viewModel.notificaciones.filter { !it.leida }
            if (sinLeer.isNotEmpty()) {
                item { Text("Notificaciones (${sinLeer.size})", style = MaterialTheme.typography.titleSmall) }
                items(sinLeer, key = { "n${it.id}" }) { notificacion ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(notificacion.titulo, style = MaterialTheme.typography.titleSmall)
                                Text(notificacion.mensaje, style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { viewModel.marcarNotificacionLeida(notificacion.id) }) { Text("Leída") }
                        }
                    }
                }
            }
            if (solicitudes.isEmpty() && !viewModel.cargando) {
                item { Text("Aún no has enviado solicitudes.") }
            }
            items(solicitudes, key = { it.solicitud.id }) { TarjetaSolicitud(it) }
        }
    }
}

@Composable
private fun TarjetaSolicitud(datos: SolicitudConItems) {
    val solicitud = datos.solicitud
    var expandida by remember { mutableStateOf(false) }
    val colorEstado by animateColorAsState(
        targetValue = when (solicitud.estado) {
            EstadoSolicitud.PENDIENTE -> Color(0xFFFFF59D)
            EstadoSolicitud.ACEPTADA -> Color(0xFFC8E6C9)
            EstadoSolicitud.RECHAZADA -> Color(0xFFFFCDD2)
            EstadoSolicitud.ENTREGADA -> Color(0xFFBBDEFB)
            EstadoSolicitud.DEVUELTA, EstadoSolicitud.FINALIZADA -> Color(0xFFE0E0E0)
        },
        label = "colorEstadoSolicitud"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expandida = !expandida },
        colors = CardDefaults.cardColors(containerColor = colorEstado)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Solicitud #${solicitud.id} · ${solicitud.estado.label}", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            Text("${solicitud.sala} · ${solicitud.fecha}", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
            Text("${solicitud.horaInicio} a ${solicitud.horaDevolucion}", style = MaterialTheme.typography.bodySmall, color = Color.Black)

            AnimatedVisibility(visible = expandida) {
                Column {
                    datos.items.forEach {
                        Text("• ${it.nombreEquipo} x${it.cantidad}", color = Color.Black)
                    }
                    if (!solicitud.comentarioGestion.isNullOrBlank()) {
                        Text("Motivo del pañol: ${solicitud.comentarioGestion}", color = Color.Black)
                    }
                    Text("Creada: ${FormatoFecha.millisADisplay(solicitud.creadaEn)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    if (solicitud.entregadaEn != null) {
                        Text("Entregada: ${FormatoFecha.millisADisplay(solicitud.entregadaEn)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    }
                    if (solicitud.devueltaEn != null) {
                        Text("Devuelta: ${FormatoFecha.millisADisplay(solicitud.devueltaEn)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    }
                }
            }
            if (!expandida) {
                Text("Toca para ver el detalle", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
            }
        }
    }
}
