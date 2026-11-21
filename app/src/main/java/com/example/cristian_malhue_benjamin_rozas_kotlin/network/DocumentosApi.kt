package com.example.cristian_malhue_benjamin_rozas_kotlin.network

import com.example.cristian_malhue_benjamin_rozas_kotlin.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DocumentosApi {

    // Documentos
    @Multipart
    @POST("api/documentos/subir")
    suspend fun subirDocumento(
        @Part archivo: MultipartBody.Part,
        @Part("usuarioId") usuarioId: RequestBody,
        @Part("nombre") nombre: RequestBody? = null
    ): Response<Documento>

    @GET("api/documentos/usuario/{usuarioId}")
    suspend fun listarDocumentos(@Path("usuarioId") usuarioId: Int): Response<List<Documento>>

    @DELETE("api/documentos/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Int): Response<Void>

    // Salas
    @GET("api/salas")
    suspend fun listarSalas(): Response<List<Sala>>

    @GET("api/salas/area/{area}")
    suspend fun listarSalasPorArea(@Path("area") area: AreaSala): Response<List<Sala>>

    @POST("api/salas")
    suspend fun crearSala(@Body sala: Sala): Response<Sala>

    @PUT("api/salas/{id}")
    suspend fun actualizarSala(
        @Path("id") id: Int,
        @Body sala: Sala
    ): Response<Sala>

    @DELETE("api/salas/{id}")
    suspend fun eliminarSala(@Path("id") id: Int): Response<Void>

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