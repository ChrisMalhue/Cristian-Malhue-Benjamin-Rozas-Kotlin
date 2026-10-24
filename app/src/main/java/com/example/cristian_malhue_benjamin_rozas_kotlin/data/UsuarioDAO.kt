package com.example.cristian_malhue_benjamin_rozas_kotlin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Usuario

@Dao
interface UsuarioDAO {

    // Inserta un usuario en la tabla.
    @Insert
    suspend fun insertar(usuario: Usuario)

    // Busca un usuario por su correo. Devuelve null si no existe.
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?
}