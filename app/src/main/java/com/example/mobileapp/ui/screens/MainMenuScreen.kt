package com.example.mobileapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapp.ui.model.AnimalPool
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.scrollBy

@Composable
fun MainMenuScreen(
    streak: Int,
    onCapture: () -> Unit,
    onPets: () -> Unit,
    onExit: () -> Unit
) {
    val topBlue    = Color(0xFF81D4FA)
    val bottomBlue = Color(0xFF29B6F6)
    val bg         = Brush.verticalGradient(listOf(topBlue, bottomBlue))
    val emojis = remember { AnimalPool.allAnimals().map { it.emoji } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.6f))

        Text(
            text  = "PETSAFARI",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Gotta Catch Em' All!",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(Modifier.height(40.dp))

        // Center the roulette area
        Spacer(Modifier.weight(0.30f))
        EmojiRouletteStrip(emojis = emojis, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.weight(0.10f))

        // Streak badge — only shown if streak is active
        if (streak > 0) {
            Spacer(Modifier.height(18.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.22f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🔥", fontSize = 20.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$streak day streak!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(Modifier.weight(0.5f))

        MenuBtn(text = "Capture", filled = true,  onClick = onCapture)
        Spacer(Modifier.height(14.dp))
        MenuBtn(text = "Pets",    filled = true,  onClick = onPets)
        Spacer(Modifier.height(14.dp))
        MenuBtn(text = "Exit",    filled = false, onClick = onExit)

        Spacer(Modifier.weight(0.12f))
    }
}

@Composable
private fun MenuBtn(text: String, filled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp, max = 70.dp).clip(shape),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) Color.White else Color.Transparent,
            contentColor   = if (filled) Color(0xFF0288D1) else Color.White
        ),
        border = if (!filled) ButtonDefaults.outlinedButtonBorder(enabled = true) else null
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EmojiRouletteStrip(
    emojis: List<String>,
    modifier: Modifier = Modifier
) {
    if (emojis.isEmpty()) return

    val listSize = emojis.size
    val startIndex = Int.MAX_VALUE / 2
    val state = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    LaunchedEffect(Unit) {
        while (true) {
            state.scrollBy(6f)   // smoother slide
            delay(16)
        }
    }

    LazyRow(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp), // makes the strip visually centered/bigger
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(horizontal = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(count = Int.MAX_VALUE) { i ->
            val emoji = emojis[i % listSize]
            Text(
                text = emoji,
                fontSize = 122.sp // bigger emojis
            )
        }
    }
}