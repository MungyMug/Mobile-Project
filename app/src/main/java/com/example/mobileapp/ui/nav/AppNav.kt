package com.example.mobileapp.ui.nav

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mobileapp.ui.model.EntryStorage
import com.example.mobileapp.ui.model.ZooEntry
import com.example.mobileapp.ui.screens.*

private const val TOTAL_SLOTS = 12

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    // Load saved entries on first launch
    val entries = remember {
        val saved = EntryStorage.load(context)
        mutableStateListOf<ZooEntry>().apply { addAll(saved) }
    }

    // Find the lowest available slot ID (1-12)
    fun nextAvailableId(): Int? {
        val usedIds = entries.map { it.id }.toSet()
        return (1..TOTAL_SLOTS).firstOrNull { it !in usedIds }
    }

    // Add a new pet to the lowest open slot
    fun addEntry(photoPath: String?) {
        val id = nextAvailableId() ?: return // all 12 slots full
        val animals = listOf("🦁 Lion", "🐼 Panda", "🐸 Frog", "🍌 Banana", "🍎 Apple")
        entries.add(
            ZooEntry(
                id = id,
                name = "Friend $id",
                animal = animals.random(),
                photoPath = photoPath
            )
        )
        EntryStorage.save(context, entries.toList())
    }

    NavHost(
        navController = nav,
        startDestination = Routes.MENU
    ) {

        composable(Routes.MENU) {
            MainMenuScreen(
                onCapture = { nav.navigate(Routes.CAMERA) },
                onPets = { nav.navigate(Routes.GALLERY) },
                onExit = { activity?.finish() }
            )
        }

        composable(Routes.GALLERY) {
            GalleryScreen(
                entries = entries,
                onBack = {
                    nav.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                },
                onOpenCamera = { nav.navigate(Routes.CAMERA) },
                onOpenDetail = { entry -> nav.navigate(Routes.detail(entry.id)) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onBack = {
                    nav.navigate(Routes.MENU) {
                        popUpTo(Routes.MENU) { inclusive = true }
                    }
                },
                onSkip = {
                    addEntry(null)
                    nav.navigate(Routes.GALLERY) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                },
                onCaptured = { photoPath ->
                    addEntry(photoPath)
                    nav.navigate(Routes.GALLERY) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                onSaveDone = { nav.navigate(Routes.GALLERY) },
                onRetake = {
                    nav.navigate(Routes.CAMERA) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getInt("entryId") ?: 0
            val entry = entries.find { it.id == entryId }
            if (entry != null) {
                DetailScreen(
                    entry = entry,
                    onBack = {
                        nav.navigate(Routes.GALLERY) {
                            popUpTo(Routes.MENU) { inclusive = false }
                        }
                    },
                    onRelease = { releasedEntry ->
                        releasedEntry.photoPath?.let { path ->
                            try { java.io.File(path).delete() } catch (_: Exception) {}
                        }
                        entries.remove(releasedEntry)
                        EntryStorage.save(context, entries.toList())
                        nav.navigate(Routes.GALLERY) {
                            popUpTo(Routes.MENU) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}
