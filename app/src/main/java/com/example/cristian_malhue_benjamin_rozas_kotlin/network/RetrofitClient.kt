package com.example.cristian_malhue_benjamin_rozas_kotlin.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val URL_USUARIOS = "http://localhost:8081/"
    private const val URL_DOCUMENTOS = "http://localhost:8082/"

    val usuariosApi: UsuariosApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_USUARIOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuariosApi::class.java)
    }

    val documentosApi: DocumentosApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_DOCUMENTOS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DocumentosApi::class.java)
    }
}