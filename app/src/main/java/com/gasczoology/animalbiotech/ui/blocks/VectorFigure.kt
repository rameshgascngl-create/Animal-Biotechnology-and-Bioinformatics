package com.gasczoology.animalbiotech.ui.blocks

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import com.gasczoology.animalbiotech.i18n.Lang
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * SPIKE (not for merge). Draws a figure natively from its geometry (assets/vector/<id>/figure.svg, the same file the PNG
 * pipeline uses) plus its string table (strings.json). Supports only what the pilot uses: rect, circle, ellipse, line (+arrow
 * marker), path with M/A/L/Z, text. Labels are drawn with the system font, so Tamil shaping, fallback fonts and the user's
 * font scale apply; the figure is a single TalkBack node whose description is the figure's alt text.
 */
internal class VEl(val tag: String, val a: Map<String, String>, val dy: Float)
internal class VectorModel(val w: Float, val h: Float, val els: List<VEl>, val strings: JSONObject)

internal fun loadVector(ctx: Context, id: String): VectorModel {
    val strings = JSONObject(ctx.assets.open("vector/$id/strings.json").bufferedReader().readText())
    val p = android.util.Xml.newPullParser(); p.setInput(ctx.assets.open("vector/$id/figure.svg"), "UTF-8")
    val els = ArrayList<VEl>(); var dy = 0f; var w = 360f; var h = 800f; var inDefs = false
    var ev = p.eventType
    while (ev != XmlPullParser.END_DOCUMENT) {
        if (ev == XmlPullParser.START_TAG) {
            val m = HashMap<String, String>(); for (i in 0 until p.attributeCount) m[p.getAttributeName(i)] = p.getAttributeValue(i)
            when (p.name) {
                "svg" -> m["viewBox"]!!.split(" ").map { it.toFloat() }.let { w = it[2]; h = it[3] }
                "defs" -> inDefs = true
                "g" -> Regex("translate\\(([-\\d.]+),([-\\d.]+)\\)").find(m["transform"] ?: "")?.let { dy = it.groupValues[2].toFloat() }
                "rect", "circle", "ellipse", "line", "path", "text" -> if (!inDefs) els.add(VEl(p.name, m, dy))
            }
        } else if (ev == XmlPullParser.END_TAG && p.name == "defs") inDefs = false
        ev = p.next()
    }
    return VectorModel(w, h, els, strings)
}

private fun col(s: String?): Color? = if (s == null || s == "none") null else Color(android.graphics.Color.parseColor(s))
private fun VEl.f(k: String, d: Float = 0f) = a[k]?.toFloat() ?: d

/** SVG circular arc (rx = ry, no rotation) from (x1,y1) to (x2,y2) as an Android-style arc on [path]. */
private fun arc(path: Path, x1: Float, y1: Float, r: Float, large: Boolean, sweep: Boolean, x2: Float, y2: Float) {
    val dx = (x1 - x2) / 2; val dy = (y1 - y2) / 2; val d2 = dx * dx + dy * dy
    var rr = r; if (d2 > rr * rr) rr = sqrt(d2)
    val sq = sqrt(maxOf(0f, (rr * rr - d2) / d2)); val sgn = if (large == sweep) -1f else 1f
    val cx = sgn * sq * dy + (x1 + x2) / 2; val cy = -sgn * sq * dx + (y1 + y2) / 2
    val a1 = Math.toDegrees(atan2((y1 - cy).toDouble(), (x1 - cx).toDouble())).toFloat()
    val a2 = Math.toDegrees(atan2((y2 - cy).toDouble(), (x2 - cx).toDouble())).toFloat()
    var sw = a2 - a1; if (sweep && sw < 0) sw += 360f; if (!sweep && sw > 0) sw -= 360f
    path.arcTo(Rect(cx - rr, cy - rr, cx + rr, cy + rr), a1, sw, false)
}

private fun parsePath(d: String, k: Float, dy: Float): Path {
    val t = Regex("[MLAZ]|-?[\\d.]+").findAll(d).map { it.value }.toList(); val path = Path(); var i = 0; var cx = 0f; var cy = 0f
    fun n() = t[i++].toFloat()
    while (i < t.size) when (t[i++]) {
        "M" -> { cx = n(); cy = n(); path.moveTo(cx * k, (cy + dy) * k) }
        "L" -> { cx = n(); cy = n(); path.lineTo(cx * k, (cy + dy) * k) }
        "A" -> { val r = n(); n(); n(); val lg = n() != 0f; val sw = n() != 0f; val x = n(); val y = n()
            val q = Path(); arc(q, cx * k, (cy + dy) * k, r * k, lg, sw, x * k, (y + dy) * k); path.addPath(q); cx = x; cy = y }
        "Z" -> path.close()
    }
    return path
}

private fun DrawScope.paint(e: VEl, k: Float, draw: (fill: Boolean, stroke: Boolean, fc: Color?, sc: Color?, sw: Float, eff: PathEffect?) -> Unit) {
    val fc = col(e.a["fill"] ?: "#000000"); val sc = col(e.a["stroke"]); val sw = e.f("stroke-width", 1f) * k
    val eff = e.a["stroke-dasharray"]?.split(" ", ",")?.filter { it.isNotBlank() }?.map { it.toFloat() * k }?.toFloatArray()?.let { PathEffect.dashPathEffect(it) }
    draw(fc != null, sc != null, fc, sc, sw, eff)
}

