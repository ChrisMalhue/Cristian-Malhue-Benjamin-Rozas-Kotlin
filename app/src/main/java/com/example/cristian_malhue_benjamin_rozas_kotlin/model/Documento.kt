package com.example.cristian_malhue_benjamin_rozas_kotlin.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Crea una tabla llamada "documentos".
@Entity(tableName = "documentos")
data class Documento(

    // Identificador único autoincremental.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Id del usuario dueño del documento (el "id" de la tabla usuarios).
    val usuarioId: Int,

    val nombre: String,

    // Dirección (URI) del archivo elegido.
    val uri: String
)