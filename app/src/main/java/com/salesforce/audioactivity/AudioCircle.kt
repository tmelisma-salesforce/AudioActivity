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
 * which scales based on an activity level using spring physics.
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
    val scale by animateFloatAsState(
        targetValue = 1.0f + safeLevel * 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CircleScale"
    )

    // --- Infinite Transition for Continuous Animations ---
    val infiniteTransition = rememberInfiniteTransition(label = "CircleEffects")

    // 1. Gradient Angle Rotation Animation (-15 to +15 degrees)
    val angle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing), // 3 seconds per sweep
            repeatMode = RepeatMode.Reverse // Go back and forth
        ),
        label = "GradientAngle"
    )

    // 2. Color Cycling Animation
    val colorCycleDuration = 2500 // Milliseconds to transition between colors in the full cycle
    val numColors = cyclingPalette.size

    // Animate a float value from 0f to numColors to represent progress through the palette
    val colorProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = numColors.toFloat(),
        animationSpec = infiniteRepeatable(
            // Duration is total time to cycle through ALL colors once
            animation = tween(durationMillis = colorCycleDuration * numColors, easing = LinearEasing),
            // *** FIX: Use RepeatMode.Restart to loop the cycle ***
            repeatMode = RepeatMode.Restart
        ),
        label = "ColorProgress"
    )

    // Calculate current index and next index based on the float progress
    val startIndex = colorProgress.toInt() % numColors
    val nextIndex = (startIndex + 1) % numColors
    // Calculate how far we are (0.0 to 1.0) between the current and next color
    val lerpFraction = colorProgress - colorProgress.toInt()

    // Calculate the smoothly interpolated start color
    val animatedStartColor = androidx.compose.ui.graphics.lerp(
        cyclingPalette[startIndex],
        cyclingPalette[nextIndex],
        lerpFraction
    )

    // Calculate the end color index (offset from start) and interpolate it similarly
    // Ensure the offset doesn't exceed numColors, use modulo
    val endColorOffset = numColors / 3 // Example offset
    val endIndex = (startIndex + endColorOffset) % numColors
    val nextEndIndex = (endIndex + 1) % numColors

    // We need to lerp the end color based on the same lerpFraction
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
                val angleRad = Math.toRadians(angle.toDouble()) // Use animated angle
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Calculate gradient start/end points based on angle and radius
                // Adjust signs if rotation direction is wrong
                val startOffset = Offset(
                    center.x - (radius * sin(angleRad)).toFloat(),
                    center.y + (radius * cos(angleRad)).toFloat()
                )
                val endOffset = Offset(
                    center.x + (radius * sin(angleRad)).toFloat(),
                    center.y - (radius * cos(angleRad)).toFloat()
                )

                // Create the linear gradient brush with animated colors and offsets
                val brush = Brush.linearGradient(
                    colors = listOf(animatedStartColor, animatedEndColor), // Use animated colors
                    start = startOffset,
                    end = endOffset
                )

                // Draw the circle shape and fill it with the calculated brush
                drawCircle(brush = brush, radius = radius, center = center)
            }
    )
}
