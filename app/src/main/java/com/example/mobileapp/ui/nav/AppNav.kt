package com.example.mobileapp.ui.nav

import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.example.mobileapp.ui.model.ZooEntry
import com.example.mobileapp.ui.screens.*
import kotlin.random.Random

object Routes {
    const val GALLERY = "gallery"
    const val CAMERA = "camera"
    const val RESULT = "result"
    const val DETAIL = "detail"
}

@Composable
fun AppNav() {

    val nav = rememberNavController()

    // ⭐ shared in-memory ZooDex list
    val entries = remember { mutableStateListOf<ZooEntry>() }

    // ⭐ helper to add new capture
    fun addEntry() {
        val animals = listOf("🦁 Lion", "🐼 Panda", "🐸 Frog", "🍌 Banana", "🍎 Apple")

        val newEntry = ZooEntry(
            id = entries.size + 1,
            name = "Friend ${entries.size + 1}",
            animal = animals.random()
        )

        entries.add(newEntry)
    }

    NavHost(
        navController = nav,
        startDestination = Routes.GALLERY
    ) {

        composable(Routes.GALLERY) {
            GalleryScreen(
                entries = entries,
                onOpenCamera = { nav.navigate(Routes.CAMERA) },
                onOpenDetail = { nav.navigate(Routes.DETAIL) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onBack = { nav.popBackStack() },
                onCaptured = {
                    addEntry()              // ⭐ add to ZooDex
                    nav.navigate(Routes.GALLERY) {
                        popUpTo(Routes.GALLERY) { inclusive = true }
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
