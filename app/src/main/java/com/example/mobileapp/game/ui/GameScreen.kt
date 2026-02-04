package com.example.mobileapp.game.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
@Composable
fun GameScreen(
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Game")
            Spacer(Modifier.width(48.dp))
        }

        // Later: Canvas sand simulation goes here
        Box(Modifier.fillMaxSize())
    }
}
