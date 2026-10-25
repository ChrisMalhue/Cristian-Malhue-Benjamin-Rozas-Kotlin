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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
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
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Usuario
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.launch


class RegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crear o abrir la base de datos llamada Room.
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
                        PantallaRegistro(
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
fun PantallaRegistro(dao: UsuarioDAO, onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    // rememberSaveable conserva lo escrito si se gira el dispositivo.
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Text(
            text = "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
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

        OutlinedTextField(
            value = confirmar,
            onValueChange = { confirmar = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(), // Mostrar o no mostrar contraseña
            modifier = Modifier.fillMaxWidth()
        )

        // Checkbox de mostrar contraseña
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
                val error = validarRegistro(nombre, correo, password, confirmar)

                if (error != null) {
                    mensajeError = error
                } else {
                    mensajeError = ""

                    // Ejecuta una corrutina para no bloquear la interfaz.
                    scope.launch {
                        val correoLimpio = correo.trim().lowercase()

                        if (dao.buscarPorCorreo(correoLimpio) != null) {
                            mensajeError = "Ya existe una cuenta con ese correo"
                        } else {
                            // Inserta el usuario en SQLite.
                            dao.insertar(
                                Usuario(
                                    nombre = nombre.trim(),
                                    correo = correoLimpio,
                                    password = password
                                )
                            )
                            Toast.makeText(contexto, "Cuenta creada correctamente", Toast.LENGTH_LONG).show()
                            onVolver()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrarme")
        }

        TextButton(
            onClick = { onVolver() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

// Devuelve el mensaje de error, o null si todos los datos son válidos.
fun validarRegistro(nombre: String, correo: String, password: String, confirmar: String): String? {

    val nombreLimpio = nombre.trim()
    val correoLimpio = correo.trim().lowercase()

    // Dominios de correo permitidos.
    val dominiosPermitidos = listOf("@gmail.com", "@duocuc.cl")

    // Busca cuál de los dominios usa el correo. Si no coincide ninguno, queda en null.
    val dominio = dominiosPermitidos.firstOrNull { correoLimpio.endsWith(it) }

    // Lo que va antes del dominio (ejemplo: en "ana.perez@gmail.com" es "ana.perez").
    val usuarioCorreo = if (dominio != null) correoLimpio.removeSuffix(dominio) else correoLimpio



    return when {
        // Nombre: mínimo 2 caracteres, solo letras y espacios (sin caracteres especiales ni números).
        nombreLimpio.length < 2 -> "El nombre debe tener al menos 2 caracteres"
        !nombreLimpio.all { it.isLetter() || it == ' ' } -> "El nombre solo puede tener letras"

        // Correo: debe terminar en uno de los dominios permitidos y tener algo válido antes.
        dominio == null -> "El correo debe terminar en @gmail.com o @duocuc.cl"
        usuarioCorreo.isEmpty() -> "Ingrese su correo antes del dominio"
        !usuarioCorreo.all { it.isLetterOrDigit() || it == '.' } -> "El correo solo puede tener letras, números y puntos"
        usuarioCorreo.startsWith(".") || usuarioCorreo.endsWith(".") -> "El correo no puede empezar ni terminar con punto"
        usuarioCorreo.contains("..") -> "El correo no puede tener dos puntos seguidos"
        !usuarioCorreo.any { it.isLetter() } -> "El correo debe tener al menos una letra antes del dominio"

        // Contraseña: mínimo 8 caracteres, 1 mayúscula, 1 número y 1 carácter especial.
        password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
        !password.any { it.isUpperCase() } -> "La contraseña debe tener al menos 1 mayúscula"
        !password.any { it.isDigit() } -> "La contraseña debe tener al menos 1 número"
        !password.any { !it.isLetterOrDigit() && !it.isWhitespace() } -> "La contraseña debe tener al menos 1 carácter especial"

        password != confirmar -> "Las contraseñas no coinciden"
        else -> null
    }
}