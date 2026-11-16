package echo.music.iad1tya.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import echo.music.iad1tya.db.MusicDatabase
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ProfileViewModel @Inject constructor(database: MusicDatabase) : ViewModel() {
  /** Top de artistas de todos los tiempos, según el historial de reproducción. */
  val topArtists =
    database
      .mostPlayedArtists(fromTimeStamp = 0L, limit = 10)
      .map { list -> list.filter { it.artist.isYouTubeArtist } }
      .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
