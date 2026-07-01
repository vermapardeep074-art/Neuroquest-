package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val progressDao: ProgressDao) {
    val progressFlow: Flow<UserProgress> = progressDao.getProgressFlow().map { it ?: UserProgress() }

    suspend fun getProgress(): UserProgress {
        return progressDao.getProgress() ?: UserProgress()
    }

    suspend fun saveProgress(progress: UserProgress) {
        progressDao.saveProgress(progress)
    }
}
