package com.example.mpazpaolr.ui.screen

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.data.remote.dto.RegistroBitacoraDto
import com.example.mpazpaolr.model.EstadoSolicitud
import com.example.mpazpaolr.model.FechaIso
import com.example.mpazpaolr.model.FormatoFecha
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.GestionSolicitudesViewModel

/** Acción del administrador que espera una confirmación explícita. */
private sealed class Accion(val solicitudId: Int) {
    class Aceptar(id: Int) : Accion(id)
    class Rechazar(id: Int) : Accion(id)
    class Entregar(id: Int) : Accion(id)
    class Devolver(id: Int) : Accion(id)
    class Finalizar(id: Int) : Accion(id)
}

@Composable
fun AdminSolicitudes(viewModel: GestionSolicitudesViewModel, onVolver: () -> Unit) {
    val context = LocalContext.current
    val solicitudes by viewModel.solicitudes.collectAsState()
    val visibles = viewModel.filtrar(solicitudes)
    var accion by remember { mutableStateOf<Accion?>(null) }

    LaunchedEffect(Unit) { viewModel.sincronizar() }
    LaunchedEffect(viewModel.mensaje) {
        viewModel.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = { Header("SOLICITUDES", onBack = onVolver) },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (viewModel.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                "Pendientes de revisión: ${viewModel.pendientes(solicitudes)}",
                style = MaterialTheme.typography.titleMedium
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(EstadoSolicitud.entries) { estado ->
                    FilterChip(
                        selected = viewModel.filtroEstado == estado,
                        onClick = { viewModel.onEstadoSeleccionado(estado) },
                        label = { Text(estado.label) }
                    )
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibles, key = { it.solicitud.id }) { datos ->
                    TarjetaGestion(
                        datos = datos,
                        bitacora = viewModel.bitacora[datos.solicitud.id].orEmpty(),
                        onExpandir = { viewModel.cargarBitacora(datos.solicitud.id) },
                        onAccion = { accion = it }
                    )
                }
                if (visibles.isEmpty() && !viewModel.cargando) {
                    item { Text("No hay solicitudes con ese estado.") }
                }
            }
        }
    }

    when (val a = accion) {
        null -> Unit
        is Accion.Rechazar -> DialogoRechazo(
            solicitudId = a.solicitudId,
            error = viewModel.rechazoError,
            onConfirmar = { comentario ->
                viewModel.rechazar(a.solicitudId, comentario) { accion = null }
            },
            onCancelar = {
                viewModel.limpiarRechazoError()
                accion = null
            }
        )
        else -> {
            val (titulo, texto) = textoConfirmacion(a)
            AlertDialog(
                onDismissRequest = { accion = null },
                title = { Text(titulo) },
                text = { Text(texto) },
                confirmButton = {
                    TextButton(onClick = {
                        when (a) {
                            is Accion.Aceptar -> viewModel.aceptar(a.solicitudId)
                            is Accion.Entregar -> viewModel.entregar(a.solicitudId)
                            is Accion.Devolver -> viewModel.devolver(a.solicitudId)
                            is Accion.Finalizar -> viewModel.finalizar(a.solicitudId)
                            is Accion.Rechazar -> Unit
                        }
                        accion = null
                    }) { Text("Confirmar") }
                },
                dismissButton = { TextButton(onClick = { accion = null }) { Text("Cancelar") } }
            )
        }
    }
}

private fun textoConfirmacion(accion: Accion): Pair<String, String> = when (accion) {
    is Accion.Aceptar -> "Aceptar solicitud" to
        "Se descontará del stock disponible lo solicitado en la solicitud #${accion.solicitudId}."
    is Accion.Entregar -> "Registrar entrega" to
        "Se dejará constancia de la fecha y hora en que se entregan los equipos de la solicitud #${accion.solicitudId}."
    is Accion.Devolver -> "Registrar devolución" to
        "Se registrará la recepción conforme de la solicitud #${accion.solicitudId} y los equipos volverán al stock."
    is Accion.Finalizar -> "Finalizar solicitud" to "La solicitud #${accion.solicitudId} quedará cerrada."
    is Accion.Rechazar -> "Rechazar solicitud" to ""
}

