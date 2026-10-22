package com.example.mpazpaolr.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SolicitudConItems(
    @Embedded val solicitud: SolicitudEntity,
    @Relation(parentColumn = "id", entityColumn = "solicitudId")
    val items: List<SolicitudItemEntity>
)
