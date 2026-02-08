package com.example.mobileapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mobileapp.ui.model.ZooEntry
import com.example.mobileapp.ui.model.Rarity
import java.io.File

@Composable
fun ZooCard(
    entry: ZooEntry,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        shape = shape
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(14.dp)
        ) {
            // id top-center like #001
            Text(
                text = "#%03d".format(entry.id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (entry.unlocked) {
                    // Show captured photo thumbnail or emoji fallback
                    if (entry.photoPath != null && File(entry.photoPath).exists()) {
                        AsyncImage(
                            model = File(entry.photoPath),
                            contentDescription = entry.name,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = entry.animal, style = MaterialTheme.typography.displaySmall)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    // locked look
                    Text(
                        text = "🦴",
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.alpha(0.25f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "???",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.alpha(0.45f)
                    )
                }

                Spacer(Modifier.height(6.dp))

                // rarity pill
                val label = entry.rarity.name
                AssistChip(
                    onClick = { },
                    enabled = false,
                    label = { Text(label) }
                )
            }

            // purple overlay for locked cards
            if (!entry.unlocked) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                )
            }
        }
    }
}
