package com.example.mobileapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mobileapp.ui.model.Rarity
import com.example.mobileapp.ui.model.ZooEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private const val GEMINI_API_KEY = "YOUR_GEMINI_KEY_HERE" // ⚠️ Replace with your key

// Returns Pair(description, isFromApi)
private suspend fun fetchDescription(animal: String, nickname: String, rarity: Rarity): Pair<String, Boolean> {
    val animalName = animal.split(" ").drop(1).joinToString(" ").ifBlank { animal }
    val rarityLabel = rarity.name.lowercase()

    val prompt = """
        You are writing a fun, witty flavour text for a pet safari game.
        The player just captured a $rarityLabel-rarity creature called "$animalName" and nicknamed it "$nickname".
        Write exactly 2-3 sentences describing this pet's personality and what makes it special.
        Keep it playful, slightly humorous, and end with something that makes the player feel lucky to have it.
        Do not start with the animal's name. Do not use quotes. Just return the description text.
    """.trimIndent()

    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=$GEMINI_API_KEY")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10_000
            conn.readTimeout = 15_000

            val body = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(
                            JSONObject().apply {
                                put("text", prompt)
                            }
                        ))
                    }
                ))
            }

            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            val responseCode = conn.responseCode
            if (responseCode != 200) {
                val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "no error body"
                return@withContext Pair("API error $responseCode: $errorBody", false)
            }

            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            val text = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
            Pair(text, true)
        } catch (e: Exception) {
            Pair(generateDescription(animal, nickname, rarity), false)
        }
    }
}

private fun generateDescription(animal: String, nickname: String, rarity: Rarity): String {
    val animalName = animal.split(" ").drop(1).joinToString(" ").ifBlank { animal }

    val rarityIntro = when (rarity) {
        Rarity.COMMON -> listOf(
            "A well-known face in the wild,",
            "Often spotted in the neighbourhood,",
            "A familiar creature to many,",
            "Not the rarest find, but beloved by all,",
            "A classic safari encounter,"
        )
        Rarity.RARE -> listOf(
            "Seldom seen by human eyes,",
            "A lucky encounter on the safari trail,",
            "Only a few have ever witnessed this creature,",
            "Spotted once in a blue moon,",
            "Travellers search for years to find one,"
        )
        Rarity.EPIC -> listOf(
            "Legends whisper of this beast,",
            "Ancient texts describe this creature with awe,",
            "Few survive an encounter — even fewer capture one,",
            "The stuff of myths and campfire stories,",
            "Even seasoned explorers go pale at the sight,"
        )
        Rarity.LEGENDARY -> listOf(
            "Once thought to exist only in dreams,",
            "The rarest discovery in safari history,",
            "Scholars have debated its existence for centuries,",
            "A single sighting sends shockwaves through the scientific world,",
            "No living explorer had seen one — until now,"
        )
    }.random()

    val traits = listOf(
        "Known for its surprisingly strong opinions about snacks.",
        "Has a habit of staring directly into your soul at 3am.",
        "Experts believe it can sense WiFi signals.",
        "Unusually fast when motivated by food.",
        "Will absolutely judge you for your music taste.",
        "Sleeps 18 hours a day and has zero regrets.",
        "Experts estimate it has outsmarted at least three scientists.",
        "Communicates exclusively through aggressive eye contact.",
        "Has never lost a staring contest. Not once.",
        "Rumoured to hoard shiny objects under its bed.",
        "Surprisingly good at parallel parking.",
        "Known to disappear the moment you need it most.",
        "Has a signature move that no one has been able to replicate.",
        "Scientists are baffled by its ability to always find the comfiest spot.",
        "Described by locals as 'disturbingly confident'.",
        "Once outsmarted a vending machine. Nobody knows how.",
        "Has a surprisingly extensive vocabulary of grumbling sounds.",
        "Believed to have strong opinions about interior design."
    ).random()

    val bond = listOf(
        "You and $nickname share a bond that defies explanation.",
        "$nickname chose you — and that means something.",
        "Consider yourself truly lucky to have $nickname by your side.",
        "Out of everyone in the world, $nickname found you.",
        "The safari gods clearly had you in mind when they sent $nickname.",
        "Treat $nickname well — creatures like this don't come around twice."
    ).random()

    return "$rarityIntro the $animalName is a creature of extraordinary character. $traits $bond"
}

@Composable
fun DetailScreen(
    entry: ZooEntry,
    onBack: () -> Unit,
    onRelease: (ZooEntry) -> Unit
) {
    val topBlue = Color(0xFF81D4FA)
    val bottomBlue = Color(0xFF29B6F6)
    val bg = Brush.verticalGradient(listOf(topBlue, bottomBlue))

    var showConfirm by remember { mutableStateOf(false) }

    val rarityColor = when (entry.rarity) {
        Rarity.COMMON -> Color(0xFF78909C)
        Rarity.RARE -> Color(0xFF42A5F5)
        Rarity.EPIC -> Color(0xFFAB47BC)
        Rarity.LEGENDARY -> Color(0xFFFFB300)
    }

    var description by remember(entry.id) { mutableStateOf<String?>(null) }
    var descriptionFromApi by remember(entry.id) { mutableStateOf(false) }
    LaunchedEffect(entry.id) {
        val (text, fromApi) = fetchDescription(entry.animal, entry.name, entry.rarity)
        description = text
        descriptionFromApi = fromApi
    }

    // Release confirmation dialog
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Release ${entry.name}?") },
            text = { Text("Are you sure you want to release this pet back into the wild? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirm = false
                        onRelease(entry)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Release") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back", color = Color.White)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Captured photo
        if (entry.photoPath != null && File(entry.photoPath).exists()) {
            AsyncImage(
                model = File(entry.photoPath),
                contentDescription = "Captured photo of ${entry.name}",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val emojiOnly = entry.animal.split(" ").firstOrNull() ?: entry.animal
                Text(text = emojiOnly, fontSize = 64.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Name
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        // Animal type
        val emoji = entry.animal.split(" ").firstOrNull() ?: ""
        val animalName = entry.animal.split(" ").drop(1).joinToString(" ")
        Text(
            text = "$emoji $animalName",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(Modifier.height(10.dp))

        // Rarity chip
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = rarityColor
        ) {
            Text(
                text = entry.rarity.name,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if(entry.location != null) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.25f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📍", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = entry.location,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Description card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
                Spacer(Modifier.height(12.dp))
                if (description == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = Color(0xFF0288D1),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = description!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF455A64)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (descriptionFromApi) "✨ AI Generated" else "⚠️ Offline (local)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (descriptionFromApi) Color(0xFF0288D1) else Color(0xFFE57373),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF0288D1)
            )
        ) {
            Text(
                text = "Back to Pets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(14.dp))

        // Release button
        Button(
            onClick = { showConfirm = true },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE53935),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Release Pet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}