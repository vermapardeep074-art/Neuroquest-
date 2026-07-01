package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProgress
import com.example.model.Achievement
import com.example.model.GameType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: GameViewModel,
    progress: UserProgress
) {
    var activeSubTab by remember { mutableStateOf("Training") }
    var showLuckyWheelDialog by remember { mutableStateOf(false) }
    var isWheelSpinning by remember { mutableStateOf(false) }
    var wheelRewardMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDarkBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // High-fidelity Top HUD
            TopHUD(
                progress = progress,
                onStoreClick = { activeSubTab = "Store" },
                onProfileClick = { activeSubTab = "Profile" }
            )

            // Dynamic Sub-Panes based on tab selection
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeSubTab) {
                    "Training" -> TrainingSubPane(viewModel, progress, onLuckyWheelTrigger = { showLuckyWheelDialog = true })
                    "Stats" -> StatsSubPane(progress)
                    "Achievements" -> AchievementsSubPane(progress)
                    "Store" -> StoreSubPane(viewModel, progress)
                    "Profile" -> ProfileSubPane(viewModel, progress)
                }
            }

            // Bottom Navigation Bar with Sleek Interface style (bg-[#161B22]/80, border-t border-white/10)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicDarkSurface.copy(alpha = 0.85f))
                    .border(BorderStroke(1.dp, Color(0x1AFFFFFF)))
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavTabButton(
                        label = "Home",
                        icon = Icons.Rounded.Home,
                        isActive = activeSubTab == "Training",
                        onClick = { activeSubTab = "Training" }
                    )
                    BottomNavTabButton(
                        label = "Stats",
                        icon = Icons.Rounded.TrendingUp,
                        isActive = activeSubTab == "Stats",
                        onClick = { activeSubTab = "Stats" }
                    )
                    BottomNavTabButton(
                        label = "Rank",
                        icon = Icons.Rounded.EmojiEvents,
                        isActive = activeSubTab == "Achievements",
                        onClick = { activeSubTab = "Achievements" }
                    )
                    BottomNavTabButton(
                        label = "Store",
                        icon = Icons.Rounded.Storefront,
                        isActive = activeSubTab == "Store",
                        onClick = { activeSubTab = "Store" }
                    )
                    BottomNavTabButton(
                        label = "Settings",
                        icon = Icons.Rounded.Settings,
                        isActive = activeSubTab == "Profile",
                        onClick = { activeSubTab = "Profile" }
                    )
                }
            }
        }

        // LUCKY WHEEL DIALOG DICTIONARY
        if (showLuckyWheelDialog) {
            AlertDialog(
                onDismissRequest = { if (!isWheelSpinning) showLuckyWheelDialog = false },
                title = {
                    Text(
                        text = "LUCKY SHUTTLE SPIN",
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LuckyWheel(
                            isSpinning = isWheelSpinning,
                            onSpinFinished = { type, amt ->
                                isWheelSpinning = false
                                wheelRewardMessage = "Congratulations! You won $amt $type!"
                            },
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (wheelRewardMessage != null) {
                            Text(
                                text = wheelRewardMessage!!,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = "Spin the celestial cosmos wheel for daily booster items!",
                                color = SlateText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (wheelRewardMessage != null) {
                            Button(
                                onClick = {
                                    showLuckyWheelDialog = false
                                    wheelRewardMessage = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                            ) {
                                Text("CLAIM", color = CosmicDarkBg, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (!isWheelSpinning) {
                                        isWheelSpinning = true
                                    }
                                },
                                enabled = !isWheelSpinning,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta)
                            ) {
                                Text("SPIN", color = IceText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                containerColor = CosmicDarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun BottomNavTabButton(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 1.05f else 1.0f, label = "tabScale")
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 24.dp)
                    .clip(CircleShape)
                    .background(if (isActive) SleekIndigo.copy(alpha = 0.15f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive) SleekIndigo else SlateText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                color = if (isActive) SleekIndigo else SlateText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ----------------------------------------------------------------------------------
// SUB-PANES
// ----------------------------------------------------------------------------------

@Composable
fun TrainingSubPane(
    viewModel: GameViewModel,
    progress: UserProgress,
    onLuckyWheelTrigger: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Welcoming Greeting & Player Level Status
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome, Commander",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = SlateText,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = progress.username,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = IceText,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                // XP Progress Bar
                val progressPct = (progress.xp.toFloat() / (progress.level * 100f)).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "XP progress", color = SlateText, fontSize = 11.sp)
                    Text(text = "${progress.xp}/${progress.level * 100}", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressPct },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonCyan,
                    trackColor = Color(0x22FFFFFF)
                )
            }
        }

        // Daily Goal Progress Card (Tracks user activity & displays progress towards reward)
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Goal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = IceText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Complete 3 quests to claim daily intelligence bounty!",
                            color = SlateText,
                            fontSize = 12.sp
                        )
                    }
                    // Bounty Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "💎 +100", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "💡 +2", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                val goalProgress = progress.dailyGoalCount.toFloat() / progress.dailyGoalTarget.toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Quest Progress",
                        color = SlateText,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${progress.dailyGoalCount} / ${progress.dailyGoalTarget}",
                        color = SleekIndigo,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(goalProgress.coerceIn(0f, 1f))
                            .background(Brush.horizontalGradient(listOf(SleekIndigo, SleekPurple)))
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                if (progress.dailyGoalClaimed) {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0x11FFFFFF),
                            disabledContentColor = SlateText
                        ),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Completed",
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bounty Claimed! Resetting Tomorrow",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (progress.dailyGoalCount >= progress.dailyGoalTarget) {
                    Button(
                        onClick = { viewModel.claimDailyGoalReward() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(SleekIndigo, SleekPurple)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CLAIM DAILY REWARD",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.navigateTo("GameSelection") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x0AFFFFFF),
                            contentColor = IceText
                        ),
                        border = BorderStroke(1.dp, BorderGlass),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Text(
                            text = "LAUNCH NEURAL QUESTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Daily Challenge Hero Card (Sleek Interface Style)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0x33312E81), Color(0x33581C87)) // from-indigo-900/40 to-purple-900/40 equivalent
                        )
                    )
                    .border(1.dp, Color(0x4D6366F1), RoundedCornerShape(32.dp)) // border-indigo-500/30
                    .clickable { viewModel.startGame(GameType.entries.random()) }
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Daily Quest",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Boost your Focus & Memory",
                        color = SlateText,
                        fontSize = 13.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Overlapping circle avatars representing dynamic teammates or level completions
                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SleekCyan)
                                    .border(2.dp, CosmicDarkBg, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SleekIndigo)
                                    .border(2.dp, CosmicDarkBg, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SleekPurple)
                                    .border(2.dp, CosmicDarkBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+5",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Level tracker indicator
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Level ${progress.level}",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "CONTINUE GAME",
                                color = SleekIndigo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.startGame(GameType.entries.random()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(SleekIndigo, SleekPurple)), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "START TRAINING",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Secondary triggers (Lucky Wheel & Explorer)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassmorphicCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLuckyWheelTrigger() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Casino,
                        contentDescription = "Wheel",
                        tint = GoldAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Celestial Spin", color = IceText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Daily free rewards", color = SlateText, fontSize = 10.sp)
                }

                GlassmorphicCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo("GameSelection") }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = "Explorer",
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mental Explorer", color = IceText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Choose discipline", color = SlateText, fontSize = 10.sp)
                }
            }
        }

        // mental disciplines header
        item {
            Text(
                text = "NEURAL DISCIPLINES",
                color = SlateText,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // list five major games quickly
        items(GameType.entries) { type ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicDarkSurface)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .clickable { viewModel.startGame(type) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (color, icon) = when (type) {
                    GameType.MEMORY -> Pair(NeonCyan, Icons.Rounded.GridOn)
                    GameType.MATH -> Pair(NeonYellow, Icons.Rounded.Calculate)
                    GameType.REACTION -> Pair(NeonMagenta, Icons.Rounded.TouchApp)
                    GameType.FOCUS -> Pair(NeonGreen, Icons.Rounded.Visibility)
                    GameType.SEQUENCE -> Pair(NeonPurple, Icons.Rounded.Repeat)
                }

                val gameLvl = when (type) {
                    GameType.MEMORY -> progress.memoryLevel
                    GameType.MATH -> progress.mathLevel
                    GameType.REACTION -> progress.reactionLevel
                    GameType.FOCUS -> progress.focusLevel
                    GameType.SEQUENCE -> progress.sequenceLevel
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.15f))
                        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = type.displayName, color = IceText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(color.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Lvl $gameLvl", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = type.description, color = SlateText, fontSize = 11.sp)
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = SlateText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StatsSubPane(progress: UserProgress) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "COGNITIVE FOOTPRINT",
                color = IceText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Radar chart
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                CognitiveRadarChart(
                    progress = progress,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Stats summary list
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("COGNITIVE METRICS", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                StatSummaryRow("Games Completed", progress.gamesPlayed.toString(), Icons.Rounded.History, NeonCyan)
                Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 8.dp))
                StatSummaryRow("Average Accuracy", "${progress.accuracyScore.toInt()}%", Icons.Rounded.CheckCircle, NeonGreen)
                Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 8.dp))
                StatSummaryRow("Memory Score", progress.memoryScore.toInt().toString(), Icons.Rounded.GridOn, NeonCyan)
                Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 8.dp))
                StatSummaryRow("IQ Math Factor", progress.iqScore.toInt().toString(), Icons.Rounded.Calculate, NeonYellow)
                Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 8.dp))
                StatSummaryRow("Best Reaction Time", if (progress.reactionScore == 60f) "N/A" else "${progress.reactionScore.toInt()}ms", Icons.Rounded.Timer, NeonMagenta)
            }
        }
    }
}

