package com.example.mobileapp.ui.nav

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.mobileapp.ui.model.ZooEntry
import com.example.mobileapp.ui.screens.*
import com.example.mobileapp.ui.nav.Routes

@Composable
fun AppNav() {
    val nav = rememberNavController()

    // shared in-memory ZooDex list
    val entries = remember { mutableStateListOf<ZooEntry>() }

    // helper to add new capture
    fun addEntry() {
        val animals = listOf("🦁 Lion", "🐼 Panda", "🐸 Frog", "🍌 Banana", "🍎 Apple")
        entries.add(
            ZooEntry(
                id = entries.size + 1,
                name = "Friend ${entries.size + 1}",
                animal = animals.random()
            )
        )
    }

    NavHost(
        navController = nav,
        startDestination = Routes.MENU
    ) {

        composable(Routes.MENU) {
            MainMenuScreen(
                onCapture = { nav.navigate(Routes.CAMERA) },
                onPets = { nav.navigate(Routes.GALLERY) },
                onExit = { /* optional later */ }
            )
        }

        composable(Routes.GALLERY) {
            GalleryScreen(
                entries = entries,
                onBack = { nav.popBackStack() },
                onOpenCamera = { nav.navigate(Routes.CAMERA) },
                onOpenDetail = { nav.navigate(Routes.DETAIL) } // later: pass id
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onBack = { nav.popBackStack() },
                onCaptured = {
                    addEntry()
                    nav.navigate(Routes.GALLERY) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                onSaveDone = { nav.navigate(Routes.GALLERY) },
                onRetake = { nav.popBackStack() }
            )
        }

        composable(Routes.DETAIL) {
            DetailScreen(onBack = { nav.popBackStack() })
        }
    }
}
