package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Documento
import com.example.cristian_malhue_benjamin_rozas_kotlin.network.RetrofitClient
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.launch

class DocumentosActivity : ComponentActivity() {
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
                        PantallaDocumentos(
                            id = id,
                            onVolver = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDocumentos(id: Int, onVolver: () -> Unit) {
    var documentos by remember { mutableStateOf<List<Documento>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        try {
            val respuesta = RetrofitClient.documentosApi.listarDocumentos(id)
            if (respuesta.isSuccessful) {
                documentos = respuesta.body() ?: emptyList()
            } else {
                mensajeError = "Error al cargar documentos (${respuesta.code()})"
            }
        } catch (e: Exception) {
            mensajeError = "No se pudo conectar con el servidor de documentos"
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Mis documentos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Column(modifier = Modifier.weight(1f)) {
            when {
                cargando -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                mensajeError.isNotEmpty() -> {
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                documentos.isEmpty() -> {
                    Text("Aún no has subido documentos")
                }

                else -> {
                    LazyColumn {
                        items(documentos, key = { it.id ?: it.nombre }) { documento ->
                            DocumentoCard(
                                documento = documento,
                                onEliminar = {
                                    val docId = documento.id ?: return@DocumentoCard
                                    scope.launch {
                                        try {
                                            val respuesta = RetrofitClient.documentosApi.eliminarDocumento(docId)
                                            if (respuesta.isSuccessful) {
                                                documentos = documentos.filter { it.id != docId }
                                                Toast.makeText(contexto, "Documento eliminado", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(contexto, "No se pudo eliminar (${respuesta.code()})", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(contexto, "Error de conexión al eliminar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
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
fun DocumentoCard(documento: Documento, onEliminar: () -> Unit) {
    val contexto = LocalContext.current
    var confirmando by remember { mutableStateOf(false) }

    val tipo = if (obtenerTipoMime(documento.nombre) == "application/pdf") "PDF" else "Word"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = documento.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text("Tipo: $tipo")

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(
                                Uri.parse(documento.uri),
                                obtenerTipoMime(documento.nombre)
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            contexto.startActivity(intent)
                        } catch (e: SecurityException) {
                            Toast.makeText(contexto, "No se puede leer el archivo en este dispositivo", Toast.LENGTH_LONG).show()
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(contexto, "No hay una app para abrir este archivo", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Abrir")
                }

                Button(
                    onClick = { confirmando = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            }

            if (confirmando) {
                AlertDialog(
                    title = { Text("Eliminar documento") },
                    text = { Text("¿Seguro que quieres eliminar \"${documento.nombre}\"?") },
                    onDismissRequest = { confirmando = false },
                    dismissButton = {
                        TextButton(onClick = { confirmando = false }) {
                            Text("Cancelar")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            confirmando = false
                            onEliminar()
                        }) {
                            Text("Eliminar")
                        }
                    }
                )
            }
        }
    }
}