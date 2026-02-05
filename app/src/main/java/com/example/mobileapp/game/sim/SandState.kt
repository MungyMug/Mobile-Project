package com.example.mobileapp.game.sim

data class SandState(
    val width: Int,
    val height: Int,
    val cells: ByteArray // 0=empty, 1..n = color id
) {
    fun index(x: Int, y: Int) = y * width + x
    fun inBounds(x: Int, y: Int) = x in 0 until width && y in 0 until height
    fun cell(x: Int, y: Int): Int = cells[index(x, y)].toInt() and 0xFF
    fun isEmpty(x: Int, y: Int) = cell(x, y) == 0
}
