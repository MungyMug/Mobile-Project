package com.example.mobileapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mobileapp.ui.model.Rarity
import com.example.mobileapp.ui.model.ZooEntry
import java.io.File

@Composable
fun DetailScreen(
    entry: ZooEntry,
    onBack: () -> Unit,
    onRelease: (ZooEntry) -> Unit
) {
    val topBlue = Color(0xFF81D4FA)
    val bottomBlue = Color(0xFF29B6F6)
    val bg = Brush.verticalGradient(listOf(topBlue, bottomBlue))

    var showConfirm by remember { mutableStateOf(false) }

    val rarityColor = when (entry.rarity) {
        Rarity.COMMON -> Color(0xFF78909C)
        Rarity.RARE -> Color(0xFF42A5F5)
        Rarity.EPIC -> Color(0xFFAB47BC)
        Rarity.LEGENDARY -> Color(0xFFFFB300)
    }

    val description = when {
        entry.animal.contains("Lion") -> "A majestic lion with a golden mane. Known for its powerful roar that can be heard from miles away."
        entry.animal.contains("Panda") -> "A gentle panda who loves munching on bamboo all day. Surprisingly fast when it wants to be!"
        entry.animal.contains("Frog") -> "A tiny tree frog with bright colors. Don't let its size fool you — it's full of energy!"
        entry.animal.contains("Banana") -> "A rare banana buddy found in the wild. Sweet personality and always happy to see you."
        entry.animal.contains("Apple") -> "A crispy apple critter that rolled right into your collection. Crunchy and full of surprises!"
        else -> "A mysterious creature you discovered on your safari adventure. Not much is known about it yet!"
    }

    // Release confirmation dialog
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Release ${entry.name}?") },
            text = { Text("Are you sure you want to release this pet back into the wild? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirm = false
                        onRelease(entry)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Release") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back", color = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Captured photo
        if (entry.photoPath != null && File(entry.photoPath).exists()) {
            AsyncImage(
                model = File(entry.photoPath),
                contentDescription = "Captured photo of ${entry.name}",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val emojiOnly = entry.animal.split(" ").firstOrNull() ?: entry.animal
                Text(text = emojiOnly, fontSize = 64.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Name
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        // Animal type
        val emoji = entry.animal.split(" ").firstOrNull() ?: ""
        val animalName = entry.animal.split(" ").drop(1).joinToString(" ")
        Text(
            text = "$emoji $animalName",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(Modifier.height(10.dp))

        // Rarity chip
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = rarityColor
        ) {
            Text(
                text = entry.rarity.name,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        // Description card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF455A64)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF0288D1)
            )
        ) {
            Text(
                text = "Back to Pets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        // Release button
        Button(
            onClick = { showConfirm = true },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Release Pet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
