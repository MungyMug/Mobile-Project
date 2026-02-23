package com.example.mobileapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileapp.ui.model.Animal
import com.example.mobileapp.ui.model.Rarity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val MAX_NAME = 14

@Composable
fun ResultScreen(
    animal: Animal,
    onSaveDone: (name: String) -> Unit,
    onRetake: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val suggestions = remember {
        listOf("Broski", "Chonk", "Sir", "Boss", "Bubu", "Baba",
            "Meowster", "Giga", "NPC", "Sigma", "Pookie", "Cutie",
            "Shadow", "Mochi", "Biscuit", "Pebble")
    }
    fun randomName() = suggestions.random()

    var name by remember { mutableStateOf(randomName()) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(250)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    val shake = remember { Animatable(0f) }
    fun triggerShake() {
        scope.launch {
            shake.snapTo(0f)
            repeat(5) {
                shake.animateTo(10f, tween(40))
                shake.animateTo(-10f, tween(40))
            }
            shake.animateTo(0f, tween(40))
        }
    }

    val rarityColor = when (animal.rarity) {
        Rarity.COMMON    -> MaterialTheme.colorScheme.onSurfaceVariant
        Rarity.RARE      -> androidx.compose.ui.graphics.Color(0xFF42A5F5)
        Rarity.EPIC      -> androidx.compose.ui.graphics.Color(0xFFAB47BC)
        Rarity.LEGENDARY -> androidx.compose.ui.graphics.Color(0xFFFFB300)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animal display
        Text(text = animal.emoji, fontSize = 72.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            text = animal.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = animal.rarity.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = rarityColor
        )

        Spacer(Modifier.height(32.dp))

        Text(
            "Give your new friend a nickname!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= MAX_NAME) name = it },
            label = { Text("Nickname") },
            singleLine = true,
            supportingText = { Text("${name.length}/$MAX_NAME") },
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shake.value.roundToInt(), 0) }
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { name = randomName() }) { Text("Randomize") }
            TextButton(onClick = onRetake) { Text("Retake") }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val trimmed = name.trim()
                if (trimmed.isBlank()) { triggerShake(); return@Button }
                keyboard?.hide()
                onSaveDone(trimmed)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("Save to Collection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}