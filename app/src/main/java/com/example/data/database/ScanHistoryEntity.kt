package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUri: String,
    val name: String,
    val estimatedAge: String,
    val gender: String,
    val features: String,
    val overallConfidence: Int,
    val bio: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
