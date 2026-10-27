package com.example.cristian_malhue_benjamin_rozas_kotlin.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Usuario
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Documento

// Define las entidades y la versión de la base de datos.
@Database(
    entities = [Usuario::class, Documento::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    // Proporciona acceso a las operaciones del DAO.
    abstract fun usuarioDao(): UsuarioDAO
    abstract fun documentoDao(): DocumentoDAO
}