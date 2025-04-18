package com.salesforce.audioactivity // Use your actual package name

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salesforce.audioactivity.ui.theme.cyclingPalette // Import the palette list
import kotlin.math.cos
import kotlin.math.sin

/**
 * A composable function that displays a circle with a rotating, color-cycling gradient background
 * which scales dramatically based on an activity level using spring physics.
 *
 * @param activityLevel The level of activity (expected 0.0f to 1.0f), controls the pulse scale.
 * @param baseSize The default diameter of the circle when activityLevel is 0.
 * @param modifier Modifier for this composable.
 */
@Composable
fun AudioCircle(
    activityLevel: Float,
    baseSize: Dp = 150.dp,
    modifier: Modifier = Modifier
) {
    // --- Activity Level Scaling Animation ---
    val safeLevel = activityLevel.coerceIn(0f, 1f)

    val minScale = 0.8f
    val maxScale = 1.8f

    val scale by animateFloatAsState(
        targetValue = minScale + safeLevel * (maxScale - minScale),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            // Increased stiffness for faster visual response
            stiffness = Spring.StiffnessMedium // <<<=== ADJUSTED STIFFNESS
        ),
        label = "CircleScale"
    )

    // --- Infinite Transition for Continuous Background Effects ---
    val infiniteTransition = rememberInfiniteTransition(label = "CircleEffects")

    // 1. Gradient Angle Rotation Animation (-15 to +15 degrees)
    val angle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GradientAngle"
    )

    // 2. Color Cycling Animation
    val colorCycleDuration = 2500
    val numColors = cyclingPalette.size
    val colorProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = numColors.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = colorCycleDuration * numColors, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ColorProgress"
    )

    val startIndex = colorProgress.toInt() % numColors
    val nextIndex = (startIndex + 1) % numColors
    val lerpFraction = colorProgress - colorProgress.toInt()

    val animatedStartColor = androidx.compose.ui.graphics.lerp(
        cyclingPalette[startIndex],
        cyclingPalette[nextIndex],
        lerpFraction
    )

    val endColorOffset = numColors / 3
    val endIndex = (startIndex + endColorOffset) % numColors
    val nextEndIndex = (endIndex + 1) % numColors
    val animatedEndColor = androidx.compose.ui.graphics.lerp(
        cyclingPalette[endIndex],
        cyclingPalette[nextEndIndex],
        lerpFraction
    )


    // --- Drawing ---
    Box(
        modifier = modifier
            .size(baseSize) // Apply the base size first
            .scale(scale) // Apply the activity scaling
            .drawBehind { // Use drawBehind for custom gradient drawing
                val angleRad = Math.toRadians(angle.toDouble())
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                val startOffset = Offset(
                    center.x - (radius * sin(angleRad)).toFloat(),
                    center.y + (radius * cos(angleRad)).toFloat()
                )
                val endOffset = Offset(
                    center.x + (radius * sin(angleRad)).toFloat(),
                    center.y - (radius * cos(angleRad)).toFloat()
                )

                val brush = Brush.linearGradient(
                    colors = listOf(animatedStartColor, animatedEndColor),
                    start = startOffset,
                    end = endOffset
                )

                drawCircle(brush = brush, radius = radius, center = center)
            }
    )
}
