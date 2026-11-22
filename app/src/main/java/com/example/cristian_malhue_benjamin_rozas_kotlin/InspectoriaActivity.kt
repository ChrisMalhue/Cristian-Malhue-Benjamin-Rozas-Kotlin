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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.AreaSala
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Reserva
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Sala
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInspectoria(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var tabSeleccionada by remember { mutableIntStateOf(0) }
    val titulosTabs = listOf("Solicitudes", "Salas")

    // --- Estados para Solicitudes ---
    var pendientes by remember { mutableStateOf<List<Reserva>>(emptyList()) }
    var cargandoPendientes by remember { mutableStateOf(true) }
    var errorPendientes by remember { mutableStateOf("") }
    var reservaARechazar by remember { mutableStateOf<Reserva?>(null) }
    var motivoRechazo by remember { mutableStateOf("") }

    // --- Estados para CRUD de Salas ---
    var salas by remember { mutableStateOf<List<Sala>>(emptyList()) }
    var cargandoSalas by remember { mutableStateOf(true) }
    var errorSalas by remember { mutableStateOf("") }

    var mostrarDialogoSala by remember { mutableStateOf(false) }
    var salaEnEdicion by remember { mutableStateOf<Sala?>(null) }
    var nombreSalaInput by remember { mutableStateOf("") }
    var areaSalaInput by remember { mutableStateOf(AreaSala.SALA_GENERICA) }
    var dropdownAreaExpandido by remember { mutableStateOf(false) }

    var salaAEliminar by remember { mutableStateOf<Sala?>(null) }
    var procesando by remember { mutableStateOf(false) }

    suspend fun cargarPendientes() {
        cargandoPendientes = true
        errorPendientes = ""
        try {
            val respuesta = RetrofitClient.documentosApi.listarReservasPendientes()
            if (respuesta.isSuccessful) {
                pendientes = respuesta.body() ?: emptyList()
            } else {
                errorPendientes = "Error al cargar solicitudes (${respuesta.code()})"
            }
        } catch (e: Exception) {
            errorPendientes = "No se pudo conectar con el servidor de reservas"
        } finally {
            cargandoPendientes = false
        }
    }

    suspend fun cargarSalas() {
        cargandoSalas = true
        errorSalas = ""
        try {
            val respuesta = RetrofitClient.documentosApi.listarSalas()
            if (respuesta.isSuccessful) {
                salas = respuesta.body() ?: emptyList()
            } else {
                errorSalas = "Error al cargar salas (${respuesta.code()})"
            }
        } catch (e: Exception) {
            errorSalas = "No se pudo conectar con el servidor de salas"
        } finally {
            cargandoSalas = false
        }
    }

    LaunchedEffect(Unit) {
        cargarPendientes()
        cargarSalas()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Panel de Inspectoría",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        TabRow(selectedTabIndex = tabSeleccionada) {
            titulosTabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionada == index,
                    onClick = { tabSeleccionada = index },
                    text = { Text(titulo) }
                )
            }
        }

        if (tabSeleccionada == 0) {
            // ================= PESTAÑA 1: SOLICITUDES PENDIENTES =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Solicitudes Pendientes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { scope.launch { cargarPendientes() } },
                    enabled = !cargandoPendientes
                ) {
                    Text(text = "Actualizar", maxLines = 1, softWrap = false)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                when {
                    cargandoPendientes -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorPendientes.isNotEmpty() -> {
                        Text(text = errorPendientes, color = MaterialTheme.colorScheme.error)
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
        } else {
            // ================= PESTAÑA 2: GESTIÓN DE SALAS (CRUD) =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Salas Registradas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        salaEnEdicion = null
                        nombreSalaInput = ""
                        areaSalaInput = AreaSala.SALA_GENERICA
                        mostrarDialogoSala = true
                    },
                    enabled = !procesando
                ) {
                    Text("+ Nueva Sala", maxLines = 1, softWrap = false)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                when {
                    cargandoSalas -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorSalas.isNotEmpty() -> {
                        Text(text = errorSalas, color = MaterialTheme.colorScheme.error)
                    }

                    salas.isEmpty() -> {
                        Text("No hay salas registradas en el sistema.")
                    }

                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(salas, key = { it.id ?: it.nombre }) { sala ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = sala.nombre,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Área: ${sala.area.etiqueta}")

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    salaEnEdicion = sala
                                                    nombreSalaInput = sala.nombre
                                                    areaSalaInput = sala.area
                                                    mostrarDialogoSala = true
                                                },
                                                enabled = !procesando
                                            ) {
                                                Text("Editar")
                                            }

                                            Button(
                                                onClick = { salaAEliminar = sala },
                                                enabled = !procesando,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Text("Eliminar")
                                            }
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

    // --- Diálogo para Rechazar Solicitud ---
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
                        val motivoLimpio = motivoRechazo.trim()
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
                    },
                    enabled = motivoRechazo.isNotBlank()
                ) {
                    Text("Confirmar rechazo", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // --- Diálogo para Crear / Editar Sala ---
    if (mostrarDialogoSala) {
        val esEdicion = salaEnEdicion != null
        AlertDialog(
            onDismissRequest = { mostrarDialogoSala = false },
            title = { Text(if (esEdicion) "Editar Sala" else "Nueva Sala") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nombreSalaInput,
                        onValueChange = { nombreSalaInput = it },
                        label = { Text("Nombre de la sala") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    ExposedDropdownMenuBox(
                        expanded = dropdownAreaExpandido,
                        onExpandedChange = { dropdownAreaExpandido = !dropdownAreaExpandido }
                    ) {
                        OutlinedTextField(
                            value = areaSalaInput.etiqueta,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Área / Categoría") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAreaExpandido)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownAreaExpandido,
                            onDismissRequest = { dropdownAreaExpandido = false }
                        ) {
                            AreaSala.entries.forEach { area ->
                                DropdownMenuItem(
                                    text = { Text(area.etiqueta) },
                                    onClick = {
                                        areaSalaInput = area
                                        dropdownAreaExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSala = false }) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nombreLimpio = nombreSalaInput.trim()
                        if (nombreLimpio.isEmpty()) {
                            Toast.makeText(contexto, "Ingresa un nombre para la sala", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        val salaAEnviar = Sala(
                            id = salaEnEdicion?.id,
                            nombre = nombreLimpio,
                            area = areaSalaInput
                        )
                        mostrarDialogoSala = false
                        procesando = true

                        scope.launch {
                            try {
                                val resp = if (esEdicion && salaAEnviar.id != null) {
                                    RetrofitClient.documentosApi.actualizarSala(salaAEnviar.id, salaAEnviar)
                                } else {
                                    RetrofitClient.documentosApi.crearSala(salaAEnviar)
                                }

                                if (resp.isSuccessful) {
                                    cargarSalas()
                                    Toast.makeText(
                                        contexto,
                                        if (esEdicion) "Sala actualizada" else "Sala creada",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(contexto, "Error al guardar (${resp.code()})", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(contexto, "Error de conexión", Toast.LENGTH_SHORT).show()
                            } finally {
                                procesando = false
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            }
        )
    }

    // --- Diálogo de Confirmación para Eliminar Sala ---
    if (salaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { salaAEliminar = null },
            title = { Text("Eliminar Sala") },
            text = {
                Text("¿Seguro que deseas eliminar \"${salaAEliminar?.nombre}\"? Esta acción no se puede deshacer.")
            },
            dismissButton = {
                TextButton(onClick = { salaAEliminar = null }) {
                    Text("Cancelar")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val idSala = salaAEliminar?.id ?: return@TextButton
                        salaAEliminar = null
                        procesando = true

                        scope.launch {
                            try {
                                val resp = RetrofitClient.documentosApi.eliminarSala(idSala)
                                if (resp.isSuccessful) {
                                    salas = salas.filter { it.id != idSala }
                                    Toast.makeText(contexto, "Sala eliminada", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(
                                        contexto,
                                        "No se pudo eliminar (puede tener reservas asociadas)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(contexto, "Error de conexión", Toast.LENGTH_SHORT).show()
                            } finally {
                                procesando = false
                            }
                        }
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}