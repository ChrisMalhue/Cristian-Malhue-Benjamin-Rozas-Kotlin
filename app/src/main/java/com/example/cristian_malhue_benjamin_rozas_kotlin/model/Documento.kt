package com.example.cristian_malhue_benjamin_rozas_kotlin.model

data class Documento(
    val id: Int? = null,
    val usuarioId: Int = 0,
    val nombre: String = "",
    val uri: String = "",
    val contenidoBase64: String? = null
)