package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.Difficulty
import com.example.model.GameType
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@Composable
fun SelectionScreen(
    viewModel: GameViewModel,
    progress: UserProgress
) {
    var selectedType by remember { mutableStateOf(GameType.MEMORY) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("Dashboard") },
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CosmicDarkSurface)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = IceText
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "NEURAL EXPLORER",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = IceText,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Intro banner
                item {
                    Text(
                        text = "CHOOSE COGNITIVE PATH",
                        color = SlateText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Game Types Horizontal Grid Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GameType.entries.forEach { type ->
                            val isSelected = selectedType == type
                            val (color, icon) = when (type) {
                                GameType.MEMORY -> Pair(NeonCyan, Icons.Rounded.GridOn)
                                GameType.MATH -> Pair(NeonYellow, Icons.Rounded.Calculate)
                                GameType.REACTION -> Pair(NeonMagenta, Icons.Rounded.TouchApp)
                                GameType.FOCUS -> Pair(NeonGreen, Icons.Rounded.Visibility)
                                GameType.SEQUENCE -> Pair(NeonPurple, Icons.Rounded.Repeat)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) CosmicDarkSurface else CosmicDarkCard.copy(alpha = 0.4f))
                                    .border(
                                        1.dp,
                                        if (isSelected) SleekIndigo else BorderGlass,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = type.displayName, color = IceText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = type.description, color = SlateText, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // 10000+ Level Picker UI
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "COGNITIVE LEVEL CONFIGURATOR",
                        color = SlateText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    val maxUnlockedLevel = when (selectedType) {
                        GameType.MEMORY -> progress.memoryLevel
                        GameType.MATH -> progress.mathLevel
                        GameType.REACTION -> progress.reactionLevel
                        GameType.FOCUS -> progress.focusLevel
                        GameType.SEQUENCE -> progress.sequenceLevel
                    }

                    // Auto-sync selected level to highest unlocked level if it exceeds it or is initialized
                    LaunchedEffect(selectedType) {
                        viewModel.selectLevel(maxUnlockedLevel)
                    }

                    val selectedLevel by viewModel.selectedLevel.collectAsState()

                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Target Neural Level",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = IceText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = "Highest Unlocked: Lvl $maxUnlockedLevel",
                                        color = NeonGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                // Interactive Stepper Component
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.adjustSelectedLevel(-1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CosmicDarkBg)
                                            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Decrease Level", tint = IceText, modifier = Modifier.size(16.dp))
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .widthIn(min = 60.dp)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CosmicDarkBg)
                                            .border(1.dp, SleekIndigo, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$selectedLevel",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.adjustSelectedLevel(1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CosmicDarkBg)
                                            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Increase Level", tint = IceText, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive scroll/slider to jump quickly across the 10,000 level span
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Lvl 1",
                                    color = SlateText,
                                    fontSize = 10.sp
                                )
                                Slider(
                                    value = selectedLevel.toFloat(),
                                    onValueChange = { viewModel.selectLevel(it.toInt()) },
                                    valueRange = 1f..10000f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = SleekPurple,
                                        activeTrackColor = SleekIndigo,
                                        inactiveTrackColor = Color(0xFF1E293B)
                                    )
                                )
                                Text(
                                    text = "10K+",
                                    color = SlateText,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Jump Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.selectLevel(maxUnlockedLevel) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkBg),
                                    border = BorderStroke(1.dp, BorderGlass),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text(text = "MAX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                                }

                                Button(
                                    onClick = { viewModel.selectLevel(500) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkBg),
                                    border = BorderStroke(1.dp, BorderGlass),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text(text = "Lvl 500", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonYellow)
                                }

                                Button(
                                    onClick = { viewModel.selectLevel(5000) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkBg),
                                    border = BorderStroke(1.dp, BorderGlass),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text(text = "Lvl 5000", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonMagenta)
                                }

                                Button(
                                    onClick = { viewModel.selectLevel(10000) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkBg),
                                    border = BorderStroke(1.dp, BorderGlass),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    modifier = Modifier.weight(1f).height(32.dp)
                                ) {
                                    Text(text = "Lvl 10000", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                                }
                            }
                            
                            // Visual prompt if selected level is above max unlocked level (premium feel!)
                            if (selectedLevel > maxUnlockedLevel) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonMagenta.copy(alpha = 0.08f))
                                        .border(1.dp, NeonMagenta.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = "Warning",
                                        tint = NeonMagenta,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WARPING INCOGNITO: Level exceeds unlocked limit. Face extreme trials!",
                                        color = NeonMagenta,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Difficulty selector section
                item {
                    Text(
                        text = "ADAPTIVE STRETCH FACTOR",
                        color = SlateText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(Difficulty.entries) { diff ->
                    val isSelected = selectedDifficulty == diff
                    val hasUnlocked = progress.stars >= diff.starsNeeded
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) CosmicDarkSurface else CosmicDarkCard.copy(alpha = 0.3f))
                            .border(
                                1.dp,
                                if (isSelected && hasUnlocked) SleekIndigo else BorderGlass,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = hasUnlocked) { selectedDifficulty = diff }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (hasUnlocked) Icons.Rounded.VerifiedUser else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (hasUnlocked) NeonGreen else SlateText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = diff.name,
                                    color = if (hasUnlocked) IceText else SlateText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Multiplier: x${diff.multiplier}",
                                    color = SlateText,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (!hasUnlocked) {
                            Text(
                                text = "Req: ${diff.starsNeeded} ⭐",
                                color = NeonMagenta,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Active",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Floating Launch Button at bottom (Sleek Interface Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { viewModel.startGame(selectedType) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(SleekIndigo, SleekPurple)), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LAUNCH NEURAL QUEST",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
