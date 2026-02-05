package com.example.mobileapp.game.sim

class SandSimulation(val width: Int, val height: Int) {

    // 0 = empty, 1..n = color id
    private val cells = ByteArray(width * height)
    // hazard cells (true = kills sand)
    private val hazards = BooleanArray(width * height)

    fun snapshot(): SandState =
        SandState(width, height, cells.copyOf())

    // now sand has a color!
    fun addSand(x: Int, y: Int, color: Int = 1) {
        if (x !in 0 until width || y !in 0 until height) return
        cells[y * width + x] = color.toByte()
    }

    fun step() {
        // bottom → top
        for (y in height - 2 downTo 0) {
            for (x in 0 until width) {

                val i = y * width + x
                val color = cells[i]

                if (color.toInt() == 0) continue

                if (hazards[i]) {
                    cells[i] = 0
                    continue
                }

                val below = (y + 1) * width + x

                // fall down
                if (cells[below].toInt() == 0) {
                    cells[i] = 0
                    cells[below] = color
                }
                else {
                    // diagonals
                    val leftOk =
                        x > 0 && cells[(y + 1) * width + (x - 1)].toInt() == 0

                    val rightOk =
                        x < width - 1 && cells[(y + 1) * width + (x + 1)].toInt() == 0

                    when {
                        leftOk -> {
                            cells[i] = 0
                            cells[(y + 1) * width + (x - 1)] = color
                        }
                        rightOk -> {
                            cells[i] = 0
                            cells[(y + 1) * width + (x + 1)] = color
                        }
                    }
                }
            }
        }
    }

    fun addHazardRect(x0: Int, y0: Int, x1: Int, y1: Int) {
        for (y in y0 until y1) {
            for (x in x0 until x1) {
                hazards[y * width + x] = true
            }
        }
    }

    fun clear() {
        cells.fill(0)
    }

    fun sandCount(): Int {
        return cells.count { it.toInt() != 0 }
    }
}
