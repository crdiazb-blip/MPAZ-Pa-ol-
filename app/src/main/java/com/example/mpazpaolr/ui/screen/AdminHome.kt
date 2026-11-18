package com.example.mpazpaolr.ui.screen

import android.Manifest
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.mpazpaolr.data.repository.AuthRepository
import com.example.mpazpaolr.ui.components.Footer
import com.example.mpazpaolr.ui.components.Header

/**
 * Pantalla de inicio del perfil Administrador (Encargado TI).
 * Recursos nativos de este perfil: linterna (revisar equipos en bodegas oscuras) y ubicación GPS
 * (referencia para registrar dónde está guardado un equipo de red), además del nivel de batería.
 */
@Composable
fun AdminHome(
    onIrInventario: () -> Unit,
    onIrSolicitudes: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val perfil = AuthRepository.perfilActivo
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

    LaunchedEffect(Unit) { bateria = leerBateria(context) }

    Scaffold(
        topBar = { Header("PANEL ADMINISTRADOR") },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(perfil?.nombre ?: "", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${perfil?.cargo ?: ""} · Batería: ${if (bateria >= 0) "$bateria %" else "-"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = onCerrarSesion) { Text("Salir") }
            }

            Button(onClick = onIrSolicitudes, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Text("  Solicitudes de préstamo")
            }
            OutlinedButton(onClick = onIrInventario, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Build, contentDescription = null)
                Text("  Gestionar inventario")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Herramientas del dispositivo", style = MaterialTheme.typography.titleMedium)
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
                                if (tienePermisoUbicacion(context)) buscarUbicacion()
                                else permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                                    val lat = ubicacion!!.latitude
                                    val lon = ubicacion!!.longitude
                                    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(Mi ubicación)")
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No hay app de mapas instalada", Toast.LENGTH_SHORT).show()
                                    }
                                }) { Text("Ver en mapa") }
                            }
                        }
                    }
                }
            }
        }
    }
}
