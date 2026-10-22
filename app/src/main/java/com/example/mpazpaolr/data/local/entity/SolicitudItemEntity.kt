package com.example.mpazpaolr.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "solicitud_items", indices = [Index("solicitudId")])
data class SolicitudItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val solicitudId: Int,
    val equipoId: Int,
    val nombreEquipo: String,
    val cantidad: Int
)
