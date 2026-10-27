package com.example.cristian_malhue_benjamin_rozas_kotlin.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Creamos la tabla usuarios.
// Se le agrega eso del unique para que los correos no se repitan.
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)

data class Usuario(

    // Id autoincrementable.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val correo: String,
    val password: String,
    val foto: String? = null
)