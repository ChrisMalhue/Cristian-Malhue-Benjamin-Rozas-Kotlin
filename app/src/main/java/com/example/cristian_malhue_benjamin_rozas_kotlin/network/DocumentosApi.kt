package com.example.cristian_malhue_benjamin_rozas_kotlin.network

import com.example.cristian_malhue_benjamin_rozas_kotlin.model.*
import retrofit2.Response
import retrofit2.http.*

interface DocumentosApi {

    // Documentos
    @POST("api/documentos")
    suspend fun subirDocumento(@Body documento: Documento): Response<Documento>

    @GET("api/documentos/usuario/{usuarioId}")
    suspend fun listarDocumentos(@Path("usuarioId") usuarioId: Int): Response<List<Documento>>

    @DELETE("api/documentos/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Int): Response<Void>

    // Salas
    @GET("api/salas")
    suspend fun listarSalas(): Response<List<Sala>>

    @GET("api/salas/area/{area}")
    suspend fun listarSalasPorArea(@Path("area") area: AreaSala): Response<List<Sala>>

    // Reservas
    @GET("api/reservas/bloques")
    suspend fun obtenerBloques(
        @Query("salaId") salaId: Int,
        @Query("fecha") fecha: String
    ): Response<List<BloqueHorario>>

    @POST("api/reservas")
    suspend fun solicitarReserva(@Body reserva: Reserva): Response<Reserva>

    @GET("api/reservas/pendientes")
    suspend fun listarReservasPendientes(): Response<List<Reserva>>

    @GET("api/reservas/usuario/{usuarioId}")
    suspend fun listarReservasProfesor(@Path("usuarioId") usuarioId: Int): Response<List<Reserva>>

    @PUT("api/reservas/{id}/aprobar")
    suspend fun aprobarReserva(@Path("id") id: Int): Response<Reserva>

    @PUT("api/reservas/{id}/rechazar")
    suspend fun rechazarReserva(
        @Path("id") id: Int,
        @Body motivoBody: Map<String, String>
    ): Response<Reserva>
}