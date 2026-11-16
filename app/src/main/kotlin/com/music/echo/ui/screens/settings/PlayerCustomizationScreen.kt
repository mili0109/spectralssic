package echo.music.iad1tya.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.CustomBgBlurKey
import echo.music.iad1tya.constants.CustomBgColorKey
import echo.music.iad1tya.constants.CustomBgDimKey
import echo.music.iad1tya.constants.CustomBgGradientEndKey
import echo.music.iad1tya.constants.CustomBgGradientStartKey
import echo.music.iad1tya.constants.CustomBgImagePathKey
import echo.music.iad1tya.constants.CustomBgModeKey
import echo.music.iad1tya.ui.component.CustomBgMode
import echo.music.iad1tya.ui.component.DEFAULT_CUSTOM_COLOR
import echo.music.iad1tya.ui.component.DEFAULT_GRADIENT_END
import echo.music.iad1tya.ui.component.DEFAULT_GRADIENT_START
import echo.music.iad1tya.utils.LocalImageStore
import echo.music.iad1tya.utils.rememberPreference

/** Paleta amplia de colores básicos y variados. */
val BackgroundPalette: List<Color> =
  listOf(
    0xFF000000, 0xFF111111, 0xFF1F1F1F, 0xFF424242, 0xFF757575, 0xFFBDBDBD, 0xFFFFFFFF,
    0xFF4A148C, 0xFF6A1B9A, 0xFF7C3AED, 0xFF8E24AA, 0xFFAB47BC, 0xFFCE93D8, 0xFFE1BEE7,
    0xFF311B92, 0xFF4527A0, 0xFF5E35B1, 0xFF7E57C2, 0xFF9575CD, 0xFFB39DDB,
    0xFF0D47A1, 0xFF1565C0, 0xFF1E88E5, 0xFF42A5F5, 0xFF90CAF9,
    0xFF006064, 0xFF00838F, 0xFF00ACC1, 0xFF26C6DA, 0xFF80DEEA,
    0xFF1B5E20, 0xFF2E7D32, 0xFF43A047, 0xFF66BB6A, 0xFFA5D6A7,
    0xFFF57F17, 0xFFFBC02D, 0xFFFFEB3B, 0xFFFFF59D,
    0xFFE65100, 0xFFEF6C00, 0xFFFB8C00, 0xFFFFB74D,
    0xFFB71C1C, 0xFFD32F2F, 0xFFED5564, 0xFFEF5350, 0xFFEF9A9A,
    0xFF880E4F, 0xFFC2185B, 0xFFE91E63, 0xFFF06292, 0xFFF8BBD0,
    0xFF3E2723, 0xFF5D4037, 0xFF8D6E63, 0xFFBCAAA4,
  ).map { Color(it) }

