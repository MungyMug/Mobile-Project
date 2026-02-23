package com.example.mobileapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapp.ui.model.Animal
import com.example.mobileapp.ui.model.Rarity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RevealScreen(
    animal: Animal,
    onContinue: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val rarityColor = when (animal.rarity) {
        Rarity.COMMON    -> Color(0xFF78909C)
        Rarity.RARE      -> Color(0xFF42A5F5)
        Rarity.EPIC      -> Color(0xFFAB47BC)
        Rarity.LEGENDARY -> Color(0xFFFFB300)
    }
    val rarityLabel = when (animal.rarity) {
        Rarity.COMMON    -> "COMMON"
        Rarity.RARE      -> "★  RARE  ★"
        Rarity.EPIC      -> "✦  EPIC  ✦"
        Rarity.LEGENDARY -> "👑  LEGENDARY  👑"
    }

    // ── Sequenced animation states ──────────────────────────────────────────
    val bgAlpha      = remember { Animatable(0f) }
    val emojiScale   = remember { Animatable(0f) }
    val badgeAlpha   = remember { Animatable(0f) }
    val badgeSlide   = remember { Animatable(60f) }
    val tapAlpha     = remember { Animatable(0f) }
    val screenAlpha  = remember { Animatable(1f) }
    var canTap by remember { mutableStateOf(false) }

    // ── Continuous animations ───────────────────────────────────────────────
    val infinite = rememberInfiniteTransition(label = "reveal")

    val sparkleRotation by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing)),
        label = "sparkle"
    )
    val tapPulse by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tap"
    )
    val legendaryHue by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "hue"
    )

    // Live rarity color (cycles for Legendary)
    val liveColor = if (animal.rarity == Rarity.LEGENDARY)
        Color.hsv(legendaryHue % 360f, 0.9f, 1f)
    else rarityColor

    // ── Run the sequence ────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        bgAlpha.animateTo(1f, tween(600, easing = FastOutSlowInEasing))

        // Emoji slams in with bounce
        launch {
            emojiScale.animateTo(1.45f, tween(220, easing = FastOutSlowInEasing))
            emojiScale.animateTo(0.82f, tween(110))
            emojiScale.animateTo(1.18f, tween(90))
            emojiScale.animateTo(0.94f, tween(75))
            emojiScale.animateTo(1f,    tween(75))
        }
        delay(300)

        // Badge slides up
        launch { badgeAlpha.animateTo(1f, tween(350)) }
        badgeSlide.animateTo(0f, tween(350, easing = FastOutSlowInEasing))

        delay(500)

        tapAlpha.animateTo(1f, tween(400))
        canTap = true
    }

    fun handleTap() {
        if (!canTap) return
        scope.launch {
            screenAlpha.animateTo(0f, tween(280))
            onContinue()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = screenAlpha.value }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { handleTap() },
        contentAlignment = Alignment.Center
    ) {
        // ── Dark base ───────────────────────────────────────────────────────
        Box(Modifier.fillMaxSize().background(Color(0xFF080808)))

        // ── Radial rarity glow ──────────────────────────────────────────────
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bgAlpha.value }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            liveColor.copy(alpha = 0.55f),
                            Color(0xFF080808)
                        ),
                        radius = 950f
                    )
                )
        )

        // ── Sparkle particles (Epic + Legendary only) ───────────────────────
        if (animal.rarity == Rarity.EPIC || animal.rarity == Rarity.LEGENDARY) {
            val particleCount  = if (animal.rarity == Rarity.LEGENDARY) 16 else 10
            val outerRadius    = if (animal.rarity == Rarity.LEGENDARY) 190f else 145f
            val particleSize   = if (animal.rarity == Rarity.LEGENDARY) 11f else 7f

            Canvas(
                modifier = Modifier
                    .size(420.dp)
                    .graphicsLayer { alpha = bgAlpha.value }
            ) {
                val cx = size.width / 2
                val cy = size.height / 2

                repeat(particleCount) { i ->
                    val base = (360f / particleCount) * i

                    // Outer ring — rotates clockwise
                    val angleOut = Math.toRadians((base + sparkleRotation).toDouble())
                    val px = cx + outerRadius * cos(angleOut).toFloat()
                    val py = cy + outerRadius * sin(angleOut).toFloat()
                    val pColor = if (animal.rarity == Rarity.LEGENDARY)
                        Color.hsv((legendaryHue + base) % 360f, 1f, 1f)
                    else Color(0xFFCE93D8)

                    drawCircle(pColor, radius = if (i % 2 == 0) particleSize else particleSize * 0.5f, center = Offset(px, py))

                    // Inner ring — rotates counter-clockwise
                    val angleIn = Math.toRadians((base - sparkleRotation * 1.4f).toDouble())
                    val ix = cx + (outerRadius * 0.52f) * cos(angleIn).toFloat()
                    val iy = cy + (outerRadius * 0.52f) * sin(angleIn).toFloat()
                    drawCircle(pColor.copy(alpha = 0.55f), radius = particleSize * 0.55f, center = Offset(ix, iy))
                }
            }
        }

        // ── Main content ────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Emoji
            Text(
                text = animal.emoji,
                fontSize = 108.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = emojiScale.value
                    scaleY = emojiScale.value
                }
            )

            Spacer(Modifier.height(28.dp))

            // Animal name
            Text(
                text = animal.name,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    alpha = badgeAlpha.value
                    translationY = badgeSlide.value
                }
            )

            Spacer(Modifier.height(14.dp))

            // Rarity badge
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = liveColor,
                modifier = Modifier.graphicsLayer {
                    alpha = badgeAlpha.value
                    translationY = badgeSlide.value
                }
            ) {
                Text(
                    text = rarityLabel,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(72.dp))

            // Tap hint
            Text(
                text = "Tap anywhere to continue",
                color = Color.White.copy(alpha = tapPulse * tapAlpha.value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}