package com.example.mobileapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val infinite = rememberInfiniteTransition(label = "rarity")

    // ── Shimmer sweep position (Rare + Epic) ────────────────────────────────
    val shimmerX by infinite.animateFloat(
        initialValue = -1f,
        targetValue  = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (entry.rarity == Rarity.EPIC) 1200 else 2000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    // ── Legendary rainbow hue rotation ─────────────────────────────────────
    val hue by infinite.animateFloat(
        initialValue = 0f,
        targetValue  = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )

    // ── Epic border pulse alpha ─────────────────────────────────────────────
    val epicBorderAlpha by infinite.animateFloat(
        initialValue = 0.6f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "epicAlpha"
    )

    // ── Legendary border colors ─────────────────────────────────────────────
    fun legendaryColors(offset: Float = 0f): List<Color> {
        val h = (hue + offset) % 360f
        return listOf(
            Color.hsv(h,            1f, 1f),
            Color.hsv((h + 60f)  % 360f, 1f, 1f),
            Color.hsv((h + 120f) % 360f, 1f, 1f),
            Color.hsv((h + 180f) % 360f, 1f, 1f),
            Color.hsv((h + 240f) % 360f, 1f, 1f),
            Color.hsv((h + 300f) % 360f, 1f, 1f),
            Color.hsv(h,            1f, 1f),
        )
    }

    // ── Shimmer overlay painter ─────────────────────────────────────────────
    val shimmerColor = when (entry.rarity) {
        Rarity.RARE -> Color(0xFF90CAF9).copy(alpha = 0.45f)
        Rarity.EPIC -> Color(0xFFCE93D8).copy(alpha = 0.55f)
        else        -> Color.Transparent
    }

    // ── Border modifier per rarity ──────────────────────────────────────────
    val borderMod: Modifier = when (entry.rarity) {
        Rarity.RARE ->
            Modifier.border(2.dp, Color(0xFF42A5F5).copy(alpha = 0.8f), shape)

        Rarity.EPIC ->
            Modifier.border(
                2.5.dp,
                Brush.sweepGradient(
                    listOf(
                        Color(0xFFAB47BC).copy(alpha = epicBorderAlpha),
                        Color(0xFF7B1FA2).copy(alpha = epicBorderAlpha),
                        Color(0xFFAB47BC).copy(alpha = epicBorderAlpha)
                    )
                ),
                shape
            )

        Rarity.LEGENDARY ->
            Modifier.border(
                3.dp,
                Brush.sweepGradient(legendaryColors()),
                shape
            )

        else -> Modifier
    }

    // ── Shimmer drawWithContent overlay ────────────────────────────────────
    val shimmerMod: Modifier = if (entry.rarity == Rarity.RARE || entry.rarity == Rarity.EPIC) {
        Modifier.drawWithContent {
            drawContent()
            val sweepX = shimmerX * size.width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        shimmerColor,
                        Color.Transparent
                    ),
                    start = Offset(sweepX - size.width * 0.3f, 0f),
                    end   = Offset(sweepX + size.width * 0.3f, size.height)
                )
            )
        }
    } else if (entry.rarity == Rarity.LEGENDARY) {
        Modifier.drawWithContent {
            drawContent()
            // Rainbow diagonal sweep
            val sweepX = shimmerX * size.width
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    start = Offset(sweepX - size.width * 0.25f, 0f),
                    end   = Offset(sweepX + size.width * 0.25f, size.height)
                )
            )
        }
    } else Modifier

    // ── Background tint per rarity ──────────────────────────────────────────
    val bgTint: Brush? = when (entry.rarity) {
        Rarity.RARE       -> Brush.verticalGradient(
            listOf(Color(0xFFE3F2FD), Color.White)
        )
        Rarity.EPIC       -> Brush.verticalGradient(
            listOf(Color(0xFFF3E5F5), Color.White)
        )
        Rarity.LEGENDARY  -> Brush.linearGradient(
            legendaryColors(offset = 180f).map { it.copy(alpha = 0.15f) }
        )
        else              -> null
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .then(borderMod)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        tonalElevation = 3.dp,
        shadowElevation = if (entry.rarity == Rarity.LEGENDARY) 12.dp else 6.dp,
        shape = shape
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .then(if (bgTint != null) Modifier.background(bgTint) else Modifier)
                .then(shimmerMod)
                .padding(14.dp)
        ) {
            // ID badge
            Text(
                text = "#%03d".format(entry.id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.BottomEnd)
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (entry.unlocked) {
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

                // Rarity chip — coloured per rarity
                val chipColor = when (entry.rarity) {
                    Rarity.COMMON    -> Color(0xFF78909C)
                    Rarity.RARE      -> Color(0xFF42A5F5)
                    Rarity.EPIC      -> Color(0xFFAB47BC)
                    Rarity.LEGENDARY -> Color.hsv(hue % 360f, 0.9f, 1f)
                }
                AssistChip(
                    onClick = { },
                    enabled = false,
                    label = { Text(entry.rarity.name, color = chipColor, fontWeight = FontWeight.Bold) }
                )


            }

            // Purple overlay for locked cards
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