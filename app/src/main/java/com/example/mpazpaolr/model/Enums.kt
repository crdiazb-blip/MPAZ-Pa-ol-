package com.example.mpazpaolr.model

enum class Categoria(val label: String) {
    COMPUTADOR("Computador"),
    NOTEBOOK("Notebook"),
    PROYECTOR("Proyector"),
    ALARGADOR("Alargador"),
    PARLANTE("Parlante"),
    ROBOTICA("Robótica"),
    RED("Equipamiento de red"),
    OTRA("Otra")
}

enum class EstadoEquipo(val label: String) {
    DISPONIBLE("Disponible"),
    RESERVADO("Reservado"),
    PRESTADO("Prestado"),
    MANTENIMIENTO("En mantenimiento"),
    FUERA_DE_SERVICIO("Fuera de servicio")
}

enum class EstadoSolicitud(val label: String) {
    PENDIENTE("Pendiente"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada"),
    ENTREGADA("Entregada"),
    DEVUELTA("Devuelta"),
    FINALIZADA("Finalizada")
}
