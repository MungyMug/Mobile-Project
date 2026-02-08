package com.example.mobileapp.ui.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EntryStorage {
    private const val PREFS_NAME = "petsafari_prefs"
    private const val KEY_ENTRIES = "zoo_entries"
    private val gson = Gson()

    fun save(context: Context, entries: List<ZooEntry>) {
        val json = gson.toJson(entries)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ENTRIES, json)
            .apply()
    }

    fun load(context: Context): List<ZooEntry> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ENTRIES, null) ?: return emptyList()
        val type = object : TypeToken<List<ZooEntry>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
