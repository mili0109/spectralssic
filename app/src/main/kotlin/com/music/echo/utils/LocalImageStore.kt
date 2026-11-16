package echo.music.iad1tya.utils

import android.content.Context
import android.net.Uri
import java.io.File

/** Copia una imagen elegida por el usuario al almacenamiento privado de la app. */
object LocalImageStore {
  fun save(context: Context, uri: Uri, prefix: String): String? =
    try {
      val dir = File(context.filesDir, "custom_images").apply { mkdirs() }
      dir.listFiles()?.filter { it.name.startsWith(prefix) }?.forEach { it.delete() }
      val out = File(dir, "${prefix}_${System.currentTimeMillis()}.img")
      context.contentResolver.openInputStream(uri)?.use { input ->
        out.outputStream().use { input.copyTo(it) }
      } ?: return null
      out.absolutePath
    } catch (e: Exception) {
      null
    }
}
