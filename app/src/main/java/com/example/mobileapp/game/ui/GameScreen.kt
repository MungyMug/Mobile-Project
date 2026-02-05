package com.example.mobileapp.game.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobileapp.game.sim.SandSimulation
import kotlinx.coroutines.delay

@Composable
fun GameScreen(onBack: () -> Unit) {
    // grid size (tune later)
    val sim = remember { SandSimulation(width = 120, height = 200) }
    var state by remember { mutableStateOf(sim.snapshot()) }
    var gameOver by remember { mutableStateOf(false) }
    var sandCount by remember { mutableStateOf(0) }

    // simulation loop
    LaunchedEffect(Unit) {
        var spawnCarry = 0f
        // create 2 holes near bottom
        sim.addHazardRect(10, 170, 40, 199)
        sim.addHazardRect(80, 170, 110, 199)

        while (true) {
            // dt approx 1/30 sec
            val dt = 1f / 30f

            // spawnRate grains per second
            val spawnRate = 20f
            spawnCarry += spawnRate * dt

            while (spawnCarry >= 1f) {
                spawnCarry -= 1f

                val x = sim.width / 2
                val y = 2

                // random color id (1..3)
                val color = (1..3).random()

                sim.addSand(x, y, color)
            }

            sim.step()

            sandCount = sim.sandCount()

            if (sandCount < 150) {
                gameOver = true
            }

            state = sim.snapshot()
            delay(33)

        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Sandbox")
            TextButton(onClick = { sim.clear()
                // simple reset: recreate sim by clearing grains
                // easiest hack: new sim instance
                // (better later: add sim.clear())
            }) { Text("Reset") }
        }

        if (gameOver) {
            Text(
                text = "GAME OVER",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        SandCanvas(
            state = state,
            modifier = Modifier.fillMaxSize(),
            onTapCell = { x, y -> }
        )
    }
}
