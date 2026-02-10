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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val MAX_NAME = 10

@Composable
fun ResultScreen(
    onSaveDone: (String) -> Unit,
    onRetake: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val suggestions = remember {
        listOf(
            "Broski", "Chonk", "Sir", "Boss", "Bubu", "Baba",
            "Meowster", "Giga", "NPC", "Sigma", "Pookie", "Cutie"
        )
    }
    fun randomName() = suggestions.random()

    var name by remember { mutableStateOf(randomName()) }

    // ✅ autofocus
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        delay(250)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    // ✅ shake when invalid
    val shake = remember { Animatable(0f) }
    fun triggerShake() {
        scope.launch {
            shake.snapTo(0f)
            repeat(6) {
                shake.animateTo(10f, tween(40))
                shake.animateTo(-10f, tween(40))
            }
            shake.animateTo(0f, tween(40))
        }
    }

    val trimmed = name.trim()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Captured!", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(16.dp))

        Text(
            "Give your friend a nickname",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { input ->
                // ✅ limit length, allow emojis, keep spaces (we’ll trim on save)
                if (input.length <= MAX_NAME) name = input
            },
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
            TextButton(onClick = { name = randomName() }) {
                Text("Randomize")
            }
            TextButton(onClick = onRetake) {
                Text("Retake")
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val finalName = trimmed
                if (finalName.isBlank()) {
                    triggerShake()
                    return@Button
                }
                keyboard?.hide()
                onSaveDone(finalName)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Save to ZooDex")
        }
    }
}
