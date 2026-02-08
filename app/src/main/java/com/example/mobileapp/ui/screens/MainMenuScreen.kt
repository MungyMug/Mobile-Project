package com.example.mobileapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuScreen(
    onCapture: () -> Unit,
    onPets: () -> Unit,
    onExit: () -> Unit
) {
    val topBlue = Color(0xFF81D4FA)
    val bottomBlue = Color(0xFF29B6F6)
    val bg = Brush.verticalGradient(listOf(topBlue, bottomBlue))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title section takes up ~35% of remaining space
        Spacer(Modifier.weight(0.2f))

        Text(
            text = "PETSAFARI",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "woof lets go",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )

        // Flexible space pushes buttons toward the bottom
        Spacer(Modifier.weight(0.5f))

        // Buttons section
        MenuBtn(text = "Capture", filled = true, onClick = onCapture)
        Spacer(Modifier.height(14.dp))
        MenuBtn(text = "Pets", filled = true, onClick = onPets)
        Spacer(Modifier.height(14.dp))
        MenuBtn(text = "Exit", filled = false, onClick = onExit)

        // Bottom breathing room — proportional, not fixed
        Spacer(Modifier.weight(0.12f))
    }
}

@Composable
private fun MenuBtn(
    text: String,
    filled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp, max = 70.dp)
            .clip(shape),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) Color.White else Color.Transparent,
            contentColor = if (filled) Color(0xFF0288D1) else Color.White
        ),
        border = if (!filled) ButtonDefaults.outlinedButtonBorder(enabled = true) else null
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
