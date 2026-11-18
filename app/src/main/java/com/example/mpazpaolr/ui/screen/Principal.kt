package com.example.mpazpaolr.ui.screen

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mpazpaolr.data.device.cambiarLinterna
import com.example.mpazpaolr.data.device.leerBateria
import com.example.mpazpaolr.data.device.obtenerUbicacion
import com.example.mpazpaolr.data.device.tieneLinterna
import com.example.mpazpaolr.data.device.tienePermisoUbicacion
import com.example.mpazpaolr.data.device.vibrar
import androidx.compose.material.icons.filled.Place
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.repository.AuthRepository
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.PanolViewModel

@Composable
fun Principal(
    viewModel: PanolViewModel,
    onIrCarrito: () -> Unit,
    onIrSolicitudes: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val equipos by viewModel.equipos.collectAsState()
    val visibles = viewModel.filtrar(equipos)
    var bateria by remember { mutableIntStateOf(-1) }

    var linternaEncendida by remember { mutableStateOf(false) }
    var ubicacion by remember { mutableStateOf<Location?>(null) }
    var buscandoUbicacion by remember { mutableStateOf(false) }

    val buscarUbicacion = {
        buscandoUbicacion = true
        obtenerUbicacion(context) { resultado ->
            buscandoUbicacion = false
            if (resultado != null) {
                ubicacion = resultado
            } else {
                Toast.makeText(context, "No se pudo obtener la ubicación. Activa el GPS.", Toast.LENGTH_LONG).show()
            }
        }
    }
    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) buscarUbicacion()
        else Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        bateria = leerBateria(context)
        viewModel.sincronizar()
    }

    Scaffold(
        topBar = { Header("CATÁLOGO DE EQUIPOS") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val perfil = AuthRepository.perfilActivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(perfil?.nombre ?: "", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${perfil?.cargo ?: ""} · Batería del teléfono: ${if (bateria >= 0) "$bateria %" else "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = onCerrarSesion) { Text("Salir") }
            }

            if (viewModel.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            viewModel.mensajeError?.let { mensaje ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        mensaje,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::sincronizar) { Text("Reintentar") }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onIrCarrito, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Text("  Carrito (${viewModel.carrito.sumOf { it.cantidad }})")
                }
                OutlinedButton(onClick = onIrSolicitudes, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Text("  Mis solicitudes")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        if (!tieneLinterna(context)) {
                            Toast.makeText(context, "Este dispositivo no tiene linterna", Toast.LENGTH_SHORT).show()
                        } else if (cambiarLinterna(context, !linternaEncendida)) {
                            linternaEncendida = !linternaEncendida
                        } else {
                            Toast.makeText(context, "No se pudo usar la linterna", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Linterna: ${if (linternaEncendida) "ON" else "OFF"}")
                }
                OutlinedButton(
                    onClick = {
                        if (tienePermisoUbicacion(context)) buscarUbicacion() else permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Text(" Mi ubicación")
                }
            }
            if (buscandoUbicacion || ubicacion != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (buscandoUbicacion) "Buscando señal GPS…"
                        else "Lat: %.5f, Lon: %.5f".format(ubicacion!!.latitude, ubicacion!!.longitude),
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!buscandoUbicacion) {
                        TextButton(onClick = {
                            val uri = Uri.parse("geo:${ubicacion!!.latitude},${ubicacion!!.longitude}?q=${ubicacion!!.latitude},${ubicacion!!.longitude}(Mi ubicación)")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No hay app de mapas instalada", Toast.LENGTH_SHORT).show()
                            }
                        }) { Text("Ver en mapa") }
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Categoria.entries) { categoria ->
                    FilterChip(
                        selected = viewModel.filtroCategoria == categoria,
                        onClick = { viewModel.onCategoriaSeleccionada(categoria) },
                        label = { Text(categoria.label) }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = viewModel.soloDisponibles,
                    onCheckedChange = viewModel::onSoloDisponiblesChanged
                )
                Text("  Solo disponibles")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibles, key = { it.id }) { equipo ->
                    TarjetaEquipo(
                        equipo = equipo,
                        enCarrito = viewModel.cantidadEnCarrito(equipo.id),
                        onAgregar = {
                            val error = viewModel.agregarAlCarrito(equipo)
                            if (error == null) {
                                Toast.makeText(context, "${equipo.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
                            } else {
                                vibrar(context)
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
                if (visibles.isEmpty()) {
                    item {
                        Text(
                            if (equipos.isEmpty()) "Aún no hay equipos descargados. Revisa tu conexión y reintenta."
                            else "No hay equipos con esos filtros."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaEquipo(equipo: EquipoEntity, enCarrito: Int, onAgregar: () -> Unit) {
    val colorEstado by animateColorAsState(
        targetValue = if (equipo.reservable) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.errorContainer,
        label = "colorEstado"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = colorEstado)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(equipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text("${equipo.marca} ${equipo.modelo} · ${equipo.categoria.label}", style = MaterialTheme.typography.bodySmall)
                Text("Estado: ${equipo.estado.label}", style = MaterialTheme.typography.bodySmall)
                Text("Disponibles: ${equipo.stockDisponible} de ${equipo.stockTotal}", style = MaterialTheme.typography.bodySmall)
                Text("Ubicación: ${equipo.ubicacion}", style = MaterialTheme.typography.bodySmall)
                if (enCarrito > 0) {
                    Text("En tu carrito: $enCarrito", style = MaterialTheme.typography.labelMedium)
                }
            }
            Button(onClick = onAgregar, enabled = equipo.reservable) {
                Icon(Icons.Default.Add, contentDescription = "Agregar al carrito")
            }
        }
    }
}
