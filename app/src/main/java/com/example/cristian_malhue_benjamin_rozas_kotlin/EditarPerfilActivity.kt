package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import androidx.room.Room
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.AppDatabase
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.UsuarioDAO
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.launch

class EditarPerfilActivity : ComponentActivity() {
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

            // recibir datos de la activity "InicioActivity"
            val id = intent.getIntExtra("id", 0)

            CristianMalhueBenjaminRozasKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                        PantallaEditarPerfil(
                            dao = dao,
                            id = id,
                            onGuardado = { nombreNuevo ->
                                // Devolver el nombre nuevo a la activity "InicioActivity"
                                val resultado = Intent()
                                resultado.putExtra("nombre", nombreNuevo)
                                setResult(RESULT_OK, resultado)
                                finish() // termina esta Activity y vuelve a la anterior
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
    dao: UsuarioDAO,
    id: Int,
    onGuardado: (String) -> Unit,
    onVolver: () -> Unit
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var cargado by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmar by rememberSaveable { mutableStateOf("") }
    var foto by rememberSaveable { mutableStateOf<String?>(null) }
    var mostrarPassword by rememberSaveable { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    // Al abrir la pantalla, carga los datos actuales del usuario desde SQLite.
    LaunchedEffect(Unit) {
        if (!cargado) {
            val usuario = dao.buscarPorId(id)
            if (usuario != null) {
                nombre = usuario.nombre
                correo = usuario.correo
                password = usuario.password
                confirmar = usuario.password
                foto = usuario.foto
            }
            cargado = true
        }
    }

    // Selector nativo de archivos, filtrado solo para imágenes.
    val selectorImagen = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        // Si cancela el selector, uri es null.
        if (uri != null) {
            // Conserva el permiso para leer la imagen aunque se cierre la app.
            contexto.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            foto = uri.toString()
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
            // Vuelve a la imagen por defecto.
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
                // Se reutilizan las mismas validaciones del registro.
                val error = validarRegistro(nombre, correo, password, confirmar)

                if (error != null) {
                    mensajeError = error
                } else {
                    mensajeError = ""

                    // Ejecuta una corrutina para no bloquear la interfaz.
                    scope.launch {
                        val correoLimpio = correo.trim().lowercase()
                        val existente = dao.buscarPorCorreo(correoLimpio)

                        // El correo puede repetirse solo si es el del mismo usuario.
                        if (existente != null && existente.id != id) {
                            mensajeError = "Ya existe una cuenta con ese correo"
                        } else {
                            // Actualiza los datos del usuario en SQLite.
                            dao.actualizar(
                                id = id,
                                nombre = nombre.trim(),
                                correo = correoLimpio,
                                password = password,
                                foto = foto
                            )
                            Toast.makeText(contexto, "Perfil actualizado", Toast.LENGTH_LONG).show()
                            onGuardado(nombre.trim())
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = cargado // se habilita cuando ya se cargaron los datos
        ) {
            Text("Guardar cambios")
        }

        TextButton(
            onClick = { onVolver() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}

@Composable
fun FotoPerfil(foto: String?) {
    val contexto = LocalContext.current

    // Convierte la imagen elegida (URI) en un bitmap. Si no hay foto o falla, queda en null.
    val bitmap = remember(foto) {
        if (foto == null) {
            null
        } else {
            try {
                contexto.contentResolver.openInputStream(Uri.parse(foto))?.use { entrada ->
                    val opciones = BitmapFactory.Options().apply { inSampleSize = 4 } // reduce el tamaño
                    BitmapFactory.decodeStream(entrada, null, opciones)
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
        // Foto por defecto: personaje gris sobre fondo blanco.
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