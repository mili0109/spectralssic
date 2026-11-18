package echo.music.iad1tya.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.AppIconColorKey
import echo.music.iad1tya.utils.IconUtils
import echo.music.iad1tya.utils.rememberPreference

private val IconBackgrounds =
  mapOf(
    "purple" to 0xFF0B0614, "blue" to 0xFF060B14, "pink" to 0xFF14060C,
    "green" to 0xFF06140A, "orange" to 0xFF140A06, "mono" to 0xFF000000,
  )

private fun fgRes(key: String): Int =
  when (key) {
    "blue" -> R.drawable.ic_fg_blue
    "pink" -> R.drawable.ic_fg_pink
    "green" -> R.drawable.ic_fg_green
    "orange" -> R.drawable.ic_fg_orange
    "mono" -> R.drawable.ic_fg_mono
    else -> R.drawable.ic_fg_purple
  }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpectralssicIconScreen(navController: NavController) {
  val context = LocalContext.current
  var selected by rememberPreference(AppIconColorKey, "purple")

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Ícono de la app", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        }
      )
    }
  ) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
      Text("Elige el color del ícono. El launcher puede tardar unos segundos en actualizarlo.")
      FlowRow(
        modifier = Modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        IconUtils.SpectralssicIcons.forEach { (key, label) ->
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              Modifier.size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(IconBackgrounds[key] ?: 0xFF000000))
                .border(
                  if (selected == key) 3.dp else 1.dp,
                  if (selected == key) MaterialTheme.colorScheme.primary else Color.Gray,
                  RoundedCornerShape(24.dp)
                )
                .clickable {
                  selected = key
                  IconUtils.setSpectralssicIcon(context, key)
                }
            ) {
              Image(painter = painterResource(fgRes(key)), contentDescription = label, modifier = Modifier.size(96.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
          }
        }
      }
    }
  }
}
