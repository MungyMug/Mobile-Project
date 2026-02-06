package com.example.mobileapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*

import com.example.mobileapp.ui.screens.*

object Routes {
    const val GALLERY = "gallery"
    const val CAMERA = "camera"
    const val RESULT = "result"
    const val DETAIL = "detail"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.GALLERY
    ) {

        composable(Routes.GALLERY) {
            GalleryScreen(
                onOpenCamera = { nav.navigate(Routes.CAMERA) },
                onOpenDetail = { nav.navigate(Routes.DETAIL) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onBack = { nav.popBackStack() },
                onCaptured = { nav.navigate(Routes.RESULT) }
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                onSaveDone = {
                    nav.navigate(Routes.GALLERY) {
                        popUpTo(Routes.GALLERY) { inclusive = true }
                    }
                },
                onRetake = { nav.popBackStack() }
            )
        }

        composable(Routes.DETAIL) {
            DetailScreen(
                onBack = { nav.popBackStack() }
            )
        }
    }
}
