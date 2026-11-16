package echo.music.iad1tya.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import echo.music.iad1tya.constants.CustomBgBlurKey
import echo.music.iad1tya.constants.CustomBgColorKey
import echo.music.iad1tya.constants.CustomBgDimKey
import echo.music.iad1tya.constants.CustomBgGradientEndKey
import echo.music.iad1tya.constants.CustomBgGradientStartKey
import echo.music.iad1tya.constants.CustomBgImagePathKey
import echo.music.iad1tya.constants.CustomBgModeKey
import echo.music.iad1tya.utils.rememberPreference
import java.io.File

/** Modos de fondo personalizado. OFF = se usa el estilo normal de la app. */
enum class CustomBgMode {
  OFF,
  COVER,
  COLOR,
  GRADIENT,
  IMAGE;

  companion object {
    fun fromName(name: String?): CustomBgMode = entries.firstOrNull { it.name == name } ?: OFF
  }
}

const val DEFAULT_CUSTOM_COLOR = 0xFF7C3AED.toInt()
const val DEFAULT_GRADIENT_START = 0xFF7C3AED.toInt()
const val DEFAULT_GRADIENT_END = 0xFF000000.toInt()

/**
 * Capa de fondo personalizada del reproductor. Se dibuja encima del estilo elegido; si el modo es
 * OFF no dibuja nada.
 */
@Composable
fun CustomPlayerBackground(thumbnailUrl: String?, alphaProvider: () -> Float = { 1f }) {
  val context = LocalContext.current
  val modeName by rememberPreference(CustomBgModeKey, CustomBgMode.OFF.name)
  val color by rememberPreference(CustomBgColorKey, DEFAULT_CUSTOM_COLOR)
  val gradStart by rememberPreference(CustomBgGradientStartKey, DEFAULT_GRADIENT_START)
  val gradEnd by rememberPreference(CustomBgGradientEndKey, DEFAULT_GRADIENT_END)
  val imagePath by rememberPreference(CustomBgImagePathKey, "")
  val blurDp by rememberPreference(CustomBgBlurKey, 40f)
  val dim by rememberPreference(CustomBgDimKey, 0.25f)

  val mode = CustomBgMode.fromName(modeName)
  if (mode == CustomBgMode.OFF) return

  Box(Modifier.fillMaxSize().graphicsLayer { alpha = alphaProvider() }) {
    when (mode) {
      CustomBgMode.COLOR -> Box(Modifier.fillMaxSize().background(Color(color)))
      CustomBgMode.GRADIENT ->
        Box(
          Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(gradStart), Color(gradEnd))))
        )
      CustomBgMode.COVER -> {
        if (thumbnailUrl != null) {
          AsyncImage(
            model =
              ImageRequest.Builder(context)
                .data(thumbnailUrl)
                .allowHardware(false)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(blurDp.dp)
          )
        } else {
          Box(Modifier.fillMaxSize().background(Color.Black))
        }
      }
      CustomBgMode.IMAGE -> {
        val file = if (imagePath.isNotEmpty()) File(imagePath) else null
        if (file != null && file.exists()) {
          AsyncImage(
            model = ImageRequest.Builder(context).data(file).allowHardware(false).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().blur(blurDp.dp)
          )
        } else {
          Box(Modifier.fillMaxSize().background(Color.Black))
        }
      }
      CustomBgMode.OFF -> Unit
    }
    // Oscurece para que el texto blanco se lea bien
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim.coerceIn(0f, 0.9f))))
  }
}
