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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mpazpaolr.data.device.vibrar
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.EstadoEquipo
import com.example.mpazpaolr.ui.components.CampoFormulario
import com.example.mpazpaolr.ui.components.CampoLista
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header
import com.example.mpazpaolr.ui.viewmodel.InventarioViewModel

/** Formulario validado de alta y edición de equipos (se reutiliza para ambos casos). */
@Composable
fun AdminEquipoForm(
    viewModel: InventarioViewModel,
    onVolver: () -> Unit,
    onGuardado: () -> Unit
) {
    val form = viewModel.form
    val context = LocalContext.current
    val editando = form.editandoId != null

    Scaffold(
        topBar = { Header(if (editando) "EDITAR EQUIPO" else "NUEVO EQUIPO", onBack = onVolver) },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CampoFormulario(
                value = form.nombre,
                onValueChange = viewModel::onNombreChanged,
                label = "Nombre del equipo",
                icono = Icons.Default.Create,
                error = form.nombreError
            )
            CampoFormulario(
                value = form.marca,
                onValueChange = viewModel::onMarcaChanged,
                label = "Marca",
                icono = Icons.Default.Info,
                error = form.marcaError
            )
            CampoFormulario(
                value = form.modelo,
                onValueChange = viewModel::onModeloChanged,
                label = "Modelo",
                icono = Icons.Default.Info,
                error = form.modeloError
            )
            CampoLista(
                value = form.categoria,
                onSeleccion = viewModel::onCategoriaChanged,
                opciones = Categoria.entries.map { it.label },
                label = "Categoría",
                icono = Icons.Default.Build,
                error = form.categoriaError
            )
            CampoFormulario(
                value = form.stockTotal,
                onValueChange = viewModel::onStockChanged,
                label = "Stock total",
                icono = Icons.Default.Settings,
                error = form.stockError,
                keyboardType = KeyboardType.Number
            )
            CampoFormulario(
                value = form.ubicacion,
                onValueChange = viewModel::onUbicacionChanged,
                label = "Ubicación de resguardo",
                icono = Icons.Default.Place,
                error = form.ubicacionError,
                placeholder = "Pañol - Estante A"
            )
            CampoLista(
                value = form.estado,
                onSeleccion = viewModel::onEstadoChanged,
                opciones = EstadoEquipo.entries.map { it.label },
                label = "Estado",
                icono = Icons.Default.Settings,
                error = null
            )

            AnimatedVisibility(
                visible = form.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = form.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.guardar(
                        onExito = {
                            Toast.makeText(context, if (editando) "Equipo actualizado" else "Equipo agregado", Toast.LENGTH_SHORT).show()
                            onGuardado()
                        },
                        onError = { vibrar(context) }
                    )
                },
                enabled = !form.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (form.guardando) "Guardando…" else if (editando) "Guardar cambios" else "Agregar al inventario")
            }
        }
    }
}
