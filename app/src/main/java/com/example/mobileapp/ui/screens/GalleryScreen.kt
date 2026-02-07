package com.example.mobileapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobileapp.ui.components.ZooCard
import com.example.mobileapp.ui.model.ZooEntry

@Composable
fun GalleryScreen(
    entries: List<ZooEntry>,
    onOpenCamera: () -> Unit,
    onOpenDetail: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("ZooDex", style = MaterialTheme.typography.headlineMedium)

            Button(onClick = onOpenCamera) {
                Text("Capture")
            }
        }

        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("You have no friends...")
            }
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { entry ->
                ZooCard(entry = entry, onClick = onOpenDetail)
            }
        }
    }
}
