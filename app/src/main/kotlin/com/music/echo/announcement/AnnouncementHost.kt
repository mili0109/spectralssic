package echo.music.iad1tya.announcement

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import echo.music.iad1tya.SpectralssicConfig
import echo.music.iad1tya.constants.AnnouncementSeenKey
import echo.music.iad1tya.utils.rememberPreference
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Announcement(
  val id: Int,
  val title: String,
  val message: String,
  val buttonText: String,
  val url: String,
)

/**
 * Lee `anuncio.json` de TU repositorio de GitHub y muestra el aviso una sola vez.
 * Para mandar un anuncio: edita anuncio.json (activo=true y sube el "id" una unidad).
 */
private suspend fun fetchAnnouncement(): Announcement? =
  withContext(Dispatchers.IO) {
    try {
      val url =
        URL("https://raw.githubusercontent.com/${SpectralssicConfig.GITHUB_REPO}/main/anuncio.json")
      val conn = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 6000
        readTimeout = 6000
      }
      if (conn.responseCode != 200) return@withContext null
      val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
      if (!json.optBoolean("activo", false)) return@withContext null
      Announcement(
        id = json.optInt("id", 0),
        title = json.optString("titulo", "Spectralssic"),
        message = json.optString("mensaje", ""),
        buttonText = json.optString("boton", ""),
        url = json.optString("url", ""),
      )
    } catch (e: Exception) {
      null
    }
  }

@Composable
fun AnnouncementHost() {
  val uriHandler = LocalUriHandler.current
  var seen by rememberPreference(AnnouncementSeenKey, 0)
  var announcement by remember { mutableStateOf<Announcement?>(null) }

  LaunchedEffect(Unit) { announcement = fetchAnnouncement() }

  val a = announcement
  if (a != null && a.id > seen && a.message.isNotBlank()) {
    AlertDialog(
      onDismissRequest = { seen = a.id },
      title = { Text(a.title) },
      text = { Text(a.message) },
      confirmButton = {
        if (a.url.isNotBlank() && a.buttonText.isNotBlank()) {
          TextButton(onClick = { seen = a.id; uriHandler.openUri(a.url) }) { Text(a.buttonText) }
        } else {
          TextButton(onClick = { seen = a.id }) { Text("Entendido") }
        }
      },
      dismissButton = {
        if (a.url.isNotBlank() && a.buttonText.isNotBlank()) {
          TextButton(onClick = { seen = a.id }) { Text("Cerrar") }
        }
      }
    )
  }
}
