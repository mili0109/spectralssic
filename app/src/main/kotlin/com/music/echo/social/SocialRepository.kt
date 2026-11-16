package echo.music.iad1tya.social

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class FriendItem(val uid: String, val name: String)

data class RemoteProfile(
  val uid: String,
  val name: String,
  val handle: String,
  val bio: String,
  val isPublic: Boolean,
  val avatarB64: String?,
  val bannerB64: String?,
  val topArtists: List<Map<String, String>>,
  val nowPlaying: Map<String, String>?,
)

/** Acceso a Firebase Auth + Firestore para perfil, amigos y "escuchando ahora". */
object SocialRepository {
  private val auth: FirebaseAuth
    get() = FirebaseAuth.getInstance()

  private val db: FirebaseFirestore
    get() = FirebaseFirestore.getInstance()

  val currentUid: String?
    get() = auth.currentUser?.uid

  fun uidFlow(): Flow<String?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
  }

  suspend fun signUp(email: String, password: String): Result<Unit> = runCatching {
    auth.createUserWithEmailAndPassword(email.trim(), password).await()
    Unit
  }

  suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
    auth.signInWithEmailAndPassword(email.trim(), password).await()
    Unit
  }

  fun signOut() = auth.signOut()

  /** Reclama un @usuario único. Libera el anterior si cambia. */
  suspend fun claimHandle(rawHandle: String, previous: String?): Result<String> = runCatching {
    val uid = currentUid ?: error("Inicia sesión primero")
    val handle = rawHandle.trim().lowercase().removePrefix("@")
    require(Regex("^[a-z0-9_]{3,20}$").matches(handle)) {
      "El usuario debe tener 3-20 letras, números o _"
    }
    val ref = db.collection("usernames").document(handle)
    val existing = ref.get().await()
    if (existing.exists() && existing.getString("uid") != uid) error("Ese usuario ya existe")
    ref.set(mapOf("uid" to uid)).await()
    if (!previous.isNullOrEmpty() && previous != handle) {
      runCatching { db.collection("usernames").document(previous).delete().await() }
    }
    db.collection("users").document(uid).set(mapOf("handle" to handle), SetOptions.merge()).await()
    handle
  }

  suspend fun publishProfile(
    name: String,
    bio: String,
    isPublic: Boolean,
    avatarPath: String?,
    bannerPath: String?,
    topArtists: List<Map<String, String>>,
  ): Result<Unit> = runCatching {
    val uid = currentUid ?: error("Inicia sesión primero")
    val data = mutableMapOf<String, Any?>(
      "name" to name,
      "bio" to bio,
      "isPublic" to isPublic,
      "topArtists" to topArtists,
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    encodeImage(avatarPath, 256, 256)?.let { data["avatarB64"] = it }
    encodeImage(bannerPath, 900, 300)?.let { data["bannerB64"] = it }
    db.collection("users").document(uid).set(data, SetOptions.merge()).await()
  }

  suspend fun updateNowPlaying(title: String, artist: String, thumb: String?) {
    val uid = currentUid ?: return
    runCatching {
      db.collection("users")
        .document(uid)
        .set(
          mapOf(
            "nowPlaying" to
              mapOf("title" to title, "artist" to artist, "thumb" to (thumb ?: ""), "at" to System.currentTimeMillis().toString())
          ),
          SetOptions.merge()
        )
        .await()
    }
  }

  suspend fun clearNowPlaying() {
    val uid = currentUid ?: return
    runCatching {
      db.collection("users").document(uid).update("nowPlaying", FieldValue.delete()).await()
    }
  }

  suspend fun findUidByHandle(rawHandle: String): Result<String> = runCatching {
    val handle = rawHandle.trim().lowercase().removePrefix("@")
    val snap = db.collection("usernames").document(handle).get().await()
    snap.getString("uid") ?: error("No existe ese usuario")
  }

  suspend fun sendRequest(targetUid: String, myName: String): Result<Unit> = runCatching {
    val uid = currentUid ?: error("Inicia sesión primero")
    require(uid != targetUid) { "No puedes agregarte a ti mismo" }
    db.collection("users")
      .document(targetUid)
      .collection("requests")
      .document(uid)
      .set(mapOf("fromUid" to uid, "fromName" to myName, "createdAt" to FieldValue.serverTimestamp()))
      .await()
  }

  suspend fun acceptRequest(fromUid: String, fromName: String, myName: String): Result<Unit> =
    runCatching {
      val uid = currentUid ?: error("Inicia sesión primero")
      val batch = db.batch()
      val users = db.collection("users")
      batch.set(users.document(uid).collection("friends").document(fromUid), mapOf("name" to fromName))
      batch.set(users.document(fromUid).collection("friends").document(uid), mapOf("name" to myName))
      batch.delete(users.document(uid).collection("requests").document(fromUid))
      batch.commit().await()
    }

  suspend fun declineRequest(fromUid: String): Result<Unit> = runCatching {
    val uid = currentUid ?: error("Inicia sesión primero")
    db.collection("users").document(uid).collection("requests").document(fromUid).delete().await()
  }

  suspend fun removeFriend(friendUid: String): Result<Unit> = runCatching {
    val uid = currentUid ?: error("Inicia sesión primero")
    val batch = db.batch()
    val users = db.collection("users")
    batch.delete(users.document(uid).collection("friends").document(friendUid))
    batch.delete(users.document(friendUid).collection("friends").document(uid))
    batch.commit().await()
  }

  fun friendsFlow(uid: String): Flow<List<FriendItem>> = callbackFlow {
    val reg =
      db.collection("users").document(uid).collection("friends").addSnapshotListener { snap, _ ->
        trySend(snap?.documents?.map { FriendItem(it.id, it.getString("name") ?: "Usuario") } ?: emptyList())
      }
    awaitClose { reg.remove() }
  }

  fun requestsFlow(uid: String): Flow<List<FriendItem>> = callbackFlow {
    val reg =
      db.collection("users").document(uid).collection("requests").addSnapshotListener { snap, _ ->
        trySend(snap?.documents?.map { FriendItem(it.id, it.getString("fromName") ?: "Usuario") } ?: emptyList())
      }
    awaitClose { reg.remove() }
  }

  suspend fun loadProfile(uid: String): Result<RemoteProfile> = runCatching {
    val d = db.collection("users").document(uid).get().await()
    if (!d.exists()) error("Perfil no encontrado")
    @Suppress("UNCHECKED_CAST")
    RemoteProfile(
      uid = uid,
      name = d.getString("name") ?: "Usuario",
      handle = d.getString("handle") ?: "",
      bio = d.getString("bio") ?: "",
      isPublic = d.getBoolean("isPublic") ?: false,
      avatarB64 = d.getString("avatarB64"),
      bannerB64 = d.getString("bannerB64"),
      topArtists = (d.get("topArtists") as? List<Map<String, String>>) ?: emptyList(),
      nowPlaying = d.get("nowPlaying") as? Map<String, String>,
    )
  }

  private fun encodeImage(path: String?, maxW: Int, maxH: Int): String? {
    if (path.isNullOrEmpty()) return null
    val file = File(path)
    if (!file.exists()) return null
    return try {
      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      BitmapFactory.decodeFile(path, bounds)
      var sample = 1
      while (bounds.outWidth / sample > maxW * 2 || bounds.outHeight / sample > maxH * 2) sample *= 2
      val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return null
      val scale = minOf(maxW.toFloat() / bmp.width, maxH.toFloat() / bmp.height, 1f)
      val scaled =
        if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt().coerceAtLeast(1), (bmp.height * scale).toInt().coerceAtLeast(1), true)
        else bmp
      val out = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
      Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
      null
    }
  }

  fun decodeImage(b64: String?): Bitmap? =
    if (b64.isNullOrEmpty()) null
    else
      try {
        val bytes = Base64.decode(b64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
      } catch (e: Exception) {
        null
      }
}
