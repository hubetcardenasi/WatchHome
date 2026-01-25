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

                        delay(250) // Reaplicar 4 veces por segundo
                    }
                }

                // Lite:   Android 10 o menor
                // Normal: Android 11+

                val modo = remember {
                    val api = Build.VERSION.SDK_INT
                    if (api <= Build.VERSION_CODES.Q) "lite" else "normal"
                }

                when (modo) {
                    "normal" -> RelojPantallaNormal()
                    "lite" -> RelojPantallaLite()
                }
            }
        }
    }
}

@Composable
fun obtenerPorcentajeBateria(): Int {
    val contexto = LocalContext.current
    var bateria by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val intent = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = contexto.registerReceiver(null, intent)

        val nivel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val escala = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        bateria = ((nivel / escala.toFloat()) * 100).toInt()
    }

    return bateria
}

@Composable
fun RelojPantallaNormal() {
    var hora by remember { mutableStateOf("") }
    var minutos by remember { mutableStateOf("") }
    var segundos by remember { mutableStateOf("") }
    var ampm by remember { mutableStateOf("") }
    var diaActual by remember { mutableStateOf("") }
    var fechaActual by remember { mutableStateOf("") }

    val bateria = obtenerPorcentajeBateria()

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

    // Actualización cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            val ahora = Calendar.getInstance().time

            hora = SimpleDateFormat("hh", Locale.getDefault()).format(ahora)
            minutos = SimpleDateFormat("mm", Locale.getDefault()).format(ahora)
            segundos = SimpleDateFormat("ss", Locale.getDefault()).format(ahora)

            ampm = SimpleDateFormat("a", Locale.getDefault()).format(ahora)
            ampm = ampm.uppercase()
            ampm = ampm.replace(".","")
            ampm = ampm.replace(" ","")

            diaActual = SimpleDateFormat(formatoDia, Locale.getDefault())
                .format(ahora)
                .replaceFirstChar { it.uppercase() }

            fechaActual = SimpleDateFormat(formatoFecha, Locale.getDefault()).format(ahora)

            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Fondo de pantalla negra
            .padding(20.dp)
            .clickable {
                // Cambiar color con un toque
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

                // ⭐ FECHA IZQUIERDA — BATERÍA DERECHA
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

                // ⭐ DÍA CENTRADO
                Text(
                    text = fechaActual,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorActual,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // ⭐ HORA EN TRES LÍNEAS
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

                Text(
                    text = segundos,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual
                )

                // ⭐ AM/PM
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

                    // Día (izquierda)
                    Text(
                        text = diaActual,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // Fecha (centro)
                    Text(
                        text = fechaActual,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    // Batería (derecha)
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

                // Hora centrada
                Text(
                    text = "$hora:$minutos:$segundos",
                    fontSize = 140.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual,
                    textAlign = TextAlign.Center
                )

                // AM/PM centrado
                Text(
                    text = ampm,
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RelojPantallaLite() {
    var hora by remember { mutableStateOf("") }
    var minutos by remember { mutableStateOf("") }
    var segundos by remember { mutableStateOf("") }
    var ampm by remember { mutableStateOf("") }
    var diaActual by remember { mutableStateOf("") }
    var fechaActual by remember { mutableStateOf("") }

    val bateria = obtenerPorcentajeBateria()

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

    // Actualización cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            val ahora = Calendar.getInstance().time

            hora = SimpleDateFormat("hh", Locale.getDefault()).format(ahora)
            minutos = SimpleDateFormat("mm", Locale.getDefault()).format(ahora)
            segundos = SimpleDateFormat("ss", Locale.getDefault()).format(ahora)

            ampm = SimpleDateFormat("a", Locale.getDefault()).format(ahora)
            ampm = ampm.uppercase()
            ampm = ampm.replace(".","")
            ampm = ampm.replace(" ","")

            diaActual = SimpleDateFormat(formatoDia, Locale.getDefault())
                .format(ahora)
                .replaceFirstChar { it.uppercase() }

            fechaActual = SimpleDateFormat(formatoFecha, Locale.getDefault()).format(ahora)

            delay(1000)
        }
    }

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

                // ⭐ FECHA IZQUIERDA — BATERÍA DERECHA
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

                // ⭐ DÍA CENTRADO
                Text(
                    text = fechaActual,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorActual,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                // ⭐ HORA EN TRES LÍNEAS
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

                Text(
                    text = segundos,
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual
                )

                // ⭐ AM/PM
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

                    // Día (izquierda)
                    Text(
                        text = diaActual,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    // Fecha (centro)
                    Text(
                        text = fechaActual,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorActual,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    // Batería (derecha)
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

                // Hora centrada
                Text(
                    text = "$hora:$minutos:$segundos",
                    fontSize = 140.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual,
                    textAlign = TextAlign.Center
                )

                // AM/PM centrado
                Text(
                    text = ampm,
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colorActual,
                    textAlign = TextAlign.Center
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
        // ⭐ Detección automática del modo
        val modo = remember {
            val api = Build.VERSION.SDK_INT

            when {
                api <= Build.VERSION_CODES.Q -> "lite"   // Android 10 o menor
                else -> "normal"                         // Android 11+
            }
        }

        when (modo) {
            "normal" -> RelojPantallaNormal()
            "lite" -> RelojPantallaLite()
        }
    }
}