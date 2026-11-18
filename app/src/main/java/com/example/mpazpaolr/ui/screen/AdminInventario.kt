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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.EstadoEquipo
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.InventarioViewModel

@Composable
fun AdminInventario(
    viewModel: InventarioViewModel,
    onVolver: () -> Unit,
    onNuevo: () -> Unit,
    onEditar: (EquipoEntity) -> Unit
) {
    val context = LocalContext.current
    val equipos by viewModel.equipos.collectAsState()
    val visibles = viewModel.filtrar(equipos)
    var aDarDeBaja by remember { mutableStateOf<EquipoEntity?>(null) }

    LaunchedEffect(Unit) { viewModel.sincronizar() }
    LaunchedEffect(viewModel.mensaje) {
        viewModel.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = { Header("INVENTARIO", onBack = onVolver) },
        bottomBar = { Footer() },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Agregar equipo") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (viewModel.cargando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Categoria.entries) { categoria ->
                    FilterChip(
                        selected = viewModel.filtroCategoria == categoria,
                        onClick = { viewModel.onCategoriaSeleccionada(categoria) },
                        label = { Text(categoria.label) }
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibles, key = { it.id }) { equipo ->
                    TarjetaInventario(
                        equipo = equipo,
                        onEditar = { onEditar(equipo) },
                        onEstado = { viewModel.cambiarEstado(equipo, it) },
                        onBaja = { aDarDeBaja = equipo }
                    )
                }
                if (visibles.isEmpty() && !viewModel.cargando) {
                    item { Text("No hay equipos en el inventario con ese filtro.") }
                }
            }
        }
    }

    aDarDeBaja?.let { equipo ->
        AlertDialog(
            onDismissRequest = { aDarDeBaja = null },
            title = { Text("Dar de baja") },
            text = { Text("¿Retirar \"${equipo.nombre}\" del inventario? Dejará de aparecer en el catálogo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.darDeBaja(equipo)
                    aDarDeBaja = null
                }) { Text("Dar de baja") }
            },
            dismissButton = { TextButton(onClick = { aDarDeBaja = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaInventario(
    equipo: EquipoEntity,
    onEditar: () -> Unit,
    onEstado: (EstadoEquipo) -> Unit,
    onBaja: () -> Unit
) {
    var menuEstado by remember { mutableStateOf(false) }
    val colorEstado by animateColorAsState(
        targetValue = when (equipo.estado) {
            EstadoEquipo.DISPONIBLE -> MaterialTheme.colorScheme.primaryContainer
            EstadoEquipo.RESERVADO, EstadoEquipo.PRESTADO -> MaterialTheme.colorScheme.tertiaryContainer
            EstadoEquipo.MANTENIMIENTO, EstadoEquipo.FUERA_DE_SERVICIO -> MaterialTheme.colorScheme.errorContainer
        },
        label = "colorEstadoEquipo"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = colorEstado)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(equipo.nombre, style = MaterialTheme.typography.titleMedium)
            Text("${equipo.marca} ${equipo.modelo} · ${equipo.categoria.label}", style = MaterialTheme.typography.bodySmall)
            Text("Stock: ${equipo.stockDisponible} disponibles de ${equipo.stockTotal}", style = MaterialTheme.typography.bodySmall)
            Text("Ubicación: ${equipo.ubicacion}", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { menuEstado = true }) { Text("Estado: ${equipo.estado.label}") }
                    DropdownMenu(expanded = menuEstado, onDismissRequest = { menuEstado = false }) {
                        EstadoEquipo.entries.forEach { estado ->
                            DropdownMenuItem(
                                text = { Text(estado.label) },
                                onClick = {
                                    menuEstado = false
                                    if (estado != equipo.estado) onEstado(estado)
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar equipo") }
                IconButton(onClick = onBaja) { Icon(Icons.Default.Delete, contentDescription = "Dar de baja") }
            }
        }
    }
}
