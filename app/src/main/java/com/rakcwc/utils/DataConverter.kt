package com.rakcwc.utils

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class DataConverter {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { json.decodeFromString<List<String>>(it) }
    }
}