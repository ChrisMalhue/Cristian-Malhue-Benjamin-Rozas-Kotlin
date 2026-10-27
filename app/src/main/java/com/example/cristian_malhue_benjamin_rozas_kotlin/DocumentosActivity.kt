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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.AppDatabase
import com.example.cristian_malhue_benjamin_rozas_kotlin.data.DocumentoDAO
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Documento
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

class DocumentosActivity : ComponentActivity() {
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
        val dao = db.documentoDao()

        setContent {

            // recibir datos de la activity "InicioActivity"
            val id = intent.getIntExtra("id", 0)

            CristianMalhueBenjaminRozasKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                        PantallaDocumentos(
                            dao = dao,
                            id = id,
                            onVolver = { finish() } // termina esta Activity y vuelve a la anterior
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaDocumentos(dao: DocumentoDAO, id: Int, onVolver: () -> Unit) {

    var documentos by remember { mutableStateOf<List<Documento>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    // Al abrir la pantalla, consulta en SQLite los documentos de este usuario.
    LaunchedEffect(Unit) {
        documentos = dao.listarPorUsuario(id)
        cargando = false
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

        // El peso hace que la lista ocupe el espacio libre y el botón quede abajo.
        Column(modifier = Modifier.weight(1f)) {
            if (!cargando && documentos.isEmpty()) {
                Text("Aún no has subido documentos")
            } else {
                // crea una lista vertical
                LazyColumn {
                    // utilizar todos los elementos de la lista documentos
                    items(documentos) { documento ->
                        // manda el documento actual al componente gráfico.
                        DocumentoCard(
                            documento = documento,
                            onEliminar = {
                                // Ejecuta una corrutina para no bloquear la interfaz.
                                scope.launch {
                                    dao.eliminar(documento.id)
                                    // Vuelve a consultar la lista para que desaparezca la tarjeta.
                                    documentos = dao.listarPorUsuario(id)
                                    Toast.makeText(contexto, "Documento eliminado", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
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

    // Controla si se muestra la ventana de confirmación.
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
                fontSize = 18.sp
            )

            Text("Tipo: $tipo")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Button(
                    onClick = {
                        // Pide a Android que abra el archivo con una app instalada.
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(
                            Uri.parse(documento.uri),
                            obtenerTipoMime(documento.nombre)
                        )
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        try {
                            contexto.startActivity(intent)
                        } catch (e: SecurityException) {
                            Toast.makeText(contexto, "No se puede leer el archivo", Toast.LENGTH_LONG).show()
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
                        TextButton(onClick = { confirmando = false })
                        { Text("Cancelar") }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            confirmando = false
                            onEliminar()
                        })
                        { Text("Eliminar") }
                    }
                )
            }
        }
    }
}