package echo.music.iad1tya.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import echo.music.iad1tya.R
import echo.music.iad1tya.social.RemoteProfile
import echo.music.iad1tya.social.SocialRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(navController: NavController, uid: String) {
  var profile by remember { mutableStateOf<RemoteProfile?>(null) }
  var error by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(uid) {
    SocialRepository.loadProfile(uid)
      .onSuccess { profile = it }
      .onFailure { error = "No se pudo cargar el perfil (puede ser privado)" }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(profile?.name ?: "Perfil", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        }
      )
    }
  ) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
      val p = profile
      if (p == null) {
        Text(error ?: "Cargando…", Modifier.padding(16.dp))
      } else {
        val banner = remember(p.bannerB64) { SocialRepository.decodeImage(p.bannerB64) }
        val avatar = remember(p.avatarB64) { SocialRepository.decodeImage(p.avatarB64) }
        Box(Modifier.fillMaxWidth().height(210.dp)) {
          if (banner != null) {
            Image(
              bitmap = banner.asImageBitmap(),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxWidth().height(150.dp)
            )
          } else {
            Box(
              Modifier.fillMaxWidth().height(150.dp)
                .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color.Black)))
            )
          }
          Box(
            Modifier.align(Alignment.BottomStart).padding(start = 16.dp).size(110.dp).clip(CircleShape)
              .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            if (avatar != null) {
              Image(
                bitmap = avatar.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Icon(painterResource(R.drawable.person), contentDescription = null, modifier = Modifier.size(56.dp))
            }
          }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(p.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
          if (p.handle.isNotEmpty()) Text("@${p.handle}", style = MaterialTheme.typography.bodyMedium)
          if (p.bio.isNotEmpty()) Text(p.bio)

          p.nowPlaying?.let { np ->
            Text("Escuchando ahora", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
              AsyncImage(
                model = np["thumb"],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small)
              )
              Column {
                Text(np["title"] ?: "", fontWeight = FontWeight.SemiBold)
                Text(np["artist"] ?: "", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          if (p.pins.isNotEmpty()) {
            Text("Pines", style = MaterialTheme.typography.titleMedium)
            PinsGrid(echo.music.iad1tya.social.PinsStore.fromMaps(p.pins))
          }

          if (p.topArtists.isNotEmpty()) {
            Text("Artistas más escuchados", style = MaterialTheme.typography.titleMedium)
            p.topArtists.forEachIndexed { i, a ->
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                  model = a["thumb"],
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.size(48.dp).clip(CircleShape)
                )
                Text("${i + 1}. ${a["name"] ?: ""}")
              }
            }
          }
        }
      }
    }
  }
}
