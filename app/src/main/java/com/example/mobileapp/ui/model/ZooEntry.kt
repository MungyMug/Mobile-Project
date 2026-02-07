package com.example.mobileapp.ui.model

enum class Rarity { COMMON, RARE, EPIC, LEGENDARY }

data class ZooEntry(
    val id: Int,
    val name: String,
    val animal: String,        // emoji or later sticker key
    val rarity: Rarity = Rarity.COMMON,
    val unlocked: Boolean = true
)
