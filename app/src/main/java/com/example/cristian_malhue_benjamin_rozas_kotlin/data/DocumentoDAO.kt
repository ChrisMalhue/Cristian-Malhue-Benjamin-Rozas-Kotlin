package com.example.cristian_malhue_benjamin_rozas_kotlin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Documento

@Dao
interface DocumentoDAO {

    // Inserta un documento en la tabla.
    @Insert
    suspend fun insertar(documento: Documento)

    // Recupera solo los documentos de un usuario.
    @Query("SELECT * FROM documentos WHERE usuarioId = :usuarioId")
    suspend fun listarPorUsuario(usuarioId: Int): List<Documento>

    // Borrar documentos
    @Query("DELETE FROM documentos WHERE id = :id")
    suspend fun eliminar(id: Int)
}