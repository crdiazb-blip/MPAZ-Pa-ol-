package com.example.mpazpaolr.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mpazpaolr.data.local.entity.EquipoEntity
import com.example.mpazpaolr.data.local.entity.SolicitudConItems
import com.example.mpazpaolr.data.repository.PanolRepository
import com.example.mpazpaolr.model.CarritoItem
import com.example.mpazpaolr.model.Categoria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PanolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PanolRepository.getRepository(application)

    val equipos: StateFlow<List<EquipoEntity>> = repository.equipos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val solicitudes: StateFlow<List<SolicitudConItems>> = repository.solicitudes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var filtroCategoria by mutableStateOf<Categoria?>(null)
        private set
    var soloDisponibles by mutableStateOf(false)
        private set
    var carrito by mutableStateOf<List<CarritoItem>>(emptyList())
        private set

    init {
        viewModelScope.launch { repository.cargarDatosIniciales() }
    }

    fun filtrar(lista: List<EquipoEntity>): List<EquipoEntity> = lista.filter { equipo ->
        (filtroCategoria == null || equipo.categoria == filtroCategoria) &&
            (!soloDisponibles || equipo.reservable)
    }

    fun onCategoriaSeleccionada(categoria: Categoria?) {
        filtroCategoria = if (filtroCategoria == categoria) null else categoria
    }

    fun onSoloDisponiblesChanged(valor: Boolean) {
        soloDisponibles = valor
    }

    fun cantidadEnCarrito(equipoId: Int): Int =
        carrito.firstOrNull { it.equipoId == equipoId }?.cantidad ?: 0

    fun agregarAlCarrito(equipo: EquipoEntity): String? {
        if (!equipo.reservable) return "${equipo.nombre} no está disponible."
        val actual = cantidadEnCarrito(equipo.id)
        if (actual >= equipo.stockDisponible) return "No hay más stock disponible de ${equipo.nombre}."
        carrito = if (actual == 0) {
            carrito + CarritoItem(equipo.id, equipo.nombre, 1, equipo.stockDisponible)
        } else {
            carrito.map { if (it.equipoId == equipo.id) it.copy(cantidad = it.cantidad + 1) else it }
        }
        return null
    }

    fun cambiarCantidad(equipoId: Int, delta: Int) {
        carrito = carrito.mapNotNull { item ->
            if (item.equipoId != equipoId) return@mapNotNull item
            val nueva = item.cantidad + delta
            when {
                nueva <= 0 -> null
                nueva > item.maximo -> item
                else -> item.copy(cantidad = nueva)
            }
        }
    }

    fun quitarDelCarrito(equipoId: Int) {
        carrito = carrito.filter { it.equipoId != equipoId }
    }

    fun vaciarCarrito() {
        carrito = emptyList()
    }
}
