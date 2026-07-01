package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgress
import com.example.model.ActiveGameState
import com.example.model.GameType
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    progress: UserProgress
) {
    val activeGame by viewModel.activeGame.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val hudMessage by viewModel.hudMessage.collectAsState()
    val streakMultiplier by viewModel.multiplier.collectAsState()

    val screenHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gameplay HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("Dashboard") },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0DFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Quit",
                        tint = IceText
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = activeGame?.displayName?.uppercase() ?: "BRAIN QUEST",
                        color = SleekIndigo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "LEVEL ${progress.level}",
                        color = SlateText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Hints Indicator button (Sleek Interface Style)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x0DFFFFFF))
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                        .clickable { viewModel.useHint() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = "Hint",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "HINT (${progress.hints})",
                        color = IceText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress HUD Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonMagenta.copy(alpha = 0.15f))
                        .border(1.dp, NeonMagenta.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Whatshot,
                        contentDescription = null,
                        tint = NeonMagenta,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "STREAK x$streakMultiplier",
                        color = NeonMagenta,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                }

                // Hearts
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Energy",
                        tint = NeonMagenta,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${progress.energy}/${progress.maxEnergy}",
                        color = IceText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Central Interactive HUD Instructions Box
            Spacer(modifier = Modifier.height(12.dp))
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 12.dp
            ) {
                Text(
                    text = hudMessage,
                    color = IceText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Play Board Wrapper
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (val state = gameState) {
                    is ActiveGameState.MemoryGridState -> MemoryGameBoard(state, viewModel)
                    is ActiveGameState.MathState -> MathGameBoard(state, viewModel)
                    is ActiveGameState.ReactionState -> ReactionGameBoard(state, viewModel)
                    is ActiveGameState.FocusStroopState -> FocusStroopGameBoard(state, viewModel)
                    is ActiveGameState.SequenceState -> SequenceGameBoard(state, viewModel)
                    else -> {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// GAME MODE RENDERING PANELS
// ----------------------------------------------------------------------------------

// 1. MEMORY GRID BOARD
@Composable
fun MemoryGameBoard(
    state: ActiveGameState.MemoryGridState,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val size = state.size
        for (row in 0 until size) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until size) {
                    val index = row * size + col
                    val isTarget = state.targetIndices.contains(index)
                    val isSelected = state.selectedIndices.contains(index)

                    val color = when {
                        state.showPattern && isTarget -> NeonCyan
                        isSelected && isTarget -> NeonCyan
                        isSelected && !isTarget -> NeonMagenta
                        else -> CosmicDarkSurface
                    }

                    val border = if (state.showPattern && isTarget) NeonCyan else BorderGlass

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                            .border(1.dp, border, RoundedCornerShape(12.dp))
                            .clickable(enabled = !state.showPattern) {
                                viewModel.submitMemoryTile(index)
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// 2. MATH BOARD
@Composable
fun MathGameBoard(
    state: ActiveGameState.MathState,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = state.question,
            color = IceText,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 20.dp)
        )

        // Multiple choice options
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.options.forEachIndexed { index, option ->
                val isEliminated = option == "✖"
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isEliminated) Color.Transparent else CosmicDarkSurface)
                        .border(1.dp, if (isEliminated) Color.Transparent else BorderGlass, RoundedCornerShape(14.dp))
                        .clickable(enabled = !isEliminated) {
                            viewModel.submitMathAnswer(index)
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        color = if (isEliminated) SlateText else NeonYellow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 3. REACTION SPEED BOARD
@Composable
fun ReactionGameBoard(
    state: ActiveGameState.ReactionState,
    viewModel: GameViewModel
) {
    val isWait = state.status == ActiveGameState.ReactionState.ReactionStatus.WAIT
    val isTap = state.status == ActiveGameState.ReactionState.ReactionStatus.TAP_NOW
    
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val color = when (state.status) {
        ActiveGameState.ReactionState.ReactionStatus.WAIT -> CosmicDarkCard.copy(alpha = alpha)
        ActiveGameState.ReactionState.ReactionStatus.TAP_NOW -> NeonGreen
        ActiveGameState.ReactionState.ReactionStatus.FOUL -> NeonMagenta
        else -> NeonCyan
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(color)
            .border(2.dp, BorderGlass, RoundedCornerShape(24.dp))
            .clickable {
                viewModel.handleReactionTap()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = when (state.status) {
                    ActiveGameState.ReactionState.ReactionStatus.WAIT -> Icons.Rounded.HourglassBottom
                    ActiveGameState.ReactionState.ReactionStatus.TAP_NOW -> Icons.Rounded.TouchApp
                    ActiveGameState.ReactionState.ReactionStatus.FOUL -> Icons.Rounded.Error
                    else -> Icons.Rounded.CheckCircle
                },
                contentDescription = null,
                tint = if (isTap) CosmicDarkBg else IceText,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (state.status) {
                    ActiveGameState.ReactionState.ReactionStatus.WAIT -> "HOLD... WAIT FOR GREEN"
                    ActiveGameState.ReactionState.ReactionStatus.TAP_NOW -> "TAP NOW!!!"
                    ActiveGameState.ReactionState.ReactionStatus.FOUL -> "FOUL! TOO EARLY!"
                    else -> "SUCCESS!"
                },
                color = if (isTap) CosmicDarkBg else IceText,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

// 4. FOCUS / STROOP WORD BOARD
@Composable
fun FocusStroopGameBoard(
    state: ActiveGameState.FocusStroopState,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.text,
                    color = state.textColorValue,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "The colored word is styled in...",
                    color = SlateText,
                    fontSize = 13.sp
                )
            }
        }

        // True False CTA Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.submitStroopAnswer(true) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Text("MATCH", color = CosmicDarkBg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Button(
                onClick = { viewModel.submitStroopAnswer(false) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Text("MISMATCH", color = IceText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// 5. SIMON PATTERN BOARD
@Composable
fun SequenceGameBoard(
    state: ActiveGameState.SequenceState,
    viewModel: GameViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val colors = listOf(NeonCyan, NeonMagenta, NeonYellow, NeonPurple)
        
        // Quad Layout
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0..1) {
                    val index = row * 2 + col
                    val isFlashed = state.activeFlashIndex == index
                    
                    val color = colors[index]
                    val opacity = if (isFlashed) 1.0f else 0.25f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(color.copy(alpha = opacity))
                            .border(
                                width = if (isFlashed) 3.dp else 1.dp,
                                color = if (isFlashed) color else BorderGlass,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = !state.isShowingPattern) {
                                viewModel.submitSequenceButton(index)
                            }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
