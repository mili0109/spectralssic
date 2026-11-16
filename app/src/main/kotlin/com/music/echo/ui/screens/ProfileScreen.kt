package echo.music.iad1tya.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.ProfileAvatarPathKey
import echo.music.iad1tya.constants.ProfileBannerPathKey
import echo.music.iad1tya.constants.ProfileBioKey
import echo.music.iad1tya.constants.ProfileNameKey
import echo.music.iad1tya.utils.LocalImageStore
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.ProfileViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
  val context = LocalContext.current
  var name by rememberPreference(ProfileNameKey, "")
  var bio by rememberPreference(ProfileBioKey, "")
  var avatarPath by rememberPreference(ProfileAvatarPathKey, "")
  var bannerPath by rememberPreference(ProfileBannerPathKey, "")
  val topArtists by viewModel.topArtists.collectAsState()

  val avatarPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) LocalImageStore.save(context, uri, "avatar")?.let { avatarPath = it }
    }
  val bannerPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) LocalImageStore.save(context, uri, "banner")?.let { bannerPath = it }
    }
  val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        }
      )
    }
  ) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
      // Banner + avatar
      Box(Modifier.fillMaxWidth().height(210.dp)) {
        val bannerFile = if (bannerPath.isNotEmpty()) File(bannerPath) else null
        if (bannerFile != null && bannerFile.exists()) {
          AsyncImage(
            model = bannerFile,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(150.dp)
          )
        } else {
          Box(
            Modifier.fillMaxWidth()
              .height(150.dp)
              .background(Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color.Black)))
          )
        }
        val avatarFile = if (avatarPath.isNotEmpty()) File(avatarPath) else null
        Box(
          Modifier.align(Alignment.BottomStart)
            .padding(start = 16.dp)
            .size(110.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          if (avatarFile != null && avatarFile.exists()) {
            AsyncImage(
              model = avatarFile,
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(onClick = { avatarPicker.launch(imageOnly) }) { Text("Cambiar foto") }
          OutlinedButton(onClick = { bannerPicker.launch(imageOnly) }) { Text("Cambiar banner") }
        }
        OutlinedButton(onClick = { navController.navigate("social") }, modifier = Modifier.fillMaxWidth()) {
          Text("Amigos, cuenta y perfil público/privado")
        }
        OutlinedButton(
          onClick = { navController.navigate("settings/appearance/player_background") },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Fondo del reproductor")
        }
        OutlinedTextField(
          value = name,
          onValueChange = { name = it.take(40) },
          label = { Text("Nombre") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = bio,
          onValueChange = { bio = it.take(160) },
          label = { Text("Descripción") },
          modifier = Modifier.fillMaxWidth()
        )

        Text("Mis artistas más escuchados", style = MaterialTheme.typography.titleMedium)
        if (topArtists.isEmpty()) {
          Text(
            "Aún no hay datos. Escucha música y aquí aparecerá tu top.",
            style = MaterialTheme.typography.bodyMedium
          )
        } else {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(topArtists, key = { _, a -> a.id }) { index, artist ->
              Column(Modifier.width(96.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                  model = artist.artist.thumbnailUrl,
                  contentDescription = null,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.size(88.dp).clip(CircleShape)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                  "${index + 1}. ${artist.artist.name}",
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 2,
                  textAlign = TextAlign.Center
                )
              }
            }
          }
        }
      }
    }
  }
}
