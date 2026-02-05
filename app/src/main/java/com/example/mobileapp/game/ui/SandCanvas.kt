package com.example.mobileapp.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.mobileapp.game.sim.SandState
import kotlin.math.min

@Composable
fun SandCanvas(
    state: SandState,
    modifier: Modifier = Modifier,
    onTapCell: (x: Int, y: Int) -> Unit = { _, _ -> } // not used now
) {
    Canvas(modifier = modifier) {
        drawSand(state)
    }
}

private fun DrawScope.drawSand(state: SandState) {

    val w = size.width
    val h = size.height

    val cellSize = min(w / state.width, h / state.height)

    val gridW = cellSize * state.width
    val gridH = cellSize * state.height

    val left = (w - gridW) / 2f
    val top = (h - gridH) / 2f

    val r = cellSize * 0.38f // grain radius

    for (y in 0 until state.height) {
        for (x in 0 until state.width) {

            val id = state.cell(x, y)
            if (id == 0) continue

            // 🎨 choose color by id
            val base = when (id) {
                1 -> Color(0xFFE6C15A) // yellow
                2 -> Color(0xFFE36A5D) // red
                3 -> Color(0xFF5DADE2) // blue
                else -> Color(0xFFE6C15A)
            }

            // stable jitter so grains look organic
            val j = jitter(x, y)
            val jx = (j.first - 0.5f) * cellSize * 0.35f
            val jy = (j.second - 0.5f) * cellSize * 0.35f

            val cx = left + (x + 0.5f) * cellSize + jx
            val cy = top + (y + 0.5f) * cellSize + jy

            // subtle brightness variation
            val shade =
                0.85f + 0.25f * ((x * 92821 + y * 68917) and 0xFF) / 255f

            val shaded = Color(
                base.red * shade,
                base.green * shade,
                base.blue * shade,
                0.95f
            )

            drawCircle(
                color = shaded,
                radius = r,
                center = Offset(cx, cy)
            )
        }
    }
}

/**
 * Deterministic pseudo-random jitter per cell
 * Makes sand look less grid-like
 */
private fun jitter(x: Int, y: Int): Pair<Float, Float> {
    var n = x * 374761393 + y * 668265263
    n = (n xor (n shr 13)) * 1274126177
    val a = (n and 0xFFFF) / 65535f
    val b = ((n shr 16) and 0xFFFF) / 65535f
    return a to b
}
