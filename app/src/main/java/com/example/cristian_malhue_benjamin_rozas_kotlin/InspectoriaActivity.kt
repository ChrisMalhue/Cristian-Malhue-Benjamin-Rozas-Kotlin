package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Reserva
import com.example.cristian_malhue_benjamin_rozas_kotlin.network.RetrofitClient
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.launch

class InspectoriaActivity : ComponentActivity() {
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
                        PantallaInspectoria(
                            onVolver = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaInspectoria(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendientes by remember { mutableStateOf<List<Reserva>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    var reservaARechazar by remember { mutableStateOf<Reserva?>(null) }
    var motivoRechazo by remember { mutableStateOf("") }
    var procesando by remember { mutableStateOf(false) }

    suspend fun cargarPendientes() {
        cargando = true
        mensajeError = ""
        try {
            val respuesta = RetrofitClient.documentosApi.listarReservasPendientes()
            if (respuesta.isSuccessful) {
                pendientes = respuesta.body() ?: emptyList()
            } else {
                mensajeError = "Error al cargar solicitudes (${respuesta.code()})"
            }
        } catch (e: Exception) {
            mensajeError = "No se pudo conectar con el servidor de reservas"
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(Unit) {
        cargarPendientes()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Solicitudes Pendientes",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = { scope.launch { cargarPendientes() } },
                enabled = !cargando
            ) {
                Text(
                    text = "Actualizar",
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

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

                pendientes.isEmpty() -> {
                    Text("No hay solicitudes de salas pendientes por revisar.")
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pendientes, key = { it.id ?: "${it.fecha}_${it.horaInicio}" }) { reserva ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = reserva.salaNombre,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Solicitante: ${reserva.profesorNombre}")
                                    Text("Fecha: ${reserva.fecha}")
                                    Text("Horario: ${reserva.horaInicio} - ${reserva.horaFin}")

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                val idRes = reserva.id ?: return@Button
                                                procesando = true
                                                scope.launch {
                                                    try {
                                                        val resp = RetrofitClient.documentosApi.aprobarReserva(idRes)
                                                        if (resp.isSuccessful) {
                                                            pendientes = pendientes.filter { it.id != idRes }
                                                            Toast.makeText(contexto, "Reserva aprobada", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(contexto, "Error al aprobar (${resp.code()})", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(contexto, "Error de conexión", Toast.LENGTH_SHORT).show()
                                                    } finally {
                                                        procesando = false
                                                    }
                                                }
                                            },
                                            enabled = !procesando,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF2E7D32)
                                            )
                                        ) {
                                            Text("Aprobar")
                                        }

                                        Button(
                                            onClick = {
                                                motivoRechazo = ""
                                                reservaARechazar = reserva
                                            },
                                            enabled = !procesando,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Text("Rechazar")
                                        }
                                    }
                                }
                            }
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

    // Diálogo para ingresar motivo de rechazo
    if (reservaARechazar != null) {
        AlertDialog(
            onDismissRequest = { reservaARechazar = null },
            title = { Text("Rechazar solicitud") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Indica el motivo del rechazo para ${reservaARechazar?.profesorNombre}:")
                    OutlinedTextField(
                        value = motivoRechazo,
                        onValueChange = { motivoRechazo = it },
                        label = { Text("Motivo (ej: Mantención eléctrica)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaARechazar = null }) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idRes = reservaARechazar?.id ?: return@TextButton
                        val motivoLimpio = motivoRechazo.trim().ifEmpty { "Sin motivo especificado" }
                        reservaARechazar = null
                        procesando = true

                        scope.launch {
                            try {
                                val resp = RetrofitClient.documentosApi.rechazarReserva(
                                    id = idRes,
                                    motivoBody = mapOf("motivo" to motivoLimpio)
                                )
                                if (resp.isSuccessful) {
                                    pendientes = pendientes.filter { it.id != idRes }
                                    Toast.makeText(contexto, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(contexto, "Error al rechazar (${resp.code()})", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(contexto, "Error de conexión", Toast.LENGTH_SHORT).show()
                            } finally {
                                procesando = false
                            }
                        }
                    }
                ) {
                    Text("Confirmar rechazo", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}