package com.example.viewmodel

import android.app.Application
import android.os.Vibrator
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.UserProgress
import com.example.model.Achievement
import com.example.model.ActiveGameState
import com.example.model.Difficulty
import com.example.model.GameType
import com.example.utils.SoundSynthesizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    
    private val _progress = MutableStateFlow(UserProgress())
    val progress: StateFlow<UserProgress> = _progress.asStateFlow()

    private val _activeGame = MutableStateFlow<GameType?>(null)
    val activeGame: StateFlow<GameType?> = _activeGame.asStateFlow()

    private val _gameState = MutableStateFlow<ActiveGameState>(ActiveGameState.Idle)
    val gameState: StateFlow<ActiveGameState> = _gameState.asStateFlow()

    private val _hudMessage = MutableStateFlow("")
    val hudMessage: StateFlow<String> = _hudMessage.asStateFlow()

    private val _multiplier = MutableStateFlow(1)
    val multiplier: StateFlow<Int> = _multiplier.asStateFlow()

    // Screen navigation flow (Dashboard, GameSelection, ActiveGame, Statistics, Store, Achievements, Profile)
    private val _currentScreen = MutableStateFlow("Dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // For particles and confetti trigger
    private val _confettiTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val confettiTrigger = _confettiTrigger.asSharedFlow()

    private var activeJob: Job? = null

    // Game level selection (Level 1 to 10,000+)
    private val _selectedLevel = MutableStateFlow(1)
    val selectedLevel: StateFlow<Int> = _selectedLevel.asStateFlow()

    private val _playingLevel = MutableStateFlow(1)
    val playingLevel: StateFlow<Int> = _playingLevel.asStateFlow()

    fun selectLevel(level: Int) {
        _selectedLevel.value = level.coerceIn(1, 10000)
    }

    fun adjustSelectedLevel(delta: Int) {
        _selectedLevel.value = (_selectedLevel.value + delta).coerceIn(1, 10000)
    }

    init {
        val progressDao = AppDatabase.getDatabase(application).progressDao()
        repository = GameRepository(progressDao)
        
        viewModelScope.launch {
            repository.progressFlow.collect { userProgress ->
                val checkedProgress = checkAndResetDailyGoal(userProgress)
                if (checkedProgress != userProgress) {
                    repository.saveProgress(checkedProgress)
                } else {
                    _progress.value = userProgress
                    // Sync settings with local Synthesizer toggles
                    SoundSynthesizer.isSoundEnabled = userProgress.soundEnabled
                    SoundSynthesizer.isMusicEnabled = userProgress.musicEnabled
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        if (screen == "Dashboard") {
            activeJob?.cancel()
            _activeGame.value = null
            _gameState.value = ActiveGameState.Idle
        }
        _currentScreen.value = screen
        SoundSynthesizer.playClick()
    }

    // Trigger phone vibration if toggled on
    private fun triggerVibration(durationMs: Long = 80) {
        if (_progress.value.vibrationEnabled) {
            val context = getApplication<Application>()
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    val attributionContext = context.createAttributionContext("vibrator")
                    attributionContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                } catch (e: Exception) {
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(durationMs, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        }
    }

    // Settings adjustments
    fun updateSettings(music: Boolean, sound: Boolean, vibration: Boolean, dark: Boolean) {
        viewModelScope.launch {
            val updated = _progress.value.copy(
                musicEnabled = music,
                soundEnabled = sound,
                vibrationEnabled = vibration,
                darkModeEnabled = dark
            )
            repository.saveProgress(updated)
            SoundSynthesizer.isSoundEnabled = sound
            SoundSynthesizer.isMusicEnabled = music
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.saveProgress(UserProgress(
                id = 1,
                username = _progress.value.username,
                avatarId = _progress.value.avatarId,
                country = _progress.value.country
            ))
            _gameState.value = ActiveGameState.Idle
            _activeGame.value = null
            _currentScreen.value = "Dashboard"
        }
    }

    fun updateProfile(username: String, avatarId: String, country: String) {
        viewModelScope.launch {
            val updated = _progress.value.copy(
                username = username,
                avatarId = avatarId,
                country = country
            )
            repository.saveProgress(updated)
        }
    }

    // Lucky wheel spin action
    fun spinLuckyWheel(onFinished: (reward: String, amount: Int) -> Unit) {
        viewModelScope.launch {
            SoundSynthesizer.playBonus()
            val rewardType = listOf("Coins", "XP", "Hints", "Energy").random()
            val amount = when (rewardType) {
                "Coins" -> (50..200 step 25).toList().random()
                "XP" -> (20..100 step 20).toList().random()
                "Hints" -> (1..2).random()
                else -> 1 // Energy refill
            }

            var updated = _progress.value
            updated = when (rewardType) {
                "Coins" -> updated.copy(coins = updated.coins + amount)
                "XP" -> addXpToProgress(updated, amount)
                "Hints" -> updated.copy(hints = updated.hints + amount)
                else -> updated.copy(energy = (updated.energy + amount).coerceAtMost(updated.maxEnergy))
            }
            repository.saveProgress(updated)
            checkAchievements(updated)
            onFinished(rewardType, amount)
        }
    }

    // Store Actions
    fun buyEnergyRefill(): Boolean {
        val current = _progress.value
        if (current.coins >= 50 && current.energy < current.maxEnergy) {
            viewModelScope.launch {
                repository.saveProgress(current.copy(
                    coins = current.coins - 50,
                    energy = current.maxEnergy
                ))
                SoundSynthesizer.playBonus()
            }
            return true
        }
        return false
    }

    fun buyHintPack(): Boolean {
        val current = _progress.value
        if (current.coins >= 100) {
            viewModelScope.launch {
                repository.saveProgress(current.copy(
                    coins = current.coins - 100,
                    hints = current.hints + 3
                ))
                SoundSynthesizer.playBonus()
            }
            return true
        }
        return false
    }

    fun buyAvatar(avatarId: String, cost: Int): Boolean {
        val current = _progress.value
        if (current.coins >= cost) {
            viewModelScope.launch {
                repository.saveProgress(current.copy(
                    coins = current.coins - cost,
                    avatarId = avatarId
                ))
                SoundSynthesizer.playBonus()
            }
            return true
        }
        return false
    }

    // Adaptive game loading & launch
    fun startGame(type: GameType) {
        if (_progress.value.energy <= 0) {
            _hudMessage.value = "Out of energy! Purchase an instant refill in the Store!"
            return
        }

        activeJob?.cancel()
        _activeGame.value = type
        _multiplier.value = 1
        _playingLevel.value = _selectedLevel.value
        _hudMessage.value = "Preparing training..."
        _currentScreen.value = "ActiveGame"
        
        generatePuzzle(type)
    }

    private fun generatePuzzle(type: GameType) {
        val lvl = _playingLevel.value
        when (type) {
            GameType.MEMORY -> generateMemoryGrid(lvl)
            GameType.MATH -> generateMathPuzzle(lvl)
            GameType.REACTION -> generateReactionTest(lvl)
            GameType.FOCUS -> generateFocusStroop(lvl)
            GameType.SEQUENCE -> generateSequencePuzzle(lvl)
        }
    }

    // 1. MEMORY GRID GENERATION
    private fun generateMemoryGrid(playerLevel: Int) {
        val size = when {
            playerLevel <= 5 -> 3
            playerLevel <= 20 -> 4
            playerLevel <= 50 -> 5
            else -> 6
        }
        val count = (3 + (playerLevel - 1) / 5).coerceAtMost(size * size - 3)

        val totalTiles = size * size
        val indices = (0 until totalTiles).toList().shuffled().take(count).toSet()

        _gameState.value = ActiveGameState.MemoryGridState(
            size = size,
            targetIndices = indices,
            showPattern = true
        )

        activeJob = viewModelScope.launch {
            _hudMessage.value = "Memorize tiles! (Level $playerLevel)"
            SoundSynthesizer.playTone(440.0, 300)
            val delayMs = (1500 - playerLevel * 5L).coerceAtLeast(400L)
            delay(delayMs) // Speed up pattern memorization with higher levels
            
            val current = _gameState.value
            if (current is ActiveGameState.MemoryGridState) {
                _gameState.value = current.copy(showPattern = false)
                _hudMessage.value = "Recall highlighted tiles!"
                SoundSynthesizer.playClick()
            }
        }
    }

    fun submitMemoryTile(index: Int) {
        val state = _gameState.value as? ActiveGameState.MemoryGridState ?: return
        if (state.showPattern) return // Still presenting

        val updatedSelected = state.selectedIndices.toMutableSet()
        if (updatedSelected.contains(index)) return

        updatedSelected.add(index)
        SoundSynthesizer.playClick()
        triggerVibration()

        if (state.targetIndices.contains(index)) {
            // Selected a correct tile
            _gameState.value = state.copy(selectedIndices = updatedSelected)
            if (updatedSelected.intersect(state.targetIndices).size == state.targetIndices.size) {
                // Win!
                onGameSuccess(GameType.MEMORY)
            }
        } else {
            // Clicked an incorrect tile -> immediate failure
            onGameFailure(GameType.MEMORY)
        }
    }

    // 2. SPEED MATH GENERATION
    private fun generateMathPuzzle(playerLevel: Int) {
        val r = Random
        var question = ""
        var answer = 0
        
        when {
            playerLevel <= 5 -> {
                // Easy Single-digit/Double-digit Addition/Subtraction
                val op = listOf("+", "-").random()
                val val1 = r.nextInt(2, 10 + playerLevel * 2)
                val val2 = r.nextInt(2, 10 + playerLevel * 2)
                if (op == "+") {
                    answer = val1 + val2
                    question = "$val1 + $val2 = ?"
                } else {
                    val maxVal = maxOf(val1, val2)
                    val minVal = minOf(val1, val2)
                    answer = maxVal - minVal
                    question = "$maxVal - $minVal = ?"
                }
            }
            playerLevel <= 20 -> {
                // Medium addition, subtraction, multiplication
                val op = listOf("+", "-", "*").random()
                when (op) {
                    "+" -> {
                        val val1 = r.nextInt(10, 50 + playerLevel)
                        val val2 = r.nextInt(10, 50 + playerLevel)
                        answer = val1 + val2
                        question = "$val1 + $val2 = ?"
                    }
                    "-" -> {
                        val val1 = r.nextInt(20, 80 + playerLevel)
                        val val2 = r.nextInt(5, val1)
                        answer = val1 - val2
                        question = "$val1 - $val2 = ?"
                    }
                    else -> {
                        val val1 = r.nextInt(2, 9 + playerLevel / 4)
                        val val2 = r.nextInt(2, 10)
                        answer = val1 * val2
                        question = "$val1 × $val2 = ?"
                    }
                }
            }
            playerLevel <= 100 -> {
                // Hard addition, subtraction, multiplication, division
                val op = listOf("+", "-", "*", "/").random()
                when (op) {
                    "+" -> {
                        val val1 = r.nextInt(50, 150 + playerLevel * 2)
                        val val2 = r.nextInt(50, 150 + playerLevel * 2)
                        answer = val1 + val2
                        question = "$val1 + $val2 = ?"
                    }
                    "-" -> {
                        val val1 = r.nextInt(100, 300 + playerLevel * 2)
                        val val2 = r.nextInt(10, val1 - 10)
                        answer = val1 - val2
                        question = "$val1 - $val2 = ?"
                    }
                    "*" -> {
                        val val1 = r.nextInt(3, 12 + playerLevel / 10)
                        val val2 = r.nextInt(3, 12 + playerLevel / 10)
                        answer = val1 * val2
                        question = "$val1 × $val2 = ?"
                    }
                    else -> {
                        val divisor = r.nextInt(3, 12)
                        answer = r.nextInt(2, 12 + playerLevel / 10)
                        val dividend = divisor * answer
                        question = "$dividend ÷ $divisor = ?"
                    }
                }
            }
            else -> {
                // Expert & Infinite Progression levels (100 to 10000+)
                // Mix in 3-term expressions like "A + B * C" or "(A - B) * C"
                val type = r.nextInt(0, 3)
                when (type) {
                    0 -> { // A + B * C
                        val a = r.nextInt(10, (100 + playerLevel / 10).coerceAtMost(1000))
                        val b = r.nextInt(2, (5 + playerLevel / 50).coerceAtMost(30))
                        val c = r.nextInt(2, (5 + playerLevel / 50).coerceAtMost(30))
                        answer = a + (b * c)
                        question = "$a + ($b × $c) = ?"
                    }
                    1 -> { // (B * C) - D
                        val b = r.nextInt(2, (5 + playerLevel / 50).coerceAtMost(30))
                        val c = r.nextInt(2, (5 + playerLevel / 50).coerceAtMost(30))
                        val a = b * c
                        val d = r.nextInt(5, (50 + playerLevel / 20).coerceAtMost(500))
                        answer = a - d
                        question = "($b × $c) - $d = ?"
                    }
                    else -> { // (B * C) + D
                        val b = r.nextInt(2, (5 + playerLevel / 50).coerceAtMost(30))
                        val c = r.nextInt(2, (5 + playerLevel / 50).coerceAtMost(30))
                        val d = r.nextInt(10, (100 + playerLevel / 10).coerceAtMost(1000))
                        answer = (b * c) + d
                        question = "($b × $c) + $d = ?"
                    }
                }
            }
        }

        // Generate 4 randomized options containing the answer
        val options = mutableListOf<String>()
        options.add(answer.toString())
        while (options.size < 4) {
            val dist = r.nextInt(-15, 15)
            val fake = answer + dist
            if (fake != answer && !options.contains(fake.toString())) {
                options.add(fake.toString())
            }
        }
        options.shuffle()
        val correctIndex = options.indexOf(answer.toString())

        _gameState.value = ActiveGameState.MathState(
            question = question,
            options = options,
            correctIndex = correctIndex
        )
        _hudMessage.value = "Solve equation! (Level $playerLevel)"
    }

    fun submitMathAnswer(optionIndex: Int) {
        val state = _gameState.value as? ActiveGameState.MathState ?: return
        SoundSynthesizer.playClick()
        triggerVibration()

        if (optionIndex == state.correctIndex) {
            onGameSuccess(GameType.MATH)
        } else {
            onGameFailure(GameType.MATH)
        }
    }

    // 3. QUICK TAP GENERATION
    private fun generateReactionTest(playerLevel: Int) {
        _gameState.value = ActiveGameState.ReactionState(
            status = ActiveGameState.ReactionState.ReactionStatus.WAIT
        )
        val targetThreshold = (600.0 - playerLevel * 1.5).coerceAtLeast(180.0).toInt()
        _hudMessage.value = "GET READY... (Target: <${targetThreshold}ms)"

        val randomDelay = Random.nextLong(1500, 4000)

        activeJob = viewModelScope.launch {
            delay(randomDelay)
            // Ensure still in wait state
            val current = _gameState.value as? ActiveGameState.ReactionState
            if (current != null && current.status == ActiveGameState.ReactionState.ReactionStatus.WAIT) {
                _gameState.value = ActiveGameState.ReactionState(
                    status = ActiveGameState.ReactionState.ReactionStatus.TAP_NOW,
                    targetTimeMs = System.currentTimeMillis()
                )
                _hudMessage.value = "TAP NOW!!!"
                SoundSynthesizer.playTone(880.0, 150)
                triggerVibration(150)
            }
        }
    }

    fun handleReactionTap() {
        val state = _gameState.value as? ActiveGameState.ReactionState ?: return
        SoundSynthesizer.playClick()
        triggerVibration()

        val playerLevel = _playingLevel.value
        val targetThreshold = (600.0 - playerLevel * 1.5).coerceAtLeast(180.0)

        if (state.status == ActiveGameState.ReactionState.ReactionStatus.WAIT) {
            activeJob?.cancel()
            _gameState.value = state.copy(status = ActiveGameState.ReactionState.ReactionStatus.FOUL)
            _hudMessage.value = "Too Early! Foul tap!"
            onGameFailure(GameType.REACTION)
        } else if (state.status == ActiveGameState.ReactionState.ReactionStatus.TAP_NOW) {
            val timeTaken = System.currentTimeMillis() - state.targetTimeMs
            
            if (timeTaken <= targetThreshold) {
                _gameState.value = ActiveGameState.ReactionState(
                    status = ActiveGameState.ReactionState.ReactionStatus.SUCCESS,
                    reactionTimeMs = timeTaken
                )
                _hudMessage.value = "Reaction time: ${timeTaken}ms (Passed!)"
                
                // Log score factor
                viewModelScope.launch {
                    val current = _progress.value
                    val updatedAccuracy = ((current.accuracyScore * current.gamesPlayed + 100f) / (current.gamesPlayed + 1)).coerceIn(50f, 100f)
                    val fastestTime = if (current.reactionScore == 60f) timeTaken.toFloat() else current.reactionScore.coerceAtMost(timeTaken.toFloat())
                    
                    val updated = current.copy(
                        reactionScore = fastestTime,
                        accuracyScore = updatedAccuracy
                    )
                    repository.saveProgress(updated)
                }
                
                onGameSuccess(GameType.REACTION)
            } else {
                _gameState.value = ActiveGameState.ReactionState(
                    status = ActiveGameState.ReactionState.ReactionStatus.FOUL
                )
                _hudMessage.value = "Too Slow! ${timeTaken}ms (Target: <${targetThreshold.toInt()}ms)"
                onGameFailure(GameType.REACTION)
            }
        }
    }

    // 4. FOCUS / STROOP TEST GENERATION
    private fun generateFocusStroop(playerLevel: Int) {
        val allColors = listOf(
            Triple("RED", Color(0xFFE53935), "RED"),
            Triple("BLUE", Color(0xFF1E88E5), "BLUE"),
            Triple("GREEN", Color(0xFF43A047), "GREEN"),
            Triple("YELLOW", Color(0xFFFDD835), "YELLOW"),
            Triple("PURPLE", Color(0xFF8E24AA), "PURPLE"),
            Triple("ORANGE", Color(0xFFFF9800), "ORANGE"),
            Triple("PINK", Color(0xFFE91E63), "PINK"),
            Triple("CYAN", Color(0xFF00BCD4), "CYAN"),
            Triple("GRAY", Color(0xFF9E9E9E), "GRAY"),
            Triple("WHITE", Color(0xFFFFFFFF), "WHITE")
        )
        // Pool scales up from 5 to 10 colors at higher levels
        val poolSize = (5 + playerLevel / 50).coerceAtMost(10)
        val colors = allColors.take(poolSize)

        val textItem = colors.random()
        val colorItem = colors.random()

        val isMatch = textItem.first == colorItem.third

        _gameState.value = ActiveGameState.FocusStroopState(
            text = textItem.first,
            textColorName = colorItem.third,
            textColorValue = colorItem.second,
            isMatch = isMatch
        )
        
        val timeLimit = (3000 - playerLevel * 20L).coerceAtLeast(600L)
        _hudMessage.value = "Does text name match actual font color? (Time: ${timeLimit}ms)"

        // Start a timeout timer
        activeJob = viewModelScope.launch {
            delay(timeLimit)
            // If the delay completes, the player was too slow
            _hudMessage.value = "TIME'S UP! Answer faster next time!"
            onGameFailure(GameType.FOCUS)
        }
    }

    fun submitStroopAnswer(answer: Boolean) {
        val state = _gameState.value as? ActiveGameState.FocusStroopState ?: return
        activeJob?.cancel() // Cancel the timeout countdown immediately
        SoundSynthesizer.playClick()
        triggerVibration()

        if (answer == state.isMatch) {
            onGameSuccess(GameType.FOCUS)
        } else {
            onGameFailure(GameType.FOCUS)
        }
    }

    // 5. SIMON PATTERN SEQUENCE GENERATION
    private fun generateSequencePuzzle(playerLevel: Int) {
        val sequenceLength = (3 + playerLevel / 10).coerceAtMost(16)

        val sequence = List(sequenceLength) { Random.nextInt(0, 4) }
        
        _gameState.value = ActiveGameState.SequenceState(
            targetSequence = sequence,
            isShowingPattern = true
        )

        activeJob = viewModelScope.launch {
            _hudMessage.value = "Watch sequence! (Level $playerLevel)"
            delay(800)
            
            val flashDuration = (300 - playerLevel * 2L).coerceAtLeast(100L)
            val delayBetween = (150 - playerLevel * 1L).coerceAtLeast(50L)
            
            for (step in sequence) {
                // Flash on
                val current = _gameState.value as? ActiveGameState.SequenceState ?: break
                _gameState.value = current.copy(activeFlashIndex = step)
                when (step) {
                    0 -> SoundSynthesizer.playTone(329.63, flashDuration.toInt()) // E4
                    1 -> SoundSynthesizer.playTone(392.00, flashDuration.toInt()) // G4
                    2 -> SoundSynthesizer.playTone(440.00, flashDuration.toInt()) // A4
                    3 -> SoundSynthesizer.playTone(523.25, flashDuration.toInt()) // C5
                }
                triggerVibration(60)
                delay(flashDuration)
                
                // Flash off
                val stateOff = _gameState.value as? ActiveGameState.SequenceState ?: break
                _gameState.value = stateOff.copy(activeFlashIndex = null)
                delay(delayBetween)
            }

            val finalState = _gameState.value as? ActiveGameState.SequenceState
            if (finalState != null) {
                _gameState.value = finalState.copy(isShowingPattern = false)
                _hudMessage.value = "Repeat sequence!"
            }
        }
    }

    fun submitSequenceButton(padIndex: Int) {
        val state = _gameState.value as? ActiveGameState.SequenceState ?: return
        if (state.isShowingPattern) return

        SoundSynthesizer.playClick()
        triggerVibration()

        val playerSeq = state.playerSequence + padIndex
        _gameState.value = state.copy(playerSequence = playerSeq)

        val stepIndex = playerSeq.size - 1
        if (state.targetSequence[stepIndex] == padIndex) {
            // Correct step
            if (playerSeq.size == state.targetSequence.size) {
                // Completed whole pattern!
                onGameSuccess(GameType.SEQUENCE)
            }
        } else {
            // Wrong step -> Immediate fail
            onGameFailure(GameType.SEQUENCE)
        }
    }

    // Hint trigger
    fun useHint() {
        val current = _progress.value
        if (current.hints <= 0) return

        viewModelScope.launch {
            repository.saveProgress(current.copy(hints = current.hints - 1))
            
            // Apply mode specific cheat
            when (val state = _gameState.value) {
                is ActiveGameState.MemoryGridState -> {
                    // Temporarily flash correct tiles
                    _gameState.value = state.copy(showPattern = true)
                    delay(1000)
                    val afterHint = _gameState.value as? ActiveGameState.MemoryGridState
                    if (afterHint != null) {
                        _gameState.value = afterHint.copy(showPattern = false)
                    }
                }
                is ActiveGameState.MathState -> {
                    // Eliminate 2 wrong choices
                    val updatedOptions = state.options.toMutableList()
                    var eliminated = 0
                    for (i in updatedOptions.indices) {
                        if (i != state.correctIndex && eliminated < 2) {
                            updatedOptions[i] = "✖"
                            eliminated++
                        }
                    }
                    _gameState.value = state.copy(options = updatedOptions)
                }
                is ActiveGameState.FocusStroopState -> {
                    // Instantly provide hint message
                    _hudMessage.value = "HINT: It is ${state.isMatch.toString().uppercase()}!"
                }
                is ActiveGameState.SequenceState -> {
                    // Highlight the next button
                    val nextStep = state.targetSequence.getOrNull(state.playerSequence.size)
                    if (nextStep != null) {
                        _hudMessage.value = "HINT: Next color pad is #${nextStep + 1}!"
                    }
                }
                else -> {}
            }
        }
    }

    // Success & Fail Outcomes
    private fun onGameSuccess(type: GameType) {
        SoundSynthesizer.playVictory()
        triggerVibration(250)
        _confettiTrigger.tryEmit(Unit)

        viewModelScope.launch {
            val current = _progress.value
            
            val xpGain = (30 * _multiplier.value)
            val coinGain = (15 * _multiplier.value)
            
            // Multiplier climbs for winstreak
            _multiplier.value = (_multiplier.value + 1).coerceAtMost(5)

            // Adjust cognitive metrics slightly based on completed category
            val updatedMemory = if (type == GameType.MEMORY) (current.memoryScore + 1.5f).coerceAtMost(100f) else current.memoryScore
            val updatedIq = if (type == GameType.MATH) (current.iqScore + 1.8f).coerceAtMost(100f) else current.iqScore
            val updatedReaction = if (type == GameType.REACTION && current.reactionScore < 60f) (current.reactionScore).coerceIn(100f, 600f) else current.reactionScore
            val updatedFocus = if (type == GameType.FOCUS) (current.focusScore + 1.5f).coerceAtMost(100f) else current.focusScore

            val gamesCount = current.gamesPlayed + 1
            val updatedAccuracy = ((current.accuracyScore * current.gamesPlayed + 100f) / gamesCount).coerceIn(50f, 100f)

            val newDailyGoalCount = (current.dailyGoalCount + 1).coerceAtMost(current.dailyGoalTarget)

            var updated = current.copy(
                coins = current.coins + coinGain,
                stars = current.stars + 1,
                gamesPlayed = gamesCount,
                memoryScore = updatedMemory,
                iqScore = updatedIq,
                reactionScore = updatedReaction,
                focusScore = updatedFocus,
                accuracyScore = updatedAccuracy,
                lastPlayTimestamp = System.currentTimeMillis(),
                dailyGoalCount = newDailyGoalCount
            )

            // Dynamic Level Up for played game mode
            val playingLvl = _playingLevel.value
            when (type) {
                GameType.MEMORY -> {
                    if (playingLvl == updated.memoryLevel) {
                        updated = updated.copy(memoryLevel = updated.memoryLevel + 1)
                    }
                }
                GameType.MATH -> {
                    if (playingLvl == updated.mathLevel) {
                        updated = updated.copy(mathLevel = updated.mathLevel + 1)
                    }
                }
                GameType.REACTION -> {
                    if (playingLvl == updated.reactionLevel) {
                        updated = updated.copy(reactionLevel = updated.reactionLevel + 1)
                    }
                }
                GameType.FOCUS -> {
                    if (playingLvl == updated.focusLevel) {
                        updated = updated.copy(focusLevel = updated.focusLevel + 1)
                    }
                }
                GameType.SEQUENCE -> {
                    if (playingLvl == updated.sequenceLevel) {
                        updated = updated.copy(sequenceLevel = updated.sequenceLevel + 1)
                    }
                }
            }

            // Dynamic Level Up check
            updated = addXpToProgress(updated, xpGain)

            repository.saveProgress(updated)
            checkAchievements(updated)

            _gameState.value = ActiveGameState.Idle
            val nextLvl = (playingLvl + 1).coerceIn(1, 10000)
            _playingLevel.value = nextLvl
            _selectedLevel.value = nextLvl
            _hudMessage.value = "EXCELLENT! +$xpGain XP, +$coinGain Coins earned! Loading Level $nextLvl..."
            
            delay(2200)
            if (_progress.value.energy <= 0) {
                _hudMessage.value = "Out of energy! Purchase an instant refill in the Store!"
                _activeGame.value = null
                _currentScreen.value = "Dashboard"
            } else {
                generatePuzzle(type)
            }
        }
    }

    private fun onGameFailure(type: GameType) {
        SoundSynthesizer.playWrong()
        triggerVibration(400)
        
        _multiplier.value = 1 // Reset streak multiplier

        viewModelScope.launch {
            val current = _progress.value
            val energyLeft = (current.energy - 1).coerceAtLeast(0)

            val gamesCount = current.gamesPlayed + 1
            val updatedAccuracy = ((current.accuracyScore * current.gamesPlayed + 0f) / gamesCount).coerceIn(50f, 100f)

            val updated = current.copy(
                energy = energyLeft,
                gamesPlayed = gamesCount,
                accuracyScore = updatedAccuracy,
                lastPlayTimestamp = System.currentTimeMillis()
            )

            repository.saveProgress(updated)
            checkAchievements(updated)

            _gameState.value = ActiveGameState.Idle
            _hudMessage.value = "FAILED CHALLENGE! Lost 1 Energy heart. Retrying Level ${_playingLevel.value}..."

            delay(2200)
            if (energyLeft <= 0) {
                _hudMessage.value = "Out of energy! Returning to Dashboard..."
                _activeGame.value = null
                _currentScreen.value = "Dashboard"
            } else {
                generatePuzzle(type)
            }
        }
    }

    private fun addXpToProgress(user: UserProgress, xpEarned: Int): UserProgress {
        var currentXp = user.xp + xpEarned
        var currentLevel = user.level
        var xpRequired = currentLevel * 100

        while (currentXp >= xpRequired) {
            currentXp -= xpRequired
            currentLevel++
            xpRequired = currentLevel * 100
            SoundSynthesizer.playBonus()
        }

        return user.copy(xp = currentXp, level = currentLevel)
    }

    private fun checkAchievements(user: UserProgress) {
        viewModelScope.launch {
            val alreadyUnlocked = user.unlockedAchievements.split(",").toSet()
            val newlyUnlocked = mutableListOf<String>()

            for (ach in Achievement.ALL) {
                if (alreadyUnlocked.contains(ach.id)) continue

                val targetMet = when (ach.type) {
                    "games_played" -> user.gamesPlayed >= ach.targetValue
                    "level" -> user.level >= ach.targetValue
                    "streak" -> user.currentStreak >= ach.targetValue
                    "coins" -> user.coins >= ach.targetValue
                    "score" -> {
                        when (ach.id) {
                            "perfect_accuracy" -> user.accuracyScore >= ach.targetValue
                            else -> false
                        }
                    }
                    else -> false
                }

                if (targetMet) {
                    newlyUnlocked.add(ach.id)
                }
            }

            if (newlyUnlocked.isNotEmpty()) {
                val fullList = (alreadyUnlocked + newlyUnlocked).filter { it.isNotEmpty() }.joinToString(",")
                val rewardTotal = Achievement.ALL.filter { newlyUnlocked.contains(it.id) }.sumOf { it.rewardCoins }
                
                val updated = user.copy(
                    unlockedAchievements = fullList,
                    coins = user.coins + rewardTotal
                )
                repository.saveProgress(updated)
                SoundSynthesizer.playBonus()
            }
        }
    }

    fun claimDailyGoalReward() {
        val current = _progress.value
        if (current.dailyGoalCount >= current.dailyGoalTarget && !current.dailyGoalClaimed) {
            viewModelScope.launch {
                val updated = current.copy(
                    coins = current.coins + 100,
                    hints = current.hints + 2,
                    dailyGoalClaimed = true
                )
                repository.saveProgress(updated)
                SoundSynthesizer.playVictory()
                _confettiTrigger.tryEmit(Unit)
                _hudMessage.value = "REWARD CLAIMED! +100 Gems and +2 Hints!"
            }
        }
    }

    private fun checkAndResetDailyGoal(current: UserProgress): UserProgress {
        val now = System.currentTimeMillis()
        if (current.lastGoalResetTimestamp == 0L) {
            return current.copy(
                dailyGoalCount = 0,
                dailyGoalClaimed = false,
                lastGoalResetTimestamp = now
            )
        }
        
        if (!isSameDay(current.lastGoalResetTimestamp, now)) {
            return current.copy(
                dailyGoalCount = 0,
                dailyGoalClaimed = false,
                lastGoalResetTimestamp = now
            )
        }
        return current
    }

    private fun isSameDay(time1: Long, time2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = time1 }
        val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = time2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    override fun onCleared() {
        super.onCleared()
        activeJob?.cancel()
    }
}
