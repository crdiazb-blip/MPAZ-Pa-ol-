package com.example.mpazpaolr.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.remote.ApiResult
import com.example.mpazpaolr.data.remote.dto.EquipoRequestDto
import com.example.mpazpaolr.data.repository.InventarioRepository
import com.example.mpazpaolr.data.repository.PanolRepository
import com.example.mpazpaolr.model.Categoria
import com.example.mpazpaolr.model.EquipoFormModel
import com.example.mpazpaolr.model.EstadoEquipo
import com.example.mpazpaolr.model.Validaciones
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lógica de la gestión de inventario del Administrador: listado, alta, edición, estado y baja. */
class InventarioViewModel(application: Application) : AndroidViewModel(application) {

    private val inventario = InventarioRepository.getRepository(application)
    private val catalogo = PanolRepository.getRepository(application)

    val equipos: StateFlow<List<EquipoEntity>> = inventario.equipos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var filtroCategoria by mutableStateOf<Categoria?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set
    var form by mutableStateOf(EquipoFormModel())
        private set

    fun sincronizar() {
        viewModelScope.launch {
            cargando = true
            mensaje = catalogo.sincronizarEquipos().mensajeError()
            cargando = false
        }
    }

    fun filtrar(lista: List<EquipoEntity>): List<EquipoEntity> =
        lista.filter { filtroCategoria == null || it.categoria == filtroCategoria }

    fun onCategoriaSeleccionada(categoria: Categoria?) {
        filtroCategoria = if (filtroCategoria == categoria) null else categoria
    }

    fun limpiarMensaje() {
        mensaje = null
    }

    // ---------------------------------------------------------------- Formulario

    fun nuevoEquipo() {
        form = EquipoFormModel()
    }

    fun editarEquipo(equipo: EquipoEntity) {
        form = EquipoFormModel(
            editandoId = equipo.id,
            nombre = equipo.nombre,
            marca = equipo.marca,
            modelo = equipo.modelo,
            descripcion = equipo.descripcion.orEmpty(),
            categoria = equipo.categoria.label,
            stockTotal = equipo.stockTotal.toString(),
            ubicacion = equipo.ubicacion,
            estado = equipo.estado.label
        )
    }

    fun onNombreChanged(v: String) { form = form.copy(nombre = v, nombreError = null, errorMessage = null) }
    fun onMarcaChanged(v: String) { form = form.copy(marca = v, marcaError = null, errorMessage = null) }
    fun onModeloChanged(v: String) { form = form.copy(modelo = v, modeloError = null, errorMessage = null) }
    fun onDescripcionChanged(v: String) { form = form.copy(descripcion = v, errorMessage = null) }
    fun onCategoriaChanged(v: String) { form = form.copy(categoria = v, categoriaError = null, errorMessage = null) }
    fun onStockChanged(v: String) { form = form.copy(stockTotal = v.filter { it.isDigit() || it == '-' }, stockError = null, errorMessage = null) }
    fun onUbicacionChanged(v: String) { form = form.copy(ubicacion = v, ubicacionError = null, errorMessage = null) }
    fun onEstadoChanged(v: String) { form = form.copy(estado = v, errorMessage = null) }

    /** Valida cada campo; si todo es correcto envía el equipo al servidor. */
    fun guardar(onExito: () -> Unit, onError: () -> Unit) {
        val f = form
        if (f.guardando) return
        val nombreError = Validaciones.textoObligatorio(f.nombre, "el nombre del equipo")
        val marcaError = Validaciones.textoObligatorio(f.marca, "la marca", 80)
        val modeloError = Validaciones.textoObligatorio(f.modelo, "el modelo", 80)
        val categoriaError = Validaciones.categoria(f.categoria)
        val stockError = Validaciones.stock(f.stockTotal)
        val ubicacionError = Validaciones.textoObligatorio(f.ubicacion, "la ubicación")
        if (listOf(nombreError, marcaError, modeloError, categoriaError, stockError, ubicacionError).any { it != null }) {
            form = f.copy(
                nombreError = nombreError, marcaError = marcaError, modeloError = modeloError,
                categoriaError = categoriaError, stockError = stockError, ubicacionError = ubicacionError
            )
            onError()
            return
        }

        val categoria = Categoria.entries.first { it.label == f.categoria }
        val estado = EstadoEquipo.entries.firstOrNull { it.label == f.estado } ?: EstadoEquipo.DISPONIBLE
        val peticion = EquipoRequestDto(
            nombre = f.nombre.trim(),
            marca = f.marca.trim(),
            modelo = f.modelo.trim(),
            descripcion = f.descripcion.trim().ifBlank { null },
            categoria = categoria.name,
            stockTotal = f.stockTotal.trim().toInt(),
            estado = estado.name,
            ubicacion = f.ubicacion.trim()
        )

        form = f.copy(guardando = true, errorMessage = null)
        viewModelScope.launch {
            val resultado = if (f.editandoId == null) inventario.crearEquipo(peticion)
            else inventario.actualizarEquipo(f.editandoId, peticion)
            if (resultado is ApiResult.Ok) {
                form = EquipoFormModel()
                onExito()
            } else {
                form = form.copy(guardando = false, errorMessage = resultado.mensajeError())
                onError()
            }
        }
    }

    // ---------------------------------------------------------------- Acciones sobre la lista

    fun cambiarEstado(equipo: EquipoEntity, estado: EstadoEquipo) {
        viewModelScope.launch {
            mensaje = inventario.cambiarEstado(equipo.id, estado).mensajeError()
                ?: "${equipo.nombre}: ${estado.label}"
        }
    }

    fun darDeBaja(equipo: EquipoEntity) {
        viewModelScope.launch {
            mensaje = inventario.darDeBaja(equipo.id).mensajeError() ?: "${equipo.nombre} fue dado de baja"
        }
    }
}
