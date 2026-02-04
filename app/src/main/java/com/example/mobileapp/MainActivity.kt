package com.example.mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.mobileapp.game.ui.GameScreen
import com.example.mobileapp.game.ui.MainMenuScreen
import com.example.mobileapp.ui.theme.MobileAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MobileAppTheme {
                var inGame by remember { mutableStateOf(false) }

                if (!inGame) {
                    MainMenuScreen(onStart = { inGame = true })
                } else {
                    GameScreen(onBack = { inGame = false })
                }
            }
        }
    }
}
