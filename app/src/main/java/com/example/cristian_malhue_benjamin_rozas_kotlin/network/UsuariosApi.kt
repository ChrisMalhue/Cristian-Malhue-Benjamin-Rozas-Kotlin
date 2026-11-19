package com.example.cristian_malhue_benjamin_rozas_kotlin.network

import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Usuario
import retrofit2.Response
import retrofit2.http.*

interface UsuariosApi {

    @POST("api/usuarios/registro")
    suspend fun registrar(@Body usuario: Usuario): Response<Usuario>

    @POST("api/usuarios/login")
    suspend fun login(@Body credenciales: Map<String, String>): Response<Usuario>

    @GET("api/usuarios/{id}")
    suspend fun obtenerPorId(@Path("id") id: Int): Response<Usuario>

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body usuario: Usuario): Response<Usuario>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Int): Response<Void>
}