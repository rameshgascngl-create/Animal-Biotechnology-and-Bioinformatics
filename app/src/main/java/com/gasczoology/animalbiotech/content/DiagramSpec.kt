package com.gasczoology.animalbiotech.content

import org.json.JSONObject

/** One numbered marker on a diagram image. x and y are fractions (0..1) of the image width and height. */
data class Marker(val n: Int, val x: Float, val y: Float, val label: Bi)

data class DiagramSpec(val id: String, val aspect: Float, val markers: List<Marker>)

/** Parses assets/diagrams/<id>.json. Returns null on any malformed input so the caller can fall back. */
fun parseDiagramSpec(json: String): DiagramSpec? = try {
    val o = JSONObject(json)
    val w = o.getDouble("w").toFloat()
    val h = o.getDouble("h").toFloat()
    val arr = o.getJSONArray("markers")
    val ms = (0 until arr.length()).map { i ->
        val m = arr.getJSONObject(i)
        Marker(
            m.getInt("n"), m.getDouble("x").toFloat().coerceIn(0f, 1f), m.getDouble("y").toFloat().coerceIn(0f, 1f),
            Bi(m.getString("en"), m.optString("ta", ""), TaStatus.DRAFT),
        )
    }
    if (w <= 0f || h <= 0f) null else DiagramSpec(o.getString("id"), w / h, ms)
} catch (e: Exception) { null }
