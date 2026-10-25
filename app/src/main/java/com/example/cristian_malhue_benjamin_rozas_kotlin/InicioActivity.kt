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
import androidx.compose.material.icons.filled.Home
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

class InicioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            // recibir datos de la activity "LoginActivity"
            val nombre = intent.getStringExtra("nombre") ?: ""

            CristianMalhueBenjaminRozasKotlinTheme {
                PantallaInicio(nombre, onCerrarSesion = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(nombre: String, onCerrarSesion: () -> Unit) {
    val contexto = LocalContext.current

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
                        Toast.makeText(contexto, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Home, contentDescription = "Inicio")
                    }
                    IconButton(onClick = {
                        Toast.makeText(contexto, "Documentos: viene pronto", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Description, contentDescription = "Documentos")
                    }
                    IconButton(onClick = {
                        Toast.makeText(contexto, "Editar datos: viene pronto", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar datos")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            Toast.makeText(contexto, "Cargar documento: viene pronto", Toast.LENGTH_SHORT).show()
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