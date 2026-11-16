package echo.music.iad1tya.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import echo.music.iad1tya.social.FriendItem
import echo.music.iad1tya.social.SocialRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SocialViewModel @Inject constructor() : ViewModel() {
  val uid =
    SocialRepository.uidFlow().stateIn(viewModelScope, SharingStarted.Eagerly, SocialRepository.currentUid)

  val friends =
    uid
      .flatMapLatest { if (it == null) flowOf(emptyList<FriendItem>()) else SocialRepository.friendsFlow(it) }
      .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val requests =
    uid
      .flatMapLatest { if (it == null) flowOf(emptyList<FriendItem>()) else SocialRepository.requestsFlow(it) }
      .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

  val message = MutableStateFlow<String?>(null)
  val busy = MutableStateFlow(false)

  private fun run(block: suspend () -> Result<*>, ok: String? = null) {
    viewModelScope.launch {
      busy.value = true
      val r = block()
      message.value = r.exceptionOrNull()?.let { it.message ?: "Error" } ?: ok
      busy.value = false
    }
  }

  fun signIn(email: String, pw: String) = run({ SocialRepository.signIn(email, pw) })

  fun signUp(email: String, pw: String) = run({ SocialRepository.signUp(email, pw) })

  fun signOut() = SocialRepository.signOut()

  fun claimHandle(handle: String, previous: String?, onDone: (String) -> Unit) {
    viewModelScope.launch {
      busy.value = true
      val r = SocialRepository.claimHandle(handle, previous)
      r.onSuccess { onDone(it); message.value = "Usuario guardado: @$it" }
      r.onFailure { message.value = it.message ?: "Error" }
      busy.value = false
    }
  }

  fun publish(
    name: String,
    bio: String,
    isPublic: Boolean,
    avatarPath: String?,
    bannerPath: String?,
    topArtists: List<Map<String, String>>,
  ) =
    run(
      { SocialRepository.publishProfile(name, bio, isPublic, avatarPath, bannerPath, topArtists) },
      "Perfil publicado"
    )

  fun addFriend(handle: String, myName: String) {
    viewModelScope.launch {
      busy.value = true
      val found = SocialRepository.findUidByHandle(handle)
      val r = found.getOrNull()?.let { SocialRepository.sendRequest(it, myName) } ?: Result.failure(found.exceptionOrNull() ?: Exception("No existe ese usuario"))
      message.value = r.exceptionOrNull()?.let { it.message ?: "Error" } ?: "Solicitud enviada"
      busy.value = false
    }
  }

  /** Busca un @usuario y abre su perfil (solo se ve si es público o ya son amigos). */
  fun openProfileByHandle(handle: String, onFound: (String) -> Unit) {
    viewModelScope.launch {
      busy.value = true
      SocialRepository.findUidByHandle(handle)
        .onSuccess { onFound(it) }
        .onFailure { message.value = it.message ?: "No existe ese usuario" }
      busy.value = false
    }
  }

  fun accept(f: FriendItem, myName: String) = run({ SocialRepository.acceptRequest(f.uid, f.name, myName) })

  fun decline(f: FriendItem) = run({ SocialRepository.declineRequest(f.uid) })

  fun remove(f: FriendItem) = run({ SocialRepository.removeFriend(f.uid) })
}
