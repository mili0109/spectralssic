package echo.music.iad1tya.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import echo.music.iad1tya.R
import echo.music.iad1tya.social.Pin
import echo.music.iad1tya.ui.utils.resize
import kotlinx.coroutines.delay

private val PinRatios = listOf(1f, 1.35f, 0.8f, 1.2f, 1.5f, 0.9f)

/** Cuadrícula estilo Pinterest: dos columnas con tarjetas de distinta altura. */
@Composable
fun PinsGrid(pins: List<Pin>, onRemove: ((Pin) -> Unit)? = null) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    for (col in 0..1) {
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pins.withIndex().filter { it.index % 2 == col }.forEach { (i, pin) ->
          PinCard(pin, PinRatios[(i + col * 3) % PinRatios.size], onRemove)
        }
      }
    }
  }
}

@Composable
private fun PinCard(pin: Pin, ratio: Float, onRemove: ((Pin) -> Unit)?) {
  Box(
    Modifier.fillMaxWidth().aspectRatio(1f / ratio).clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
  ) {
    AsyncImage(
      model = pin.cover,
      contentDescription = pin.title,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    Box(
      Modifier.align(Alignment.BottomStart).fillMaxWidth()
        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
        .padding(8.dp)
    ) {
      Column {
        Text(pin.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2,
          overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
        if (pin.subtitle.isNotBlank()) {
          Text(pin.subtitle, color = Color.White.copy(alpha = 0.8f), maxLines = 1,
            overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
        }
      }
    }
    if (onRemove != null) {
      IconButton(
        onClick = { onRemove(pin) },
        modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(32.dp)
          .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
      ) {
        Icon(painterResource(R.drawable.close), contentDescription = "Quitar", tint = Color.White,
          modifier = Modifier.size(16.dp))
      }
    }
  }
}

/** Busca álbumes o artistas en YouTube Music y deja elegir uno para el perfil. */
@Composable
fun PinSearchDialog(onDismiss: () -> Unit, onPick: (Pin) -> Unit) {
  var query by remember { mutableStateOf("") }
  var albums by remember { mutableStateOf(true) }
  var results by remember { mutableStateOf<List<Pin>>(emptyList()) }
  var loading by remember { mutableStateOf(false) }
  var failed by remember { mutableStateOf(false) }

  LaunchedEffect(query, albums) {
    if (query.length < 2) {
      results = emptyList()
      return@LaunchedEffect
    }
    delay(450)
    loading = true
    failed = false
    val filter = if (albums) YouTube.SearchFilter.FILTER_ALBUM else YouTube.SearchFilter.FILTER_ARTIST
    YouTube.search(query, filter)
      .onSuccess { r ->
        results =
          r.items.mapNotNull { item ->
            when (item) {
              is AlbumItem ->
                Pin(item.title, item.artists?.joinToString { it.name }.orEmpty(), item.thumbnail.resize(544, 544), "album")
              is ArtistItem ->
                item.thumbnail?.let { Pin(item.title, "Artista", it.resize(544, 544), "artist") }
              else -> null
            }
          }
      }
      .onFailure { failed = true; results = emptyList() }
    loading = false
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Agregar a tus pines") },
    text = {
      Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(selected = albums, onClick = { albums = true }, label = { Text("Álbumes") })
          FilterChip(selected = !albums, onClick = { albums = false }, label = { Text("Artistas") })
        }
        OutlinedTextField(
          value = query,
          onValueChange = { query = it },
          label = { Text(if (albums) "Nombre del álbum o artista" else "Nombre del artista") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        if (loading) Text("Buscando…", Modifier.padding(top = 8.dp))
        if (failed) Text("No se pudo buscar. Revisa tu conexión.", Modifier.padding(top = 8.dp))
        LazyColumn(Modifier.fillMaxWidth().height(300.dp).padding(top = 8.dp)) {
          items(results) { pin ->
            Row(
              Modifier.fillMaxWidth().clickable { onPick(pin) }.padding(vertical = 6.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              AsyncImage(
                model = pin.cover,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
              )
              Column {
                Text(pin.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(pin.subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
    },
    confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } }
  )
}
