package com.example.cristian_malhue_benjamin_rozas_kotlin.model

data class BloqueHorario(
    val horaInicio: String,
    val horaFin: String,
    val ocupado: Boolean,
    val estado: String
)