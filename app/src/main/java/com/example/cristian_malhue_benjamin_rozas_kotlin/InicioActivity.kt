package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.saveable.rememberSaveable
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.rememberCoroutineScope
import androidx.room.Room
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.AppDatabase
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.DocumentoDAO
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Documento
import kotlinx.coroutines.launch

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Crear o abrir la base de datos Room.
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "gestor.db"
        ).build()

        // Obtener el DAO para acceder a la tabla documentos.
        val documentoDao = db.documentoDao()

        setContent {
            // recibir datos de la activity "LoginActivity"
            val nombre = intent.getStringExtra("nombre") ?: ""
            val id = intent.getIntExtra("id", 0)

            CristianMalhueBenjaminRozasKotlinTheme {
                PantallaInicio(
                    nombreInicial = nombre,
                    id = id,
                    documentoDao = documentoDao,
                    onCerrarSesion = { finish() } // termina esta Activity y vuelve a la anterior
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(nombreInicial: String, id: Int, documentoDao: DocumentoDAO, onCerrarSesion: () -> Unit) {
    val contexto = LocalContext.current

    var nombre by rememberSaveable { mutableStateOf(nombreInicial) }

    // Abre otra Activity y recibe lo que ella devuelva al terminar.
    val editarPerfil = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        if (resultado.resultCode == Activity.RESULT_OK) {
            nombre = resultado.data?.getStringExtra("nombre") ?: nombre
        }
    }

    val scope = rememberCoroutineScope()

    // Selector nativo de archivos (el mismo de EjemploArchivos).
    val selectorDocumento = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        // Si cancela el selector, uri es null.
        if (uri != null) {
            val nombreArchivo = obtenerNombreArchivo(contexto, uri)

            if (obtenerTipoMime(nombreArchivo) == null) {
                Toast.makeText(contexto, "Solo se permiten archivos Word o PDF", Toast.LENGTH_LONG).show()
            } else {
                // Conserva el permiso para leer el archivo aunque se cierre la app.
                contexto.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // Ejecuta una corrutina para no bloquear la interfaz.
                scope.launch {
                    documentoDao.insertar(
                        Documento(
                            usuarioId = id,
                            nombre = nombreArchivo,
                            uri = uri.toString()
                        )
                    )
                    Toast.makeText(contexto, "Subido correctamente", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Controla si el menú desplegable está abierto o cerrado.
    var menuAbierto by remember { mutableStateOf(false) }

    // Controla la aparición animada del mensaje de bienvenida.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        // Barra de arriba
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(
                        "Inicio",
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
                                text = { Text("Acción 1") },
                                onClick = { menuAbierto = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Acción 2") },
                                onClick = { menuAbierto = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Acción 3") },
                                onClick = { menuAbierto = false }
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

        // Barra de abajo
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

        // Mensaje de bienvenida con el nombre del usuario.
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
                Text(
                    text = "Bienvenido, $nombre",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Obtiene el nombre del archivo elegido (ejemplo: "informe.pdf").
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

// Devuelve el tipo del archivo si es PDF o Word. Si es de otro tipo, devuelve null.
fun obtenerTipoMime(nombre: String): String? {
    val nombreMinuscula = nombre.lowercase()

    return when {
        nombreMinuscula.endsWith(".pdf") -> "application/pdf"
        nombreMinuscula.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        nombreMinuscula.endsWith(".doc") -> "application/msword"
        else -> null
    }
}