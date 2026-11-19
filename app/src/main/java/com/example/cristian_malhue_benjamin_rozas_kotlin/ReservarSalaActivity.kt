package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.BloqueHorario
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.EstadoReserva
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Reserva
import com.example.cristian_malhue_benjamin_rozas_kotlin.model.Sala
import com.example.cristian_malhue_benjamin_rozas_kotlin.network.RetrofitClient
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ReservarSalaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val usuarioId = intent.getIntExtra("id", 0)
            val profesorNombre = intent.getStringExtra("nombre") ?: "Profesor"

            CristianMalhueBenjaminRozasKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(24.dp)
                    ) {
                        PantallaReservarSala(
                            usuarioId = usuarioId,
                            profesorNombre = profesorNombre,
                            onVolver = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaReservarSala(
    usuarioId: Int,
    profesorNombre: String,
    onVolver: () -> Unit
) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var salas by remember { mutableStateOf<List<Sala>>(emptyList()) }
    var salaSeleccionada by remember { mutableStateOf<Sala?>(null) }

    // Si hoy es sábado o domingo, avanza automáticamente al lunes siguiente
    var fechaSeleccionada by remember {
        mutableStateOf(
            when (LocalDate.now().dayOfWeek) {
                DayOfWeek.SATURDAY -> LocalDate.now().plusDays(2)
                DayOfWeek.SUNDAY -> LocalDate.now().plusDays(1)
                else -> LocalDate.now()
            }
        )
    }

    var bloques by remember { mutableStateOf<List<BloqueHorario>>(emptyList()) }
    var bloqueSeleccionado by remember { mutableStateOf<BloqueHorario?>(null) }
    var misReservas by remember { mutableStateOf<List<Reserva>>(emptyList()) }

    var cargandoInicial by remember { mutableStateOf(true) }
    var cargandoBloques by remember { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val formatoIso = DateTimeFormatter.ISO_LOCAL_DATE
    val formatoVisual = DateTimeFormatter.ofPattern("EEEE dd 'de' MMMM", Locale("es", "CL"))

    // Función para recargar bloques del día y las solicitudes del profesor
    suspend fun recargarDatos(sala: Sala?, fecha: LocalDate) {
        if (sala?.id != null) {
            cargandoBloques = true
            bloqueSeleccionado = null
            try {
                val respBloques = RetrofitClient.documentosApi.obtenerBloques(
                    salaId = sala.id,
                    fecha = fecha.format(formatoIso)
                )
                if (respBloques.isSuccessful) {
                    bloques = respBloques.body() ?: emptyList()
                }
            } catch (e: Exception) {
                mensajeError = "Error al cargar los bloques horarios"
            } finally {
                cargandoBloques = false
            }
        }

        try {
            val respMisReservas = RetrofitClient.documentosApi.listarReservasProfesor(usuarioId)
            if (respMisReservas.isSuccessful) {
                misReservas = respMisReservas.body() ?: emptyList()
            }
        } catch (_: Exception) { }
    }

    // Carga inicial de salas y reservas del profesor
    LaunchedEffect(Unit) {
        try {
            val respSalas = RetrofitClient.documentosApi.listarSalas()
            if (respSalas.isSuccessful) {
                salas = respSalas.body() ?: emptyList()
                salaSeleccionada = salas.firstOrNull()
            }
            recargarDatos(salaSeleccionada, fechaSeleccionada)
        } catch (e: Exception) {
            mensajeError = "No se pudo conectar con el servidor de reservas"
        } finally {
            cargandoInicial = false
        }
    }

    // Selector de fecha nativo (DatePickerDialog)
    val selectorFecha = DatePickerDialog(
        contexto,
        { _, anio, mes, dia ->
            val nuevaFecha = LocalDate.of(anio, mes + 1, dia)
            if (nuevaFecha.dayOfWeek == DayOfWeek.SATURDAY || nuevaFecha.dayOfWeek == DayOfWeek.SUNDAY) {
                Toast.makeText(contexto, "Solo se permiten reservas de Lunes a Viernes", Toast.LENGTH_LONG).show()
            } else {
                fechaSeleccionada = nuevaFecha
                mensajeError = ""
                scope.launch {
                    recargarDatos(salaSeleccionada, nuevaFecha)
                }
            }
        },
        fechaSeleccionada.year,
        fechaSeleccionada.monthValue - 1,
        fechaSeleccionada.dayOfMonth
    ).apply {
        datePicker.minDate = System.currentTimeMillis() - 1000
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Reservar Sala",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        if (cargandoInicial) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Selector de Sala (Deslizable horizontal)
                item {
                    Text(
                        text = "1. Selecciona un espacio",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(salas) { sala ->
                            FilterChip(
                                selected = salaSeleccionada?.id == sala.id,
                                onClick = {
                                    salaSeleccionada = sala
                                    mensajeError = ""
                                    scope.launch {
                                        recargarDatos(sala, fechaSeleccionada)
                                    }
                                },
                                label = { Text("${sala.nombre} (${sala.area.name})") }
                            )
                        }
                    }
                }

                // 2. Selector de Fecha
                item {
                    Text(
                        text = "2. Fecha de uso (Lunes a Viernes)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { selectorFecha.show() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = fechaSeleccionada.format(formatoVisual)
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                }

                // 3. Carrusel de Bloques Horarios de 45 min
                item {
                    Text(
                        text = "3. Bloques de 45 minutos (Desliza para ver)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (cargandoBloques) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bloques) { bloque ->
                                val esSeleccionado = bloqueSeleccionado == bloque
                                val colorFondo = when {
                                    bloque.ocupado -> Color(0xFFE0E0E0) // Gris para ocupados/pendientes
                                    esSeleccionado -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val colorTexto = if (bloque.ocupado) Color.Gray else MaterialTheme.colorScheme.onSurface

                                Card(
                                    modifier = Modifier
                                        .width(135.dp)
                                        .clickable(enabled = !bloque.ocupado) {
                                            bloqueSeleccionado = if (esSeleccionado) null else bloque
                                        },
                                    colors = CardDefaults.cardColors(containerColor = colorFondo),
                                    border = BorderStroke(
                                        width = if (esSeleccionado) 2.dp else 1.dp,
                                        color = if (esSeleccionado) MaterialTheme.colorScheme.primary else Color.LightGray
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${bloque.horaInicio} - ${bloque.horaFin}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = colorTexto
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when (bloque.estado) {
                                                "ACEPTADA" -> "Ocupado"
                                                "PENDIENTE" -> "En revisión"
                                                else -> "Disponible"
                                            },
                                            fontSize = 12.sp,
                                            color = if (bloque.ocupado) Color.DarkGray else Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (mensajeError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = mensajeError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val sala = salaSeleccionada
                            val bloque = bloqueSeleccionado
                            if (sala?.id == null || bloque == null) {
                                mensajeError = "Selecciona una sala y un bloque disponible"
                            } else {
                                mensajeError = ""
                                enviando = true
                                scope.launch {
                                    try {
                                        val nuevaReserva = Reserva(
                                            salaId = sala.id,
                                            salaNombre = sala.nombre,
                                            usuarioId = usuarioId,
                                            profesorNombre = profesorNombre,
                                            fecha = fechaSeleccionada.format(formatoIso),
                                            horaInicio = bloque.horaInicio,
                                            horaFin = bloque.horaFin
                                        )
                                        val respuesta = RetrofitClient.documentosApi.solicitarReserva(nuevaReserva)
                                        if (respuesta.isSuccessful) {
                                            Toast.makeText(
                                                contexto,
                                                "Solicitud enviada a Inspectoría",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            recargarDatos(sala, fechaSeleccionada)
                                        } else {
                                            mensajeError = "El bloque ya no está disponible (${respuesta.code()})"
                                        }
                                    } catch (e: Exception) {
                                        mensajeError = "No se pudo enviar la solicitud"
                                    } finally {
                                        enviando = false
                                    }
                                }
                            }
                        },
                        enabled = bloqueSeleccionado != null && !enviando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (enviando) "Enviando solicitud..." else "Solicitar Bloque")
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Mis solicitudes de reserva",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (misReservas.isEmpty()) {
                    item {
                        Text("Aún no has realizado solicitudes de salas.")
                    }
                } else {
                    items(misReservas, key = { it.id ?: "${it.fecha}_${it.horaInicio}" }) { reserva ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = reserva.salaNombre,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    val colorEstado = when (reserva.estado) {
                                        EstadoReserva.ACEPTADA -> Color(0xFF2E7D32)
                                        EstadoReserva.RECHAZADA -> MaterialTheme.colorScheme.error
                                        EstadoReserva.PENDIENTE -> Color(0xFFEF6C00)
                                    }
                                    Text(
                                        text = reserva.estado.name,
                                        fontWeight = FontWeight.Bold,
                                        color = colorEstado
                                    )
                                }
                                Text("Fecha: ${reserva.fecha} | Bloque: ${reserva.horaInicio} - ${reserva.horaFin}")

                                if (reserva.estado == EstadoReserva.RECHAZADA && !reserva.motivoRechazo.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Motivo de rechazo: ${reserva.motivoRechazo}",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 13.sp
                                    )
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
}