@Composable
private fun DialogoRechazo(
    solicitudId: Int,
    error: String?,
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var comentario by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Rechazar solicitud #$solicitudId") },
        text = {
            Column {
                Text("Escribe el motivo del rechazo. El solicitante lo verá en su app.")
                OutlinedTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = { Text("Justificación (obligatoria)") },
                    isError = error != null,
                    supportingText = { if (error != null) Text(error) },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirmar(comentario) }) { Text("Rechazar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun TarjetaGestion(
    datos: SolicitudConItems,
    bitacora: List<RegistroBitacoraDto>,
    onExpandir: () -> Unit,
    onAccion: (Accion) -> Unit
) {
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
        label = "colorEstadoGestion"
    )

    // La bitácora se vuelve a pedir al expandir y cada vez que cambia el estado de la solicitud.
    LaunchedEffect(expandida, solicitud.estado) {
        if (expandida) onExpandir()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = colorEstado)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.clickable { expandida = !expandida }) {
                Text("Solicitud #${solicitud.id} · ${solicitud.estado.label}", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                Text(
                    "${solicitud.solicitanteNombre}${solicitud.solicitanteCargo?.let { " ($it)" } ?: ""}",
                    style = MaterialTheme.typography.bodyMedium, color = Color.Black
                )
                Text("${solicitud.sala} · ${solicitud.fecha}", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                Text("${solicitud.horaInicio} a ${solicitud.horaDevolucion}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                datos.items.forEach { Text("• ${it.nombreEquipo} x${it.cantidad}", style = MaterialTheme.typography.bodySmall, color = Color.Black) }
                if (!solicitud.comentarioGestion.isNullOrBlank()) {
                    Text("Motivo del rechazo: ${solicitud.comentarioGestion}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                }
            }

            AnimatedVisibility(visible = expandida) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("Trazabilidad", style = MaterialTheme.typography.titleSmall, color = Color.Black)
                    Text("Creada: ${FormatoFecha.millisADisplay(solicitud.creadaEn)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    Text("Entregada: ${FormatoFecha.millisADisplay(solicitud.entregadaEn)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    Text("Devuelta: ${FormatoFecha.millisADisplay(solicitud.devueltaEn)}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
                    if (bitacora.isNotEmpty()) {
                        Text("Bitácora", style = MaterialTheme.typography.titleSmall, color = Color.Black, modifier = Modifier.padding(top = 6.dp))
                        bitacora.forEach { registro ->
                            val cuando = FormatoFecha.millisADisplay(FechaIso.aMillis(registro.fecha))
                            Text(
                                "$cuando · ${registro.hito} · ${registro.actorNombre}" +
                                    (registro.detalle?.let { " — $it" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (solicitud.estado) {
                    EstadoSolicitud.PENDIENTE -> {
                        Button(onClick = { onAccion(Accion.Aceptar(solicitud.id)) }, modifier = Modifier.weight(1f)) { Text("Aceptar") }
                        OutlinedButton(onClick = { onAccion(Accion.Rechazar(solicitud.id)) }, modifier = Modifier.weight(1f)) { Text("Rechazar") }
                    }
                    EstadoSolicitud.ACEPTADA ->
                        Button(onClick = { onAccion(Accion.Entregar(solicitud.id)) }, modifier = Modifier.fillMaxWidth()) { Text("Registrar entrega") }
                    EstadoSolicitud.ENTREGADA ->
                        Button(onClick = { onAccion(Accion.Devolver(solicitud.id)) }, modifier = Modifier.fillMaxWidth()) { Text("Registrar devolución") }
                    EstadoSolicitud.DEVUELTA ->
                        OutlinedButton(onClick = { onAccion(Accion.Finalizar(solicitud.id)) }, modifier = Modifier.fillMaxWidth()) { Text("Finalizar") }
                    EstadoSolicitud.RECHAZADA, EstadoSolicitud.FINALIZADA -> Unit
                }
            }
        }
    }
}
