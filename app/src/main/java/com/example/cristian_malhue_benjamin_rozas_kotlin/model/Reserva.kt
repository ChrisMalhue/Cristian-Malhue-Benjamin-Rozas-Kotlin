package com.example.cristian_malhue_benjamin_rozas_kotlin.model

data class Reserva(
    val id: Int? = null,
    val salaId: Int = 0,
    val salaNombre: String = "",
    val usuarioId: Int = 0,
    val profesorNombre: String = "",
    val fecha: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val estado: EstadoReserva = EstadoReserva.PENDIENTE,
    val motivoRechazo: String? = null
)