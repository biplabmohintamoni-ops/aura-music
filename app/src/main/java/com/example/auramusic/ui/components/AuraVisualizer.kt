package com.example.auramusic.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.auramusic.ui.theme.AuraPrimary
import kotlin.math.abs

@Composable
fun AuraVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = AuraPrimary,
    primaryColor: Color = barColor,
    secondaryColor: Color = barColor,
    barCount: Int = 20,
    height: Dp = 32.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_anim")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase3"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barSpacing = 3.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((totalWidth - totalSpacing) / barCount).coerceAtLeast(2f)

        for (i in 0 until barCount) {
            val factor = when (i % 3) {
                0 -> phase1
                1 -> phase2
                else -> phase3
            }

            val centerRatio = 1f - (abs(i - barCount / 2f) / (barCount / 2f)) * 0.3f
            val calculatedHeight = if (isPlaying) {
                (canvasHeight * factor * centerRatio).coerceIn(3.dp.toPx(), canvasHeight)
            } else {
                4.dp.toPx()
            }

            val x = i * (barWidth + barSpacing)
            val y = (canvasHeight - calculatedHeight) / 2f

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, calculatedHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
