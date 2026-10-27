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

    // Busca un usuario por su id. Devuelve null si no existe.
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?

    // Actualiza los datos de un usuario.
    @Query("UPDATE usuarios SET nombre = :nombre, correo = :correo, password = :password, foto = :foto WHERE id = :id")
    suspend fun actualizar(id: Int, nombre: String, correo: String, password: String, foto: String?)
}