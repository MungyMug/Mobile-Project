package com.example.mobileapp.ui.nav

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.example.mobileapp.ui.model.*
import com.example.mobileapp.ui.screens.*
import com.google.android.gms.location.LocationServices
import java.util.Locale

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    // ── Persistent state ────────────────────────────────────────────────────
    val entries = remember {
        mutableStateListOf<ZooEntry>().apply { addAll(EntryStorage.load(context)) }
    }
    var currentStreak by remember { mutableIntStateOf(StreakStorage.getStreak(context)) }

    // ── Pending capture (set before navigating to Reveal/Result) ────────────
    var pendingAnimal    by remember { mutableStateOf<Animal?>(null) }
    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }

    // ── Location permission ─────────────────────────────────────────────────
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    LaunchedEffect(Unit) {
        if (!locationGranted) locationPermLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    fun nextAvailableId(): Int = entries.size + 1

    fun renumberEntries() {
        val renumbered = entries.sortedBy { it.id }.mapIndexed { index, entry ->
            entry.copy(id = index + 1)
        }
        entries.clear()
        entries.addAll(renumbered)
        EntryStorage.save(context, entries.toList())
    }

    fun resolveLocation(lat: Double, lng: Double): String = try {
        @Suppress("DEPRECATION")
        val results = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
        if (!results.isNullOrEmpty()) {
            val a = results[0]
            when {
                a.subLocality != null && a.locality != null -> "${a.subLocality}, ${a.locality}"
                a.subLocality != null                       -> a.subLocality
                a.thoroughfare != null && a.locality != null -> "${a.thoroughfare}, ${a.locality}"
                a.locality != null                          -> a.locality
                a.adminArea != null                         -> a.adminArea
                else                                        -> "%.4f, %.4f".format(lat, lng)
            }
        } else "%.4f, %.4f".format(lat, lng)
    } catch (_: Exception) { "%.4f, %.4f".format(lat, lng) }

    fun saveEntry(name: String, animal: Animal, photoPath: String?, locationLabel: String?) {
        val entry = ZooEntry(
            id        = nextAvailableId(),
            name      = name,
            animal    = animal.label(),
            rarity    = animal.rarity,
            unlocked  = true,
            photoPath = photoPath,
            location  = locationLabel
        )
        entries.add(entry)
        EntryStorage.save(context, entries.toList())
        currentStreak = StreakStorage.recordCapture(context)
    }

    fun addEntry(name: String, animal: Animal, photoPath: String?) {
        if (locationGranted) {
            try {
                LocationServices.getFusedLocationProviderClient(context).lastLocation
                    .addOnSuccessListener { loc ->
                        saveEntry(name, animal, photoPath,
                            if (loc != null) resolveLocation(loc.latitude, loc.longitude) else null)
                    }
                    .addOnFailureListener { saveEntry(name, animal, photoPath, null) }
            } catch (_: Exception) { saveEntry(name, animal, photoPath, null) }
        } else {
            saveEntry(name, animal, photoPath, null)
        }
    }

    // ── Nav graph ───────────────────────────────────────────────────────────
    NavHost(navController = nav, startDestination = Routes.MENU) {

        composable(Routes.MENU) {
            MainMenuScreen(
                streak    = currentStreak,
                onCapture = { nav.navigate(Routes.CAMERA) },
                onPets    = { nav.navigate(Routes.GALLERY) },
                onExit    = { activity?.finish() }
            )
        }

        composable(Routes.GALLERY) {
            GalleryScreen(
                entries      = entries,
                onBack       = { nav.navigate(Routes.MENU) { popUpTo(Routes.MENU) { inclusive = true } } },
                onOpenCamera = { nav.navigate(Routes.CAMERA) },
                onOpenDetail = { entry -> nav.navigate(Routes.detail(entry.id)) }
            )
        }

        composable(Routes.CAMERA) {
            CameraScreen(
                onBack  = { nav.navigate(Routes.MENU) { popUpTo(Routes.MENU) { inclusive = true } } },
                onSkip  = {
                    pendingAnimal    = AnimalPool.randomAnimal()
                    pendingPhotoPath = null
                    nav.navigate(Routes.REVEAL)
                },
                onCaptured = { photoPath, animal ->
                    pendingAnimal    = animal
                    pendingPhotoPath = photoPath
                    nav.navigate(Routes.REVEAL)
                }
            )
        }

        composable(Routes.REVEAL) {
            val animal = pendingAnimal ?: AnimalPool.randomAnimal().also { pendingAnimal = it }
            RevealScreen(
                animal     = animal,
                onContinue = { nav.navigate(Routes.RESULT) }
            )
        }

        composable(Routes.RESULT) {
            val animal = pendingAnimal ?: AnimalPool.randomAnimal()
            ResultScreen(
                animal      = animal,
                onSaveDone  = { name ->
                    addEntry(name, animal, pendingPhotoPath)
                    pendingAnimal    = null
                    pendingPhotoPath = null
                    nav.navigate(Routes.GALLERY) { popUpTo(Routes.MENU) { inclusive = false } }
                },
                onRetake = {
                    pendingAnimal    = null
                    pendingPhotoPath = null
                    nav.navigate(Routes.CAMERA) { popUpTo(Routes.CAMERA) { inclusive = true } }
                }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(androidx.navigation.navArgument("entryId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStack ->
            val entryId = backStack.arguments?.getInt("entryId") ?: 0
            val entry = entries.find { it.id == entryId }
            if (entry != null) {
                DetailScreen(
                    entry    = entry,
                    onBack   = { nav.popBackStack() },
                    onRelease = { released ->
                        released.photoPath?.let { try { java.io.File(it).delete() } catch (_: Exception) {} }
                        entries.remove(released)
                        renumberEntries()
                        nav.navigate(Routes.GALLERY) { popUpTo(Routes.GALLERY) { inclusive = false } }
                    }
                )
            } else {
                LaunchedEffect(Unit) { nav.popBackStack() }
            }
        }
    }
}