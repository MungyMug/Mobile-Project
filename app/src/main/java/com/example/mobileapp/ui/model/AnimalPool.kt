package com.example.mobileapp.ui.model

import kotlin.random.Random

data class Animal(
    val emoji: String,
    val name: String,
    val rarity: Rarity = Rarity.COMMON
) {
    fun label(): String = "$emoji $name"
}

object AnimalPool {

    // Feel free to add/remove anytime.
    // Emojis are all “keyboard emojis”, no assets needed.
    private val animals = listOf(
        // ── Common ──────────────────────────────────────────────────────────────
        Animal("🐶", "Doggo",       Rarity.COMMON),
        Animal("🐱", "Catto",       Rarity.COMMON),
        Animal("🐭", "Mouse",       Rarity.COMMON),
        Animal("🐹", "Hamster",     Rarity.COMMON),
        Animal("🐰", "Bunny",       Rarity.COMMON),
        Animal("🦊", "Fox",         Rarity.COMMON),
        Animal("🐻", "Bear",        Rarity.COMMON),
        Animal("🐼", "Panda",       Rarity.COMMON),
        Animal("🐨", "Koala",       Rarity.COMMON),
        Animal("🐯", "Tiger",       Rarity.COMMON),
        Animal("🦁", "Lion",        Rarity.COMMON),
        Animal("🐮", "Cow",         Rarity.COMMON),
        Animal("🐷", "Pig",         Rarity.COMMON),
        Animal("🐸", "Frog",        Rarity.COMMON),
        Animal("🐵", "Monkey",      Rarity.COMMON),
        Animal("🐔", "Chicken",     Rarity.COMMON),
        Animal("🦆", "Duck",        Rarity.COMMON),
        Animal("🦉", "Owl",         Rarity.COMMON),
        Animal("🐧", "Penguin",     Rarity.COMMON),
        Animal("🐢", "Turtle",      Rarity.COMMON),
        Animal("🐠", "Fish",        Rarity.COMMON),
        Animal("🐙", "Octopus",     Rarity.COMMON),
        Animal("🐑", "Sheep",       Rarity.COMMON),
        Animal("🐐", "Goat",        Rarity.COMMON),
        Animal("🦃", "Turkey",      Rarity.COMMON),
        Animal("🐇", "Rabbit",      Rarity.COMMON),
        Animal("🦔", "Hedgehog",    Rarity.COMMON),
        Animal("🐿️", "Squirrel",   Rarity.COMMON),
        Animal("🦎", "Lizard",      Rarity.COMMON),
        Animal("🐍", "Snake",       Rarity.COMMON),
        Animal("🐌", "Snail",       Rarity.COMMON),
        Animal("🐛", "Caterpillar", Rarity.COMMON),
        Animal("🐜", "Ant",         Rarity.COMMON),
        Animal("🐝", "Bee",         Rarity.COMMON),
        Animal("🐞", "Ladybug",     Rarity.COMMON),
        Animal("🦗", "Cricket",     Rarity.COMMON),
        Animal("🦟", "Mosquito",    Rarity.COMMON),
        Animal("🐡", "Pufferfish",  Rarity.COMMON),
        Animal("🦀", "Crab",        Rarity.COMMON),
        Animal("🦞", "Lobster",     Rarity.COMMON),
        Animal("🦐", "Shrimp",      Rarity.COMMON),
        Animal("🐓", "Rooster",     Rarity.COMMON),
        Animal("🦚", "Peacock",     Rarity.COMMON),

        // ── Rare ─────────────────────────────────────────────────────────────────
        Animal("🦄", "Unicorn",     Rarity.RARE),
        Animal("🦋", "Butterfly",   Rarity.RARE),
        Animal("🦜", "Parrot",      Rarity.RARE),
        Animal("🦈", "Shark",       Rarity.RARE),
        Animal("🐬", "Dolphin",     Rarity.RARE),
        Animal("🦥", "Sloth",       Rarity.RARE),
        Animal("🦦", "Otter",       Rarity.RARE),
        Animal("🦩", "Flamingo",    Rarity.RARE),
        Animal("🐘", "Elephant",    Rarity.RARE),
        Animal("🦏", "Rhino",       Rarity.RARE),
        Animal("🦛", "Hippo",       Rarity.RARE),
        Animal("🦒", "Giraffe",     Rarity.RARE),
        Animal("🦓", "Zebra",       Rarity.RARE),
        Animal("🦘", "Kangaroo",    Rarity.RARE),
        Animal("🦙", "Llama",       Rarity.RARE),
        Animal("🦬", "Bison",       Rarity.RARE),
        Animal("🐆", "Cheetah",     Rarity.RARE),
        Animal("🐅", "SaberTooth Tiger",      Rarity.RARE),
        Animal("🦅", "Eagle",       Rarity.RARE),
        Animal("🦢", "Swan",        Rarity.RARE),
        Animal("🦭", "Seal",        Rarity.RARE),
        Animal("🐊", "Croc",        Rarity.RARE),
        Animal("🦕", "Brontosaurus",Rarity.RARE),
        Animal("🐋", "Whale",       Rarity.RARE),
        Animal("🦑", "Squid",       Rarity.RARE),

        // ── Epic ─────────────────────────────────────────────────────────────────
        Animal("🐉", "Dragon",      Rarity.EPIC),
        Animal("🦖", "T-Rex",       Rarity.EPIC),
        Animal("🦂", "Scorpion",    Rarity.EPIC),
        Animal("🐺", "Wolf",        Rarity.EPIC),
        Animal("🦇", "Vampire Bat", Rarity.EPIC),
        Animal("🕷️", "Spider",     Rarity.EPIC),
        Animal("🦠", "Microbe",     Rarity.EPIC),
        Animal("🐲", "Green Dragon",Rarity.EPIC),
        Animal("🦤", "Dodo",        Rarity.EPIC),
        Animal("🪲", "Beetle",      Rarity.EPIC),
        Animal("🪸", "Coral Beast", Rarity.EPIC),
        Animal("🐻‍❄️", "Polar Bear",Rarity.EPIC),

        // ── Legendary ────────────────────────────────────────────────────────────
        Animal("👑", "King Beast",     Rarity.LEGENDARY),
        Animal("🌈", "Rainbow Beast",  Rarity.LEGENDARY),
        Animal("🔥", "Fire Spirit",    Rarity.LEGENDARY),
        Animal("⚡", "Storm Beast",    Rarity.LEGENDARY),
        Animal("🌊", "Ocean God",      Rarity.LEGENDARY),
        Animal("🌙", "Moon Spirit",    Rarity.LEGENDARY),
        Animal("☄️", "Comet Beast",   Rarity.LEGENDARY),
        Animal("🍄", "Shroom God",     Rarity.LEGENDARY),
    )

    // Weighted rarity roll (tweak the chances)
    fun randomAnimal(rng: Random = Random): Animal {
        val roll = rng.nextInt(100) // 0..99
        val rarity = when {
            roll < 65 -> Rarity.COMMON      // 65%
            roll < 88 -> Rarity.RARE        // 23%
            roll < 97 -> Rarity.EPIC        // 9%
            else -> Rarity.LEGENDARY        // 3%
        }

        val pool = animals.filter { it.rarity == rarity }
        return pool.random(rng)
    }
}
