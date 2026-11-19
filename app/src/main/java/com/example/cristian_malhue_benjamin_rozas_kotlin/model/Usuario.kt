package com.example.cristian_malhue_benjamin_rozas_kotlin.model

data class Usuario(
    val id: Int? = null,
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val rol: Rol = Rol.PROFESOR,
    val foto: String? = null
)