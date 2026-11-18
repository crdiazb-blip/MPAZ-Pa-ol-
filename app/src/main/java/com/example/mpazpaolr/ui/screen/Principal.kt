package com.example.mpazpaolr.ui.screen

import android.widget.Toast
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mpazpaolr.data.device.leerBateria
import com.example.mpazpaolr.data.device.vibrar
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.repository.AuthRepository
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.PanolViewModel

/**
 * Catálogo del perfil Solicitante.
 * Recursos nativos de este perfil: vibración (confirmaciones y errores) y nivel de batería.
 */
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
