package com.example.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "social_accounts",
    foreignKeys = [
        ForeignKey(
            entity = ScanHistoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["scanId"])]
)
data class SocialAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scanId: Int,
    val platform: String, // "Twitter", "Instagram", "TikTok", "LinkedIn", "Facebook"
    val username: String,
    val name: String,
    val profileUrl: String,
    val bio: String,
    val matchScore: Int,
    val isVerified: Boolean,
    val profilePicUrl: String
) : Serializable
