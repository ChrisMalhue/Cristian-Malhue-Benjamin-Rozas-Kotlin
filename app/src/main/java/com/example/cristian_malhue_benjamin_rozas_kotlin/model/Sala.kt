package com.example.cristian_malhue_benjamin_rozas_kotlin.model

data class Sala(
    val id: Int? = null,
    val nombre: String = "",
    val area: AreaSala = AreaSala.DEPORTE
)