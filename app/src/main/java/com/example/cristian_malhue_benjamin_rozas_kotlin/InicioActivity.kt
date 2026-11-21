package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cristian_malhue_benjamin_rozas_kotlin.network.RetrofitClient
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val nombre = intent.getStringExtra("nombre") ?: ""
            val id = intent.getIntExtra("id", 0)
            val rol = intent.getStringExtra("rol") ?: "PROFESOR"

            CristianMalhueBenjaminRozasKotlinTheme {
                PantallaInicio(
                    nombreInicial = nombre,
                    id = id,
                    rol = rol,
                    onCerrarSesion = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    nombreInicial: String,
    id: Int,
    rol: String,
    onCerrarSesion: () -> Unit
) {
    val contexto = LocalContext.current
    var nombre by rememberSaveable { mutableStateOf(nombreInicial) }
    val scope = rememberCoroutineScope()

    val editarPerfil = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            nombre = resultado.data?.getStringExtra("nombre") ?: nombre
        }
    }

    val selectorDocumento = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val nombreArchivo = obtenerNombreArchivo(contexto, uri)
            val tipoMime = obtenerTipoMime(nombreArchivo)

            if (tipoMime == null) {
                Toast.makeText(contexto, "Solo se permiten archivos Word o PDF", Toast.LENGTH_LONG).show()
            } else {
                scope.launch {
                    try {
                        val bytes = withContext(Dispatchers.IO) {
                            contexto.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }

                        if (bytes == null) {
                            Toast.makeText(contexto, "No se pudo leer el archivo", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        val requestFile = bytes.toRequestBody(tipoMime.toMediaTypeOrNull())
                        val archivoPart = MultipartBody.Part.createFormData("archivo", nombreArchivo, requestFile)
                        val usuarioIdBody = id.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val nombreBody = nombreArchivo.toRequestBody("text/plain".toMediaTypeOrNull())

                        val respuesta = RetrofitClient.documentosApi.subirDocumento(
                            archivo = archivoPart,
                            usuarioId = usuarioIdBody,
                            nombre = nombreBody
                        )

                        if (respuesta.isSuccessful) {
                            Toast.makeText(contexto, "Subido correctamente a la nube", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(contexto, "Error al subir (${respuesta.code()})", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(contexto, "No se pudo conectar con el servidor de documentos", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    var menuAbierto by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        if (rol == "INSPECTORIA") "Panel Inspectoría" else "Inicio",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuAbierto = !menuAbierto }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menú"
                            )
                        }

                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Mis documentos") },
                                onClick = {
                                    menuAbierto = false
                                    val intent = Intent(contexto, DocumentosActivity::class.java)
                                    intent.putExtra("id", id)
                                    contexto.startActivity(intent)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Editar perfil") },
                                onClick = {
                                    menuAbierto = false
                                    val intent = Intent(contexto, EditarPerfilActivity::class.java)
                                    intent.putExtra("id", id)
                                    editarPerfil.launch(intent)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    menuAbierto = false
                                    onCerrarSesion()
                                }
                            )
                        }
                    }
                }
            )
        },

        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(contexto, DocumentosActivity::class.java)
                        intent.putExtra("id", id)
                        contexto.startActivity(intent)
                    }) {
                        Icon(Icons.Filled.Description, contentDescription = "Documentos")
                    }
                    IconButton(onClick = {
                        val intent = Intent(contexto, EditarPerfilActivity::class.java)
                        intent.putExtra("id", id)
                        editarPerfil.launch(intent)
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar datos")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            selectorDocumento.launch(arrayOf("*/*"))
                        },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.AddBox, contentDescription = "Cargar documento")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Bienvenido, $nombre",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (rol == "INSPECTORIA") "Cuenta de Administración / Inspectoría" else "Portal Docente",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (rol == "INSPECTORIA") {
                        Button(
                            onClick = {
                                val intent = Intent(contexto, InspectoriaActivity::class.java)
                                contexto.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.EventAvailable, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Gestionar Solicitudes de Salas")
                        }
                    } else {
                        Button(
                            onClick = {
                                val intent = Intent(contexto, ReservarSalaActivity::class.java).apply {
                                    putExtra("id", id)
                                    putExtra("nombre", nombre)
                                }
                                contexto.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.EventAvailable, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Reservar Sala de Clases")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(contexto, DocumentosActivity::class.java)
                                intent.putExtra("id", id)
                                contexto.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver Mis Documentos")
                        }
                    }
                }
            }
        }
    }
}

fun obtenerNombreArchivo(contexto: Context, uri: Uri): String {
    var nombre = "documento"

    contexto.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (indice >= 0 && cursor.moveToFirst()) {
            nombre = cursor.getString(indice)
        }
    }

    return nombre
}

fun obtenerTipoMime(nombre: String): String? {
    val nombreMinuscula = nombre.lowercase()

    return when {
        nombreMinuscula.endsWith(".pdf") -> "application/pdf"
        nombreMinuscula.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        nombreMinuscula.endsWith(".doc") -> "application/msword"
        else -> null
    }
}