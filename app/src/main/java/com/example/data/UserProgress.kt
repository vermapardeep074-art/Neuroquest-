package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val username: String = "BrainExplorer",
    val avatarId: String = "avatar_neon_brain",
    val country: String = "US",
    val coins: Int = 250,
    val stars: Int = 0,
    val xp: Int = 0,
    val energy: Int = 5,
    val maxEnergy: Int = 5,
    val hints: Int = 5,
    val level: Int = 1,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    
    // Cognitive scores (0 to 100)
    val memoryScore: Float = 60f,
    val iqScore: Float = 60f,
    val reactionScore: Float = 60f,
    val focusScore: Float = 60f,
    val accuracyScore: Float = 75f,
    
    // History
    val gamesPlayed: Int = 0,
    val totalPlayTimeSec: Long = 0,
    val lastPlayTimestamp: Long = 0,
    
    // Daily Goal progress towards rewards
    val dailyGoalCount: Int = 0,
    val dailyGoalTarget: Int = 3,
    val dailyGoalClaimed: Boolean = false,
    val lastGoalResetTimestamp: Long = 0L,
    
    // Level progress for each individual game mode (supports 10,000+ levels)
    val memoryLevel: Int = 1,
    val mathLevel: Int = 1,
    val reactionLevel: Int = 1,
    val focusLevel: Int = 1,
    val sequenceLevel: Int = 1,
    
    // Unlocked achievements (comma-separated list of IDs)
    val unlockedAchievements: String = "first_login",
    
    // Settings
    val musicEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val darkModeEnabled: Boolean = true
)
