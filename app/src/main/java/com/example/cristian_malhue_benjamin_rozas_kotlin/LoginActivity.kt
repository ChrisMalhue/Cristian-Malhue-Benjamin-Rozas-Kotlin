package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.AppDatabase
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.UsuarioDAO
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crear o abrir la base de datos Room.
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "gestor.db"
        ).build()

        // Obtener el DAO para acceder a la tabla usuarios.
        val dao = db.usuarioDao()

        setContent {
            CristianMalhueBenjaminRozasKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                        // Entregar el DAO a nuestra pantalla.
                        PantallaLogin(
                            dao = dao,
                            onVolver = { finish() } // termina esta Activity y vuelve a la anterior
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaLogin(dao: UsuarioDAO, onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            text = "Iniciar sesión",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(), // Mostrar o no mostrar contraseña
            modifier = Modifier.fillMaxWidth()
        )

        // Checkbox para mostrar contraseña
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = mostrarPassword,
                onCheckedChange = { mostrarPassword = it }
            )
            Text("Mostrar contraseña")
        }

        // El mensaje de error aparece y desaparece con animación.
        AnimatedVisibility(
            visible = mensajeError.isNotEmpty(),
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = {
                if (correo.isBlank() || password.isBlank()) {
                    mensajeError = "Ingrese su correo y su contraseña"
                } else {
                    mensajeError = ""

                    // Ejecuta una corrutina para no bloquear la interfaz.
                    scope.launch {
                        // Busca al usuario por su correo en SQLite (null si no existe).
                        val usuario = dao.buscarPorCorreo(correo.trim().lowercase())

                        if (usuario == null || usuario.password != password) {
                            mensajeError = "Correo o contraseña incorrectos"
                        } else {
                            // Provisorio: en el Paso 7 abrimos la vista de bienvenida del usuario.
                            Toast.makeText(contexto, "Bienvenido, ${usuario.nombre}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        TextButton(
            onClick = { onVolver() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}