/** Degradados listos (inicio, fin). */
val GradientPresets: List<Pair<Color, Color>> =
  listOf(
    0xFF7C3AED to 0xFF000000,
    0xFFAB47BC to 0xFF311B92,
    0xFFE91E63 to 0xFF4A148C,
    0xFF1E88E5 to 0xFF4A148C,
    0xFF00ACC1 to 0xFF1A237E,
    0xFF43A047 to 0xFF004D40,
    0xFFFB8C00 to 0xFFB71C1C,
    0xFFFFEB3B to 0xFFE65100,
    0xFFEF5350 to 0xFF000000,
    0xFF424242 to 0xFF000000,
    0xFFF06292 to 0xFF7C3AED,
    0xFF26C6DA to 0xFF7C3AED,
  ).map { Color(it.first) to Color(it.second) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerCustomizationScreen(navController: NavController) {
  val context = LocalContext.current
  var modeName by rememberPreference(CustomBgModeKey, CustomBgMode.OFF.name)
  var color by rememberPreference(CustomBgColorKey, DEFAULT_CUSTOM_COLOR)
  var gradStart by rememberPreference(CustomBgGradientStartKey, DEFAULT_GRADIENT_START)
  var gradEnd by rememberPreference(CustomBgGradientEndKey, DEFAULT_GRADIENT_END)
  var imagePath by rememberPreference(CustomBgImagePathKey, "")
  var blur by rememberPreference(CustomBgBlurKey, 40f)
  var dim by rememberPreference(CustomBgDimKey, 0.25f)
  val mode = CustomBgMode.fromName(modeName)

  var pickingEnd by remember { mutableStateOf(false) }
  var hexText by remember { mutableStateOf("") }

  val imagePicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        LocalImageStore.save(context, uri, "player_bg")?.let {
          imagePath = it
          modeName = CustomBgMode.IMAGE.name
        }
      }
    }

  fun parseHex(text: String): Int? =
    try {
      val clean = text.trim().removePrefix("#")
      if (clean.length == 6) (0xFF000000 or clean.toLong(16)).toInt() else null
    } catch (e: Exception) {
      null
    }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Fondo del reproductor", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        }
      )
    }
  ) { padding ->
    Column(
      Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text("Tipo de fondo", style = MaterialTheme.typography.titleMedium)
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            CustomBgMode.OFF to "Normal",
            CustomBgMode.COVER to "Portada / blur",
            CustomBgMode.COLOR to "Color",
            CustomBgMode.GRADIENT to "Degradado",
            CustomBgMode.IMAGE to "Mi imagen",
          )
          .forEach { (m, label) ->
            FilterChip(
              selected = mode == m,
              onClick = { modeName = m.name },
              label = { Text(label) }
            )
          }
      }

      if (mode == CustomBgMode.COVER || mode == CustomBgMode.IMAGE) {
        Text("Intensidad del blur: ${blur.toInt()}", style = MaterialTheme.typography.titleMedium)
        Slider(value = blur, onValueChange = { blur = it }, valueRange = 0f..100f)
        Text(
          "0 = imagen nítida. El blur solo se ve en Android 12 o superior.",
          style = MaterialTheme.typography.bodySmall
        )
      }

      if (mode != CustomBgMode.OFF) {
        Text("Oscurecer fondo: ${(dim * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
        Slider(value = dim, onValueChange = { dim = it }, valueRange = 0f..0.8f)
      }

      if (mode == CustomBgMode.IMAGE) {
        Button(
          onClick = {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(if (imagePath.isEmpty()) "Elegir imagen" else "Cambiar imagen")
        }
      }

      if (mode == CustomBgMode.COLOR || mode == CustomBgMode.GRADIENT) {
        if (mode == CustomBgMode.GRADIENT) {
          Text("Degradados", style = MaterialTheme.typography.titleMedium)
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            GradientPresets.forEach { (a, b) ->
              Box(
                Modifier.size(56.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Brush.verticalGradient(listOf(a, b)))
                  .border(
                    if (gradStart == a.toArgb() && gradEnd == b.toArgb()) 3.dp else 0.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                  )
                  .clickable {
                    gradStart = a.toArgb()
                    gradEnd = b.toArgb()
                  }
              )
            }
          }
          Text("O crea el tuyo: elige qué extremo cambiar", style = MaterialTheme.typography.bodyMedium)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !pickingEnd, onClick = { pickingEnd = false }, label = { Text("Inicio") })
            FilterChip(selected = pickingEnd, onClick = { pickingEnd = true }, label = { Text("Fin") })
          }
        }

        Text("Colores", style = MaterialTheme.typography.titleMedium)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          BackgroundPalette.forEach { c ->
            val selected =
              if (mode == CustomBgMode.COLOR) color == c.toArgb()
              else (if (pickingEnd) gradEnd else gradStart) == c.toArgb()
            Box(
              Modifier.size(40.dp)
                .clip(CircleShape)
                .background(c)
                .border(
                  if (selected) 3.dp else 1.dp,
                  if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                  CircleShape
                )
                .clickable {
                  if (mode == CustomBgMode.COLOR) color = c.toArgb()
                  else if (pickingEnd) gradEnd = c.toArgb() else gradStart = c.toArgb()
                }
            )
          }
        }

        OutlinedTextField(
          value = hexText,
          onValueChange = {
            hexText = it
            parseHex(it)?.let { argb ->
              if (mode == CustomBgMode.COLOR) color = argb
              else if (pickingEnd) gradEnd = argb else gradStart = argb
            }
          },
          label = { Text("Color propio (hex, ej. #7C3AED)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }

      OutlinedButton(
        onClick = { navController.navigate("settings/appearance/theme") },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Cambiar el color de toda la app")
      }
    }
  }
}
