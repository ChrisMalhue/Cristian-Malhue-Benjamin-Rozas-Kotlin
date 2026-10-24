package com.example.cristian_malhue_benjamin_rozas_kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cristian_malhue_benjamin_rozas_kotlin.ui.theme.CristianMalhueBenjaminRozasKotlinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CristianMalhueBenjaminRozasKotlinTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).padding(24.dp)) {
                        PantallaBienvenida()
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaBienvenida() {
    val contexto = LocalContext.current

    // Aparicion de titulo
    var visible by remember { mutableStateOf(false) }

    // Se ejecuta solo 1 vez al abrir la pantalla.
    LaunchedEffect(Unit) { visible = true }

    // Detecta si el botón está siendo presionado para animar su color.
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()

    val colorBoton by animateColorAsState(
        targetValue = if (presionado) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "Color del botón registrarse"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1500))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(), // sin esto el texto no tiene ancho para justificarse
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Bienvenido!",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp),
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Sistema de reservas de espacios comunes",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 31.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Justify
                )
                Text(
                    text = "Escuela Escritora Marcela Paz",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 40.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            onClick = {
                // Provisorio por ahora: en el Paso 5 lo conectamos con la pantalla de registro.
                Toast.makeText(contexto, "El registro viene en el siguiente paso", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            interactionSource = interaccion,
            colors = ButtonDefaults.buttonColors(containerColor = colorBoton)
        ) {
            Text("Registrarse")
        }

        OutlinedButton(
            onClick = {
                // Provisorio en un rato: la pantalla de login se hace más adelante.
                Toast.makeText(contexto, "El inicio de sesión viene pronto", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Iniciar sesión")
        }
    }
}