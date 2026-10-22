package com.example.mpazpaolr.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.PanolViewModel

@Composable
fun Carrito(
    viewModel: PanolViewModel,
    onVolver: () -> Unit,
    onContinuar: () -> Unit
) {
    val carrito = viewModel.carrito

    Scaffold(
        topBar = { Header("MI CARRITO", onBack = onVolver) },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (carrito.isEmpty()) {
                Text("Tu carrito está vacío. Agrega equipos desde el catálogo.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(carrito, key = { it.equipoId }) { item ->
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.nombre, style = MaterialTheme.typography.titleMedium)
                                    Text("Máximo disponible: ${item.maximo}", style = MaterialTheme.typography.bodySmall)
                                }
                                OutlinedButton(onClick = { viewModel.cambiarCantidad(item.equipoId, -1) }) { Text("-") }
                                Text("  ${item.cantidad}  ", style = MaterialTheme.typography.titleMedium)
                                OutlinedButton(onClick = { viewModel.cambiarCantidad(item.equipoId, 1) }) { Text("+") }
                                IconButton(onClick = { viewModel.quitarDelCarrito(item.equipoId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Quitar")
                                }
                            }
                        }
                    }
                }
                Button(onClick = onContinuar, modifier = Modifier.fillMaxWidth()) {
                    Text("Continuar con la solicitud")
                }
                OutlinedButton(onClick = viewModel::vaciarCarrito, modifier = Modifier.fillMaxWidth()) {
                    Text("Vaciar carrito")
                }
            }
        }
    }
}