@Composable
fun StatSummaryRow(label: String, value: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = SlateText, fontSize = 13.sp)
        }
        Text(text = value, color = IceText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun AchievementsSubPane(progress: UserProgress) {
    val unlockedSet = progress.unlockedAchievements.split(",").toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "MEDAL OF COGNITION",
                color = IceText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Unlock milestones to secure bonus stellar coins.",
                color = SlateText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(Achievement.ALL) { ach ->
            val isUnlocked = unlockedSet.contains(ach.id)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isUnlocked) CosmicDarkSurface else CosmicDarkCard.copy(alpha = 0.5f))
                    .border(
                        1.dp,
                        if (isUnlocked) NeonCyan.copy(alpha = 0.5f) else BorderGlass,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isUnlocked) NeonCyan.copy(alpha = 0.2f) else Color(0x11FFFFFF))
                        .border(
                            1.dp,
                            if (isUnlocked) NeonCyan else SlateText.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (ach.iconName) {
                        "play_arrow" -> Icons.Rounded.PlayArrow
                        "fitness_center" -> Icons.Rounded.FitnessCenter
                        "psychology" -> Icons.Rounded.Psychology
                        "star" -> Icons.Rounded.Star
                        "workspace_premium" -> Icons.Rounded.WorkspacePremium
                        "local_fire_department" -> Icons.Rounded.LocalFireDepartment
                        "whatshot" -> Icons.Rounded.Whatshot
                        "monetization_on" -> Icons.Rounded.MonetizationOn
                        else -> Icons.Rounded.GpsFixed
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isUnlocked) GoldAccent else SlateText,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ach.title,
                        color = if (isUnlocked) IceText else SlateText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = ach.description,
                        color = SlateText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.MonetizationOn,
                            contentDescription = "Reward",
                            tint = NeonYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${ach.rewardCoins} Coins",
                            color = NeonYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                
                // Status checkmark
                if (isUnlocked) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Unlocked",
                        tint = NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        tint = SlateText.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StoreSubPane(viewModel: GameViewModel, progress: UserProgress) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "COSMIC BOOSTER DEPOT",
                color = IceText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Use stellar credits to acquire hints and instant life capsules.",
                color = SlateText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Booster 1: Energy Refill
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicDarkSurface)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonMagenta.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Hearts",
                        tint = NeonMagenta,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Instant Energy Capsule", color = IceText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Completely refill your energy hearts.", color = SlateText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MonetizationOn, null, tint = NeonYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("50 Coins", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = { viewModel.buyEnergyRefill() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                    enabled = progress.energy < progress.maxEnergy && progress.coins >= 50
                ) {
                    Text("BUY", color = IceText, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Booster 2: Hint Pack
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicDarkSurface)
                    .border(1.dp, BorderGlass, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = "Hints",
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stellar Hint Bundle", color = IceText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Acquire 3 helper hints to bypass tough patterns.", color = SlateText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MonetizationOn, null, tint = NeonYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("100 Coins", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = { viewModel.buyHintPack() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    enabled = progress.coins >= 100
                ) {
                    Text("BUY", color = CosmicDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileSubPane(viewModel: GameViewModel, progress: UserProgress) {
    var newUsername by remember { mutableStateOf(progress.username) }
    var selectedAvatar by remember { mutableStateOf(progress.avatarId) }

    val avatars = listOf("avatar_neon_brain", "avatar_quantum", "avatar_hyperion", "avatar_nebula")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "NEURAL CENTER & SETTINGS",
                color = IceText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Customize username & avatar card
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("CUSTOMIZE COMMANDER", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                TextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Commander Identifier") },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = IceText,
                        unfocusedTextColor = IceText,
                        focusedContainerColor = CosmicDarkSurface,
                        unfocusedContainerColor = CosmicDarkSurface,
                        cursorColor = NeonCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Select Celestial Avatar Profile", color = SlateText, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    avatars.forEach { avatar ->
                        val isSelected = selectedAvatar == avatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonCyan else Color(0x22FFFFFF))
                                .border(
                                    2.dp,
                                    if (isSelected) NeonCyan else BorderGlass,
                                    CircleShape
                                )
                                .clickable { selectedAvatar = avatar }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = if (isSelected) CosmicDarkBg else IceText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updateProfile(newUsername, selectedAvatar, progress.country)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SAVE PROFILE DETAILS", color = CosmicDarkBg, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sound, Music, Haptic, Vibration Settings Card
        item {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text("AUDIO & TELEMETRY SETTINGS", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Synthesizer Sound Effects", color = IceText, fontSize = 13.sp)
                    Switch(
                        checked = progress.soundEnabled,
                        onCheckedChange = { viewModel.updateSettings(progress.musicEnabled, it, progress.vibrationEnabled, progress.darkModeEnabled) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
                Divider(color = BorderGlass, modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Haptic Screen Vibrations", color = IceText, fontSize = 13.sp)
                    Switch(
                        checked = progress.vibrationEnabled,
                        onCheckedChange = { viewModel.updateSettings(progress.musicEnabled, progress.soundEnabled, it, progress.darkModeEnabled) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }
            }
        }

        // Reset progress dangerous action
        item {
            Button(
                onClick = { viewModel.resetProgress() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, NeonMagenta),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("WIPE ENTIRE PROGRESS HISTORY", color = NeonMagenta, fontWeight = FontWeight.Bold)
            }
        }
    }
}
