package com.example.mpazpaolr.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpaolr.data.remote.ApiResult
import com.example.mpazpaolr.data.repository.PanolRepository
import com.example.mpazpaolr.model.CarritoItem
import com.example.mpazpaolr.model.SolicitudModel
import com.example.mpazpaolr.model.Validaciones
import kotlinx.coroutines.launch

class SolicitudViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PanolRepository.getRepository(application)

    var uiState by mutableStateOf(SolicitudModel())
        private set

    fun onSalaChanged(valor: String) {
        uiState = uiState.copy(sala = valor, salaError = null, errorMessage = null)
    }

    fun onFechaChanged(valor: String) {
        uiState = uiState.copy(fecha = valor, fechaError = null, errorMessage = null)
    }

    fun onHoraInicioChanged(valor: String) {
        uiState = uiState.copy(horaInicio = valor, horaInicioError = null, errorMessage = null)
    }

    fun onHoraDevolucionChanged(valor: String) {
        uiState = uiState.copy(horaDevolucion = valor, horaDevolucionError = null, errorMessage = null)
    }

    fun limpiar() {
        uiState = SolicitudModel()
    }

    fun onEnviarClick(
        carrito: List<CarritoItem>,
        ahoraMillis: Long = System.currentTimeMillis(),
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        val estado = uiState
        if (carrito.isEmpty()) {
            uiState = estado.copy(errorMessage = "El carrito está vacío.")
            onError()
            return
        }

        val salaError = Validaciones.sala(estado.sala)
        val fechaError = Validaciones.fecha(estado.fecha)
        val inicioError = Validaciones.hora(estado.horaInicio)
        var devolucionError = Validaciones.hora(estado.horaDevolucion)
        if (inicioError == null && devolucionError == null) {
            devolucionError = Validaciones.rangoHoras(estado.horaInicio, estado.horaDevolucion)
        }
        var anticipacionError: String? = null
        if (fechaError == null && inicioError == null) {
            anticipacionError = Validaciones.anticipacion(estado.fecha, estado.horaInicio, ahoraMillis)
        }

        val hayErrores = listOf(salaError, fechaError, inicioError, devolucionError, anticipacionError)
            .any { it != null }
        if (hayErrores) {
            uiState = estado.copy(
                salaError = salaError,
                fechaError = fechaError,
                horaInicioError = inicioError ?: anticipacionError,
                horaDevolucionError = devolucionError,
                errorMessage = anticipacionError
            )
            onError()
            return
        }

        if (estado.enviando) return
        uiState = estado.copy(enviando = true, errorMessage = null)
        viewModelScope.launch {
            val resultado = repository.crearSolicitud(
                estado.sala, estado.fecha, estado.horaInicio, estado.horaDevolucion, carrito
            )
            if (resultado is ApiResult.Ok) {
                uiState = SolicitudModel()
                onSuccess()
            } else {
                // El servidor también valida (3 horas, stock): se muestra su mensaje al usuario.
                uiState = uiState.copy(enviando = false, errorMessage = resultado.mensajeError())
                onError()
            }
        }
    }
}
