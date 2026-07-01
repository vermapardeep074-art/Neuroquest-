package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlin.random.Random
import com.example.data.UserProgress
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = BorderGlass,
    backgroundColor: Color = CosmicDarkCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
            .padding(16.dp),
        content = content
    )
}

@Composable
fun TopHUD(
    progress: UserProgress,
    onStoreClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val initials = if (progress.username.length >= 2) progress.username.substring(0, 2).uppercase() else "JD"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1F0B0E14))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Info: Rounded-2xl gradient avatar block with XP indicator
        Row(
            modifier = Modifier
                .clickable { onProfileClick() }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(SleekIndigo, SleekPurple)))
                    .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "LEVEL ${progress.level}",
                    color = SleekIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val xpNeeded = progress.level * 100
                    val progressPct = (progress.xp.toFloat() / xpNeeded).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressPct)
                                .background(Brush.horizontalGradient(listOf(SleekIndigo, SleekCyan)))
                        )
                    }
                    Text(
                        text = "${progress.xp} XP",
                        color = SlateText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Stats Display Pill Badges with border-white/10 thin translucent borders
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coins Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x0DFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
                    .clickable { onStoreClick() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💎", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = progress.coins.toString(), color = IceText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            // Stars Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x0DFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⭐", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = progress.stars.toString(), color = IceText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            // Energy Heart Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x0DFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Energy",
                    tint = NeonMagenta,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${progress.energy}",
                    color = IceText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun HudStatBadge(
    icon: ImageVector,
    value: String,
    tint: Color,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x22FFFFFF))
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            color = IceText,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

// COGNITIVE RADAR CHART (Canvas Spider diagram)
@Composable
fun CognitiveRadarChart(
    progress: UserProgress,
    modifier: Modifier = Modifier
) {
    val scores = listOf(
        progress.memoryScore,
        progress.iqScore,
        progress.reactionScore.let { if (it == 60f) 50f else (600f - it).coerceIn(0f, 600f) / 6f }, // scale reaction nicely
        progress.focusScore,
        progress.accuracyScore
    )
    val labels = listOf("Memory", "IQ Math", "Reaction", "Focus", "Accuracy")

    Canvas(modifier = modifier.fillMaxWidth()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.3f
        val count = 5
        val angleStep = (2 * Math.PI / count)

        // Draw background rings
        for (ring in 1..4) {
            val ringRadius = radius * (ring / 4f)
            val ringPath = Path()
            for (i in 0 until count) {
                val angle = i * angleStep - Math.PI / 2
                val x = center.x + ringRadius * cos(angle).toFloat()
                val y = center.y + ringRadius * sin(angle).toFloat()
                if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
            }
            ringPath.close()
            drawPath(
                path = ringPath,
                color = SlateText.copy(alpha = 0.2f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Draw grid spokes (axes)
        for (i in 0 until count) {
            val angle = i * angleStep - Math.PI / 2
            val endX = center.x + radius * cos(angle).toFloat()
            val endY = center.y + radius * sin(angle).toFloat()
            drawLine(
                color = SlateText.copy(alpha = 0.3f),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Plot User Scores
        val scorePath = Path()
        val scorePoints = mutableListOf<Offset>()
        for (i in 0 until count) {
            val angle = i * angleStep - Math.PI / 2
            val scorePct = (scores.getOrElse(i) { 50f } / 100f).coerceIn(0.1f, 1.0f)
            val pointRadius = radius * scorePct
            val px = center.x + pointRadius * cos(angle).toFloat()
            val py = center.y + pointRadius * sin(angle).toFloat()
            scorePoints.add(Offset(px, py))
            if (i == 0) scorePath.moveTo(px, py) else scorePath.lineTo(px, py)
        }
        scorePath.close()

        // Draw filled polygon
        drawPath(
            path = scorePath,
            brush = Brush.radialGradient(
                colors = listOf(NeonCyan.copy(alpha = 0.4f), NeonPurple.copy(alpha = 0.4f)),
                center = center,
                radius = radius
            )
        )
        // Draw polygon outline
        drawPath(
            path = scorePath,
            color = NeonCyan,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw score nodes
        for (point in scorePoints) {
            drawCircle(
                color = NeonMagenta,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = IceText,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}

// LUCKY WHEEL (Drawn on Canvas)
@Composable
fun LuckyWheel(
    isSpinning: Boolean,
    onSpinFinished: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    
    val prizes = listOf(
        "50 Coins" to Pair("Coins", 50),
        "20 XP" to Pair("XP", 20),
        "1 Hint" to Pair("Hints", 1),
        "200 Coins" to Pair("Coins", 200),
        "1 Heart" to Pair("Energy", 1),
        "2 Hints" to Pair("Hints", 2),
        "100 Coins" to Pair("Coins", 100),
        "50 XP" to Pair("XP", 50)
    )

    val colors = listOf(
        CosmicDarkSurface, CosmicDarkCard,
        CosmicDarkSurface, CosmicDarkCard,
        CosmicDarkSurface, CosmicDarkCard,
        CosmicDarkSurface, CosmicDarkCard
    )

    // Smooth physics spin rotation animation
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = if (isSpinning) {
            infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessVeryLow)
        },
        label = "wheelRotation"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .clip(CircleShape)
            .border(4.dp, Brush.horizontalGradient(listOf(NeonCyan, NeonMagenta)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(animatedRotation)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            val sweepAngle = 360f / prizes.size

            for (i in prizes.indices) {
                // Draw Segment Arc
                drawArc(
                    color = colors[i],
                    startAngle = i * sweepAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = size
                )

                // Divider line
                val angleRad = Math.toRadians((i * sweepAngle).toDouble())
                val endX = center.x + radius * cos(angleRad).toFloat()
                val endY = center.y + radius * sin(angleRad).toFloat()
                drawLine(
                    color = BorderGlass,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Outer decorative ring dots
            for (i in 0 until 16) {
                val dotRad = Math.toRadians((i * (360f / 16)).toDouble())
                val dotX = center.x + (radius - 8.dp.toPx()) * cos(dotRad).toFloat()
                val dotY = center.y + (radius - 8.dp.toPx()) * sin(dotRad).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) NeonCyan else NeonMagenta,
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Static Center pointer
        Icon(
            imageVector = Icons.Rounded.Navigation,
            contentDescription = "Pointer",
            tint = GoldAccent,
            modifier = Modifier
                .size(48.dp)
                .rotate(180f)
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
        )

        // Center hub button
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(NeonCyan, NeonPurple)))
                .border(2.dp, IceText, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Casino,
                contentDescription = "Spin",
                tint = IceText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Spin trigger
    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            rotationAngle += 1440f + Random.nextInt(0, 360)
        } else if (rotationAngle > 0f) {
            // Calculate which prize the pointer landed on (Pointer is at top / 270 degrees)
            val normalizedAngle = (360f - (rotationAngle % 360f) + 270f) % 360f
            val sectorIndex = ((normalizedAngle / (360f / prizes.size)).toInt()) % prizes.size
            val landedPrize = prizes[sectorIndex]
            delay(1000)
            onSpinFinished(landedPrize.second.first, landedPrize.second.second)
        }
    }
}

// Particle/Confetti Effect overlay
@Composable
fun ConfettiEffect(trigger: Boolean) {
    if (!trigger) return
    var activeParticles by remember { mutableStateOf(true) }

    LaunchedEffect(trigger) {
        activeParticles = true
        delay(2500)
        activeParticles = false
    }

    if (activeParticles) {
        Box(modifier = Modifier.fillMaxSize()) {
            repeat(30) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "confetti")
                
                val startX = Random.nextFloat() * LocalConfiguration.current.screenWidthDp
                val animY by infiniteTransition.animateFloat(
                    initialValue = -50f,
                    targetValue = LocalConfiguration.current.screenHeightDp.toFloat() + 50f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500 + (index * 40), easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "confettiY"
                )
                val animRot by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200 + index * 50, easing = LinearEasing)
                    ),
                    label = "confettiRot"
                )

                val color = listOf(NeonCyan, NeonMagenta, NeonGreen, NeonYellow, NeonPurple).random()

                Box(
                    modifier = Modifier
                        .offset(x = startX.dp, y = animY.dp)
                        .rotate(animRot)
                        .size(Random.nextInt(8, 16).dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
