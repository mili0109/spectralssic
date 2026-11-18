package echo.music.iad1tya.social

import org.json.JSONArray
import org.json.JSONObject

/** Un "pin" del perfil: portada de un álbum o artista. */
data class Pin(val title: String, val subtitle: String, val cover: String, val kind: String)

object PinsStore {
  const val MAX_PINS = 30

  fun toJson(pins: List<Pin>): String {
    val arr = JSONArray()
    pins.forEach {
      arr.put(
        JSONObject()
          .put("title", it.title)
          .put("subtitle", it.subtitle)
          .put("cover", it.cover)
          .put("kind", it.kind)
      )
    }
    return arr.toString()
  }

  fun fromJson(json: String): List<Pin> =
    try {
      if (json.isBlank()) emptyList()
      else {
        val arr = JSONArray(json)
        (0 until arr.length()).map {
          val o = arr.getJSONObject(it)
          Pin(o.optString("title"), o.optString("subtitle"), o.optString("cover"), o.optString("kind"))
        }
      }
    } catch (e: Exception) {
      emptyList()
    }

  fun toMaps(pins: List<Pin>): List<Map<String, String>> =
    pins.map { mapOf("title" to it.title, "subtitle" to it.subtitle, "cover" to it.cover, "kind" to it.kind) }

  fun fromMaps(maps: List<Map<String, String>>): List<Pin> =
    maps.map { Pin(it["title"].orEmpty(), it["subtitle"].orEmpty(), it["cover"].orEmpty(), it["kind"].orEmpty()) }
}