@Composable
internal fun VectorFigure(id: String, lang: Lang, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val model = remember(id) { loadVector(ctx, id) }
    val tm = rememberTextMeasurer(); val density = LocalDensity.current
    val code = if (lang == Lang.TA) "ta" else "en"
    val alt = model.strings.optJSONObject("_alt")?.optString(code) ?: id
    Canvas(modifier.fillMaxWidth().aspectRatio(model.w / model.h).clearAndSetSemantics { contentDescription = alt }) {
        val k = size.width / model.w
        for (e in model.els) {
            val dy = e.dy
            when (e.tag) {
                "rect" -> paint(e, k) { f, s, fc, sc, sw, eff ->
                    val tl = Offset(e.f("x") * k, (e.f("y") + dy) * k); val sz = Size(e.f("width") * k, e.f("height") * k)
                    if (f) drawRect(fc!!, tl, sz, style = Fill); if (s) drawRect(sc!!, tl, sz, style = Stroke(sw, pathEffect = eff)) }
                "circle" -> paint(e, k) { f, s, fc, sc, sw, eff ->
                    val c = Offset(e.f("cx") * k, (e.f("cy") + dy) * k); val r = e.f("r") * k
                    if (f) drawCircle(fc!!, r, c); if (s) drawCircle(sc!!, r, c, style = Stroke(sw, pathEffect = eff)) }
                "ellipse" -> paint(e, k) { f, s, fc, sc, sw, eff ->
                    val tl = Offset((e.f("cx") - e.f("rx")) * k, (e.f("cy") + dy - e.f("ry")) * k); val sz = Size(2 * e.f("rx") * k, 2 * e.f("ry") * k)
                    if (f) drawOval(fc!!, tl, sz); if (s) drawOval(sc!!, tl, sz, style = Stroke(sw, pathEffect = eff)) }
                "path" -> paint(e, k) { _, s, _, sc, sw, eff ->
                    if (s) drawPath(parsePath(e.a["d"]!!, k, dy), sc!!, style = Stroke(sw, pathEffect = eff)) }
                "line" -> {
                    val x1 = e.f("x1"); val y1 = e.f("y1") + dy; val x2 = e.f("x2"); val y2 = e.f("y2") + dy
                    val sc = col(e.a["stroke"])!!; val sw = e.f("stroke-width", 1f)
                    drawLine(sc, Offset(x1 * k, y1 * k), Offset(x2 * k, y2 * k), sw * k)
                    if (e.a.containsKey("marker-end")) {
                        val ang = atan2(y2 - y1, x2 - x1); val L = 5f * sw; val tip = L / 2
                        fun pt(fw: Float, side: Float) = Offset((x2 + cos(ang) * fw - sin(ang) * side) * k, (y2 + sin(ang) * fw + cos(ang) * side) * k)
                        val ah = Path().apply { moveTo(pt(tip, 0f).x, pt(tip, 0f).y); lineTo(pt(-tip, -L / 2).x, pt(-tip, -L / 2).y); lineTo(pt(-tip, L / 2).x, pt(-tip, L / 2).y); close() }
                        drawPath(ah, sc)
                    } }
                "text" -> drawLabel(e, model, code, k, dy, tm, density.density, density.fontScale)
            }
        }
    }
}

private fun DrawScope.drawLabel(e: VEl, m: VectorModel, code: String, k: Float, dy: Float, tm: TextMeasurer, dens: Float, fs: Float) {
    val raw = e.a["data-num"] ?: m.strings.optJSONObject(e.a["data-k"] ?: return)?.optString(code) ?: return
    val units = when (e.a["class"]) { "h" -> 17f; "b" -> 14.5f; else -> 13.5f }
    // font size in sp = units * (figure width in dp / 360): follows the user's font scale like any system text
    val style = TextStyle(fontSize = (units * k / dens).sp, color = col(e.a["fill"]) ?: Color.Black,
        fontWeight = if (e.a["class"] == "h" || e.a["font-weight"] == "700") FontWeight.Bold else FontWeight.Normal)
    val maxW = (e.a["data-w"]?.toFloat() ?: 360f) * k
    // greedy wrap by measured width
    val lines = ArrayList<String>()
    for (para in raw.split("\n")) {
        var cur = ""
        for (word in para.split(" ")) { val t = if (cur.isEmpty()) word else "$cur $word"
            if (cur.isNotEmpty() && tm.measure(t, style, softWrap = false).size.width > maxW) { lines.add(cur); cur = word } else cur = t }
        lines.add(cur)
    }
    val x = e.f("x") * k; var y = (e.f("y") + dy) * k; val anchor = e.a["text-anchor"] ?: "start"
    for (ln in lines) {
        val r = tm.measure(ln, style, softWrap = false, constraints = Constraints())
        val left = when (anchor) { "middle" -> x - r.size.width / 2f; "end" -> x - r.size.width; else -> x }
        drawText(r, topLeft = Offset(left, y - r.firstBaseline)); y += r.size.height * 1.05f
    }
}
