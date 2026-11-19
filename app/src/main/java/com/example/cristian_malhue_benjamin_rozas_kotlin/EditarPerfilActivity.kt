package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class EditarPerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val id = intent.getIntExtra("id", 0)

            CristianMalhueBenjaminRozasKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(24.dp)
                    ) {
                        PantallaEditarPerfil(
                            id = id,
                            onGuardado = { nombreNuevo ->
                                val resultado = Intent()
                                resultado.putExtra("nombre", nombreNuevo)
                                setResult(RESULT_OK, resultado)
                                finish()
                            },
                            onVolver = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaEditarPerfil(
    id: Int,
    onGuardado: (String) -> Unit,
    onVolver: () -> Unit
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var cargado by rememberSaveable { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<String?>(null) }
    var rolUsuario by rememberSaveable { mutableStateOf(Rol.PROFESOR) }
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    LaunchedEffect(id) {
        if (!cargado) {
            try {
                val respuesta = RetrofitClient.usuariosApi.obtenerPorId(id)
                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val usuario = respuesta.body()!!
                    nombre = usuario.nombre
                    correo = usuario.correo
                    password = usuario.password
                    confirmar = usuario.password
                    foto = usuario.foto
                    rolUsuario = usuario.rol
                } else {
                    mensajeError = "No se pudieron cargar los datos del perfil"
                }
            } catch (e: Exception) {
                mensajeError = "Error de conexión con el servidor de usuarios"
            } finally {
                cargado = true
            }
        }
    }

    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val base64 = withContext(Dispatchers.IO) {
                    comprimirImagenABase64(contexto, uri)
                }
                if (base64 != null) {
                    foto = base64
                } else {
                    Toast.makeText(contexto, "No se pudo procesar la imagen", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editar perfil",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        FotoPerfil(foto)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { selectorImagen.launch(arrayOf("image/*")) }) {
                Text("Cambiar foto")
            }
            TextButton(
                onClick = { foto = null },
                enabled = foto != null
            ) {
                Text("Quitar foto")
            }
        }

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    guardando = true

                    scope.launch {
                        try {
                            val usuarioActualizado = Usuario(
                                id = id,
                                nombre = nombre.trim(),
                                correo = correo.trim().lowercase(),
                                password = password,
                                rol = rolUsuario,
                                foto = foto
                            )

                            val respuesta = RetrofitClient.usuariosApi.actualizar(id, usuarioActualizado)

                            if (respuesta.isSuccessful) {
                                Toast.makeText(contexto, "Perfil actualizado", Toast.LENGTH_LONG).show()
                                onGuardado(nombre.trim())
                            } else if (respuesta.code() == 409) {
                                mensajeError = "Ya existe una cuenta con ese correo"
                            } else {
                                mensajeError = "Error al guardar (${respuesta.code()})"
                            }
                        } catch (e: Exception) {
                            mensajeError = "No se pudo conectar con el servidor de usuarios"
                        } finally {
                            guardando = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = cargado && !guardando
        ) {
            Text(if (guardando) "Guardando..." else "Guardar cambios")
        }

        TextButton(
            onClick = { onVolver() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

fun comprimirImagenABase64(contexto: Context, uri: Uri): String? {
    return try {
        val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
        val bitmap = contexto.contentResolver.openInputStream(uri)?.use { entrada ->
            BitmapFactory.decodeStream(entrada, null, opciones)
        } ?: return null

        val salida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, salida)
        Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun FotoPerfil(foto: String?) {
    val contexto = LocalContext.current

    val bitmap = remember(foto) {
        if (foto.isNullOrBlank()) {
            null
        } else {
            try {
                if (foto.startsWith("content://") || foto.startsWith("file://")) {
                    contexto.contentResolver.openInputStream(Uri.parse(foto))?.use { entrada ->
                        val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
                        BitmapFactory.decodeStream(entrada, null, opciones)
                    }
                } else {
                    val bytes = Base64.decode(foto, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(2.dp, Color.LightGray, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Foto de perfil por defecto",
                tint = Color.Gray,
                modifier = Modifier.fillMaxSize(0.7f)
            )
        }
    }
}