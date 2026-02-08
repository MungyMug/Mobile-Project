package com.example.mobileapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobileapp.ui.components.ZooCard
import com.example.mobileapp.ui.model.ZooEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    entries: List<ZooEntry>,
    onBack: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenDetail: (ZooEntry) -> Unit
) {
    val totalSlots = 12
    val captured = entries.count { it.unlocked }

    // Build exactly 12 slots: for each ID 1-12, show captured entry or locked placeholder
    val entryById = entries.associateBy { it.id }
    val slots = (1..totalSlots).map { id ->
        entryById[id] ?: ZooEntry(
            id = id,
            name = "???",
            animal = "❓",
            unlocked = false
        )
    }

    // background like your screenshot (purple-ish). You can change this to skyblue later.
    val bg = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Pets")
                        Text(
                            "Captured: $captured/$totalSlots",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←") // simple; swap for Icon if you want
                    }
                },
                actions = {
                    TextButton(onClick = onOpenCamera) { Text("Capture") }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bg)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(slots) { entry ->
                    ZooCard(
                        entry = entry,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = if (entry.unlocked) ({ onOpenDetail(entry) }) else null
                    )
                }
            }
        }
    }
}
