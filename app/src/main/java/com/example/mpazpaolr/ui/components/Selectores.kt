package com.example.mpazpaolr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

@Composable
private fun CampoSelector(
    value: String,
    label: String,
    icono: ImageVector,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contenidoExtra: @Composable () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                } else {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            supportingText = { if (error != null) Text(error) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if (error != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (error != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledSupportingTextColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
        contenidoExtra()
    }
}

@Composable
fun CampoLista(
    value: String,
    onSeleccion: (String) -> Unit,
    opciones: List<String>,
    label: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    CampoSelector(
        value = value,
        label = label,
        icono = icono,
        error = error,
        onClick = { abierto = true },
        modifier = modifier
    ) {
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccion(opcion)
                        abierto = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    value: String,
    onSeleccion: (String) -> Unit,
    label: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    CampoSelector(
        value = value,
        label = label,
        icono = icono,
        error = error,
        onClick = { abierto = true },
        modifier = modifier
    )
    if (abierto) {
        val hoyUtc = remember { inicioDeHoyUtc() }
        val estado = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= hoyUtc
            }
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(
                    enabled = estado.selectedDateMillis != null,
                    onClick = {
                        estado.selectedDateMillis?.let { onSeleccion(formatoFechaUtc(it)) }
                        abierto = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = estado)
        }
    }
}

private fun inicioDeHoyUtc(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun formatoFechaUtc(millis: Long): String {
    val f = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    f.timeZone = TimeZone.getTimeZone("UTC")
    return f.format(millis)
}

@Composable
fun CampoHora(
    value: String,
    onSeleccion: (String) -> Unit,
    label: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    CampoSelector(
        value = value,
        label = label,
        icono = icono,
        error = error,
        onClick = { abierto = true },
        modifier = modifier
    )
    if (abierto) {
        val partes = value.split(":")
        val horaInicial = partes.getOrNull(0)?.toIntOrNull()?.takeIf { it in 0..23 }
        val minutoInicial = partes.getOrNull(1)?.toIntOrNull()?.takeIf { it in 0..59 }
        val porDefecto = remember { Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 3) } }
        var hora by remember { mutableIntStateOf(horaInicial ?: porDefecto.get(Calendar.HOUR_OF_DAY)) }
        var minuto by remember { mutableIntStateOf(minutoInicial ?: 0) }

        AlertDialog(
            onDismissRequest = { abierto = false },
            title = { Text(label) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Rueda(
                        items = (0..23).map { "%02d".format(it) },
                        inicial = hora,
                        onSeleccion = { hora = it }
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    Rueda(
                        items = (0..59).map { "%02d".format(it) },
                        inicial = minuto,
                        onSeleccion = { minuto = it }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSeleccion("%02d:%02d".format(hora, minuto))
                    abierto = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun Rueda(items: List<String>, inicial: Int, onSeleccion: (Int) -> Unit) {
    val altoItem = 48.dp
    val estado = rememberLazyListState(initialFirstVisibleItemIndex = inicial)
    var seleccionado by remember { mutableIntStateOf(inicial) }

    LaunchedEffect(estado) {
        snapshotFlow {
            val info = estado.layoutInfo
            val centro = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - centro) }?.index
        }.collect { indice ->
            if (indice != null) {
                seleccionado = indice
                onSeleccion(indice)
            }
        }
    }

    LazyColumn(
        state = estado,
        flingBehavior = rememberSnapFlingBehavior(estado),
        contentPadding = PaddingValues(vertical = altoItem),
        modifier = Modifier
            .width(88.dp)
            .height(altoItem * 3)
    ) {
        itemsIndexed(items) { indice, texto ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(altoItem),
                contentAlignment = Alignment.Center
            ) {
                val activo = indice == seleccionado
                Text(
                    texto,
                    textAlign = TextAlign.Center,
                    style = if (activo) MaterialTheme.typography.headlineMedium
                    else MaterialTheme.typography.titleMedium,
                    color = if (activo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
