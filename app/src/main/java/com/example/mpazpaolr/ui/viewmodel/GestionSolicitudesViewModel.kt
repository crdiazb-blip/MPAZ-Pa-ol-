package com.example.mpazpaolr.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.data.remote.ApiResult
import com.example.mpazpaolr.data.remote.dto.RegistroBitacoraDto
import com.example.mpazpaolr.data.repository.GestionSolicitudesRepository
import com.example.mpazpaolr.model.EstadoSolicitud
import com.example.mpazpaolr.model.Validaciones
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Consola del Administrador: auditoría de solicitudes y registro del flujo logístico. */
class GestionSolicitudesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GestionSolicitudesRepository.getRepository(application)

    val solicitudes: StateFlow<List<SolicitudConItems>> = repository.solicitudes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var filtroEstado by mutableStateOf<EstadoSolicitud?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var bitacora by mutableStateOf<Map<Int, List<RegistroBitacoraDto>>>(emptyMap())
        private set
    var rechazoError by mutableStateOf<String?>(null)
        private set

    fun filtrar(lista: List<SolicitudConItems>): List<SolicitudConItems> =
        lista.filter { filtroEstado == null || it.solicitud.estado == filtroEstado }

    fun onEstadoSeleccionado(estado: EstadoSolicitud?) {
        filtroEstado = if (filtroEstado == estado) null else estado
    }

    fun pendientes(lista: List<SolicitudConItems>): Int =
        lista.count { it.solicitud.estado == EstadoSolicitud.PENDIENTE }

    fun limpiarMensaje() {
        mensaje = null
    }

    fun limpiarRechazoError() {
        rechazoError = null
    }

    fun sincronizar() {
        viewModelScope.launch {
            cargando = true
            mensaje = repository.sincronizar().mensajeError()
            cargando = false
        }
    }

    fun aceptar(id: Int) = ejecutar("Solicitud #$id aceptada") { repository.aceptar(id) }

    fun entregar(id: Int) = ejecutar("Entrega registrada") { repository.entregar(id) }

    fun devolver(id: Int) = ejecutar("Devolución registrada") { repository.devolver(id) }

    fun finalizar(id: Int) = ejecutar("Solicitud finalizada") { repository.finalizar(id) }

    /** El rechazo exige una justificación escrita; se valida antes de enviarla al servidor. */
    fun rechazar(id: Int, comentario: String, onListo: () -> Unit) {
        val error = Validaciones.justificacionRechazo(comentario)
        rechazoError = error
        if (error != null) return
        viewModelScope.launch {
            val resultado = repository.rechazar(id, comentario)
            if (resultado is ApiResult.Ok) {
                mensaje = "Solicitud #$id rechazada"
                recargarBitacora(id)
                onListo()
            } else {
                rechazoError = resultado.mensajeError()
            }
        }
    }

    fun cargarBitacora(id: Int) {
        viewModelScope.launch { recargarBitacora(id) }
    }

    private suspend fun recargarBitacora(id: Int) {
        repository.bitacora(id).valorONull()?.let { bitacora = bitacora + (id to it) }
    }

    private fun ejecutar(exito: String, accion: suspend () -> ApiResult<Unit>) {
        viewModelScope.launch {
            cargando = true
            val resultado = accion()
            mensaje = if (resultado is ApiResult.Ok) exito else resultado.mensajeError()
            cargando = false
        }
    }
}
