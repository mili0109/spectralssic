package echo.music.iad1tya.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import echo.music.iad1tya.R
import echo.music.iad1tya.constants.ProfileAvatarPathKey
import echo.music.iad1tya.constants.ProfileBannerPathKey
import echo.music.iad1tya.constants.ProfileBioKey
import echo.music.iad1tya.constants.ProfileHandleKey
import echo.music.iad1tya.constants.ProfileNameKey
import echo.music.iad1tya.constants.PinsJsonKey
import echo.music.iad1tya.constants.ProfilePublicKey
import echo.music.iad1tya.constants.ShareListeningKey
import echo.music.iad1tya.utils.rememberPreference
import echo.music.iad1tya.viewmodels.ProfileViewModel
import echo.music.iad1tya.viewmodels.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
  navController: NavController,
  viewModel: SocialViewModel = hiltViewModel(),
  profileViewModel: ProfileViewModel = hiltViewModel(),
) {
  val uid by viewModel.uid.collectAsState()
  val friends by viewModel.friends.collectAsState()
  val requests by viewModel.requests.collectAsState()
  val message by viewModel.message.collectAsState()
  val busy by viewModel.busy.collectAsState()
  val topArtists by profileViewModel.topArtists.collectAsState()

  val name by rememberPreference(ProfileNameKey, "")
  val bio by rememberPreference(ProfileBioKey, "")
  val avatarPath by rememberPreference(ProfileAvatarPathKey, "")
  val bannerPath by rememberPreference(ProfileBannerPathKey, "")
  var isPublic by rememberPreference(ProfilePublicKey, false)
  var handle by rememberPreference(ProfileHandleKey, "")
  var shareListening by rememberPreference(ShareListeningKey, false)
  val pinsJson by rememberPreference(PinsJsonKey, "")

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var handleInput by remember { mutableStateOf(handle) }
  var friendInput by remember { mutableStateOf("") }
  val myName = name.ifBlank { handle.ifBlank { "Usuario" } }

  LaunchedEffect(handle) { if (handleInput.isEmpty()) handleInput = handle }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Amigos y perfil online", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = { navController.navigateUp() }) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
          }
        }
      )
    }
  ) { padding ->
    Column(
      Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

      if (uid == null) {
        Text("Inicia sesión o crea una cuenta", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Correo") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Contraseña (mínimo 6)") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(enabled = !busy, onClick = { viewModel.signIn(email, password) }) { Text("Entrar") }
          OutlinedButton(enabled = !busy, onClick = { viewModel.signUp(email, password) }) {
            Text("Crear cuenta")
          }
        }
      } else {
        Text("Mi perfil online", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
          value = handleInput,
          onValueChange = { handleInput = it },
          label = { Text("Usuario único (@)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        if (handle.isEmpty()) {
          Text(
            "Primero guarda tu usuario: sin él nadie podrá encontrarte.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
          )
        } else {
          Text("Tu usuario: @$handle", style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(
          enabled = !busy,
          onClick = { viewModel.claimHandle(handleInput, handle) { handle = it } }
        ) {
          Text("Guardar usuario")
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f)) {
            Text(if (isPublic) "Perfil público" else "Perfil privado")
            Text(
              if (isPublic) "Cualquiera con tu usuario puede ver tu perfil"
              else "Solo tus amigos pueden ver tu perfil",
              style = MaterialTheme.typography.bodySmall
            )
          }
          Switch(checked = isPublic, onCheckedChange = { isPublic = it })
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f)) {
            Text("Compartir lo que escucho")
            Text("Tus amigos verán la canción actual", style = MaterialTheme.typography.bodySmall)
          }
          Switch(checked = shareListening, onCheckedChange = { shareListening = it })
        }
        Button(
          enabled = !busy,
          onClick = {
            viewModel.publishWithHandle(
              handleInput = handleInput,
              savedHandle = handle,
              onHandle = { handle = it },
              pins = echo.music.iad1tya.social.PinsStore.toMaps(echo.music.iad1tya.social.PinsStore.fromJson(pinsJson)),
              name = name,
              bio = bio,
              isPublic = isPublic,
              avatarPath = avatarPath,
              bannerPath = bannerPath,
              topArtists =
                topArtists.map {
                  mapOf("name" to it.artist.name, "thumb" to (it.artist.thumbnailUrl ?: ""))
                }
            )
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Publicar mi perfil")
        }
        OutlinedButton(onClick = { navController.navigate("profile") }, modifier = Modifier.fillMaxWidth()) {
          Text("Editar foto, banner y datos")
        }

        Text("Agregar amigo", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = friendInput,
            onValueChange = { friendInput = it },
            label = { Text("@usuario") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          Button(enabled = !busy, onClick = { viewModel.addFriend(friendInput, myName) }) { Text("Agregar") }
        }
        OutlinedButton(
          enabled = !busy && friendInput.isNotBlank(),
          onClick = { viewModel.openProfileByHandle(friendInput) { navController.navigate("friend/$it") } },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Ver perfil de @usuario")
        }

        if (requests.isNotEmpty()) {
          Text("Solicitudes", style = MaterialTheme.typography.titleMedium)
          requests.forEach { r ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
              Text(r.name, Modifier.weight(1f))
              OutlinedButton(onClick = { viewModel.decline(r) }) { Text("Rechazar") }
              Button(onClick = { viewModel.accept(r, myName) }, modifier = Modifier.padding(start = 8.dp)) {
                Text("Aceptar")
              }
            }
          }
        }

        Text("Mis amigos (${friends.size})", style = MaterialTheme.typography.titleMedium)
        friends.forEach { f ->
          Row(
            Modifier.fillMaxWidth().clickable { navController.navigate("friend/${f.uid}") }.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(f.name, Modifier.weight(1f))
            OutlinedButton(onClick = { viewModel.remove(f) }) { Text("Quitar") }
          }
        }

        OutlinedButton(onClick = { viewModel.signOut() }, modifier = Modifier.fillMaxWidth()) {
          Text("Cerrar sesión")
        }
      }
    }
  }
}
