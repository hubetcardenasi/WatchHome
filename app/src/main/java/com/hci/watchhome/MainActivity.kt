package com.hci.watchhome

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

    }
}

@Composable
fun RelojPantallaNormal() {
    var horaActual by remember { mutableStateOf("") }
    var diaActual by remember { mutableStateOf("") }
    var fechaActual by remember { mutableStateOf("") }

    // Detectar idioma
    val idioma = Locale.getDefault().language
    val esIngles = idioma == "en"

    val formatoFecha = if (esIngles) "MM/dd/yyyy" else "dd/MM/yyyy"
    val formatoDia = "EEEE"
    val formatoHora = "hh:mm:ss"

    // Colores cíclicos
    val colores = listOf(
        Color.Red, Color.Green, Color.White,
        Color.Yellow, Color.Blue, Color.Magenta,
        Color.Cyan, Color.Gray
    )
    var indiceColor by remember { mutableStateOf(0) }
    val colorActual = colores[indiceColor]

    // Actualización cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            val ahora = Calendar.getInstance().time

            horaActual = SimpleDateFormat(formatoHora, Locale.getDefault()).format(ahora)

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
        // Día arriba a la izquierda
        Text(
            text = diaActual,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = colorActual,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Fecha arriba a la derecha
        Text(
            text = fechaActual,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = colorActual,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Hora estilo digital LED al centro
        Text(
            text = horaActual,
            fontSize = 140.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace, // estilo digital
            color = colorActual,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun RelojPantallaLite() {
    var horaActual by remember { mutableStateOf("") }
    var diaActual by remember { mutableStateOf("") }
    var fechaActual by remember { mutableStateOf("") }

    // Detectar idioma
    val idioma = Locale.getDefault().language
    val esIngles = idioma == "en"

    val formatoFecha = if (esIngles) "MM/dd/yyyy" else "dd/MM/yyyy"
    val formatoDia = "EEEE"
    val formatoHora = "hh:mm:ss"

    // Colores cíclicos
    val colores = listOf(
        Color.Red, Color.Green, Color.White,
        Color.Yellow, Color.Blue, Color.Magenta,
        Color.Cyan, Color.Gray
    )
    var indiceColor by remember { mutableStateOf(0) }
    val colorActual = colores[indiceColor]

    // Actualización cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            val ahora = Calendar.getInstance().time

            horaActual = SimpleDateFormat(formatoHora, Locale.getDefault()).format(ahora)

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
        Text(
            text = diaActual,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = colorActual,
            modifier = Modifier.align(Alignment.TopStart)
        )

        Text(
            text = fechaActual,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = colorActual,
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Text(
            text = horaActual,
            fontSize = 120.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = colorActual,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center)
        )
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