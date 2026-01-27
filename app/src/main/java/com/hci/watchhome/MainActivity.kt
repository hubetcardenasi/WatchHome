package com.hci.watchhome

import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hci.watchhome.ui.theme.WatchHomeTheme
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mantener pantalla encendida
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Pantalla completa compatible con todos los Android
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        // Brillo al 85%
        val lp = window.attributes
        lp.screenBrightness = 0.85f
        window.attributes = lp

        setContent {
            WatchHomeTheme {

                // Reaplicar fullscreen de manera periódica
                LaunchedEffect(Unit) {
                    while (true) {
                        window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                        delay(1000) // Reaplicar 1 vez por segundo
                    }
                }

                // Normal y Lite unificada
                RelojPantalla()
            }
        }
    }
}

@Composable
fun RelojPantalla() {

    var hora by remember { mutableStateOf("") }
    var minutos by remember { mutableStateOf("") }
    var segundos by remember { mutableStateOf("") }
    var ampm by remember { mutableStateOf("") }

    var diaActual by remember { mutableStateOf("") }
    var fechaActual by remember { mutableStateOf("") }

    // ⭐ Batería dinámica optimizada
    var bateria by remember { mutableStateOf(0) }

    val contexto = LocalContext.current
    val actividad = LocalContext.current as ComponentActivity

    // Detectar idioma
    val idioma = Locale.getDefault().language
    val esIngles = idioma == "en"

    val formatoFecha = if (esIngles) "MM/dd/yyyy" else "dd/MM/yyyy"
    val formatoDia = "EEEE"

    // Colores cíclicos
    val colores = listOf(
        Color.Red, Color.Green, Color.White,
        Color.Yellow, Color.Blue, Color.Magenta,
        Color.Cyan, Color.Gray
    )
    var indiceColor by remember { mutableStateOf(0) }
    val colorActual = colores[indiceColor]

    val configuration = LocalConfiguration.current
    val esVertical = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // ⭐ Actualización optimizada: hora cada 1s, batería cada 30s
    LaunchedEffect(Unit) {
        var contadorBateria = 0

        while (true) {
            val ahora = Calendar.getInstance().time

            // Hora
            hora = SimpleDateFormat("hh", Locale.getDefault()).format(ahora)
            minutos = SimpleDateFormat("mm", Locale.getDefault()).format(ahora)
            segundos = SimpleDateFormat("ss", Locale.getDefault()).format(ahora)

            ampm = SimpleDateFormat("a", Locale.getDefault())
                .format(ahora)
                .uppercase()
                .replace(".", "")
                .replace(" ", "")

            // Fecha
            diaActual = SimpleDateFormat(formatoDia, Locale.getDefault())
                .format(ahora)
                .replaceFirstChar { it.uppercase() }

            fechaActual = SimpleDateFormat(formatoFecha, Locale.getDefault())
                .format(ahora)
                .replaceFirstChar { it.uppercase() }

            // ⭐ Actualizar batería cada 30 segundos
            if (contadorBateria % 30 == 0) {
                val intent = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = contexto.registerReceiver(null, intent)

                val nivel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val escala = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

                bateria = ((nivel / escala.toFloat()) * 100).toInt()
            }

            contadorBateria++
            delay(1000)
        }
    }

    // ⭐ Ahorro de energía automático (batería ≤ 20%)
    SideEffect {
        val lp = actividad.window.attributes

        lp.screenBrightness = if (bateria <= 20) {
            0.50f   // ⭐ Modo ahorro
        } else {
            0.85f   // ⭐ Brillo normal
        }

        actividad.window.attributes = lp
    }

    // ⭐ UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
            .clickable {
                indiceColor = (indiceColor + 1) % colores.size
            }
    ) {

        if (esVertical) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ⭐ DÍA IZQUIERDA — BATERÍA DERECHA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diaActual,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual
                    )

                    Text(
                        text = "$bateria%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ⭐ FECHA CENTRADA
                Text(
                    text = fechaActual,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorActual,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // ⭐ HORA
                Text(
                    text = hora,
                    fontSize = 140.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual
                )
                Text(
                    text = minutos,
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual
                )

                // ⭐ Ocultar segundos cuando batería ≤ 10%
                if (bateria > 10) {
                    Text(
                        text = segundos,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = colorActual
                    )
                }

                Text(
                    text = ampm,
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual
                )

                Spacer(modifier = Modifier.weight(1f))
            }

        } else {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ⭐ DÍA IZQUIERDA — FECHA CENTRO — BATERÍA DERECHA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 10.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = diaActual,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    Text(
                        text = fechaActual,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "$bateria%",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ⭐ Ocultar segundos cuando batería ≤ 10%
                Text(
                    text = if (bateria > 10) "$hora:$minutos:$segundos" else "$hora:$minutos",
                    fontSize = 140.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual
                )

                Text(
                    text = ampm,
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RelojPantallaPreview() {
    WatchHomeTheme {
        RelojPantalla()
    }
}