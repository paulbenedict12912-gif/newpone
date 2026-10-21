package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val totalDurationMillis: Long,
    val lapsCount: Int,
    val fastestLapMillis: Long?,
    val slowestLapMillis: Long?,
    val lapsData: String = "" // formatted summary or serialized laps
)
