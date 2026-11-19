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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Rol
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Usuario
import com.example.cristian_malhue_benjamin_rozas_kotlin.network.RetrofitClient
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.launch

class RegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CristianMalhueBenjaminRozasKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(24.dp)
                    ) {
                        PantallaRegistro(
                            onVolver = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaRegistro(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

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
            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmar,
            onValueChange = { confirmar = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = mostrarPassword,
                onCheckedChange = { mostrarPassword = it }
            )
            Text("Mostrar contraseña")
        }

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
                    cargando = true

                    scope.launch {
                        try {
                            val correoLimpio = correo.trim().lowercase()
                            val nuevoUsuario = Usuario(
                                nombre = nombre.trim(),
                                correo = correoLimpio,
                                password = password,
                                rol = Rol.PROFESOR // Todo registro entra como profesor del colegio
                            )

                            val respuesta = RetrofitClient.usuariosApi.registrar(nuevoUsuario)

                            if (respuesta.isSuccessful) {
                                Toast.makeText(
                                    contexto,
                                    "Cuenta creada correctamente",
                                    Toast.LENGTH_LONG
                                ).show()
                                onVolver()
                            } else if (respuesta.code() == 409) {
                                mensajeError = "Ya existe una cuenta con ese correo"
                            } else {
                                mensajeError = "Error al registrar (${respuesta.code()})"
                            }
                        } catch (e: Exception) {
                            mensajeError = "No se pudo conectar con el servidor de usuarios"
                        } finally {
                            cargando = false
                        }
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (cargando) "Registrando..." else "Registrarme")
        }

        TextButton(
            onClick = { onVolver() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

fun validarRegistro(nombre: String, correo: String, password: String, confirmar: String): String? {
    val nombreLimpio = nombre.trim()
    val correoLimpio = correo.trim().lowercase()

    val dominiosPermitidos = listOf("@gmail.com", "@duocuc.cl", "@colegio.cl")
    val dominio = dominiosPermitidos.firstOrNull { correoLimpio.endsWith(it) }
    val usuarioCorreo = if (dominio != null) correoLimpio.removeSuffix(dominio) else correoLimpio

    return when {
        nombreLimpio.length < 2 -> "El nombre debe tener al menos 2 caracteres"
        !nombreLimpio.all { it.isLetter() || it == ' ' } -> "El nombre solo puede tener letras"

        dominio == null -> "El correo debe terminar en @gmail.com, @duocuc.cl o @colegio.cl"
        usuarioCorreo.isEmpty() -> "Ingrese su correo antes del dominio"
        !usuarioCorreo.all { it.isLetterOrDigit() || it == '.' } -> "El correo solo puede tener letras, números y puntos"
        usuarioCorreo.startsWith(".") || usuarioCorreo.endsWith(".") -> "El correo no puede empezar ni terminar con punto"
        usuarioCorreo.contains("..") -> "El correo no puede tener dos puntos seguidos"
        !usuarioCorreo.any { it.isLetter() } -> "El correo debe tener al menos una letra antes del dominio"

        password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
        !password.any { it.isUpperCase() } -> "La contraseña debe tener al menos 1 mayúscula"
        !password.any { it.isDigit() } -> "La contraseña debe tener al menos 1 número"
        !password.any { !it.isLetterOrDigit() && !it.isWhitespace() } -> "La contraseña debe tener al menos 1 carácter especial"

        password != confirmar -> "Las contraseñas no coinciden"
        else -> null
    }
}