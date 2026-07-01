package com.example.model

import androidx.compose.ui.graphics.Color

sealed class ActiveGameState {
    object Idle : ActiveGameState()
    
    data class MemoryGridState(
        val size: Int,
        val targetIndices: Set<Int>,
        val selectedIndices: Set<Int> = emptySet(),
        val showPattern: Boolean = true
    ) : ActiveGameState()

    data class MathState(
        val question: String,
        val options: List<String>,
        val correctIndex: Int
    ) : ActiveGameState()

    data class ReactionState(
        val status: ReactionStatus,
        val targetTimeMs: Long = 0,
        val reactionTimeMs: Long = 0
    ) : ActiveGameState() {
        enum class ReactionStatus { WAIT, TAP_NOW, FOUL, SUCCESS }
    }

    data class FocusStroopState(
        val text: String,
        val textColorName: String,
        val textColorValue: Color,
        val isMatch: Boolean
    ) : ActiveGameState()

    data class SequenceState(
        val targetSequence: List<Int>,
        val playerSequence: List<Int> = emptyList(),
        val activeFlashIndex: Int? = null,
        val isShowingPattern: Boolean = true
    ) : ActiveGameState()
}
