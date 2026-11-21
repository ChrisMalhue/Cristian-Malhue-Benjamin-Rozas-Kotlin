package com.example.cristian_malhue_benjamin_rozas_kotlin.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL_USUARIOS = "https://microservicio-usuario-cristian-benjamin.onrender.com/"
    private const val BASE_URL_DOCUMENTOS = "https://microservicio-documentos-cristian.onrender.com/"

    val usuariosApi: UsuariosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_USUARIOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuariosApi::class.java)
    }

    val documentosApi: DocumentosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_DOCUMENTOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DocumentosApi::class.java)
    }
}