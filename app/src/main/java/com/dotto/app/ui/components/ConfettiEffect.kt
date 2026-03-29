package com.dotto.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val PARTICLE_COUNT = 6

private data class Particle(
    val angle: Float,       // radians
    val maxRadius: Float,   // px
    val size: Float,        // px
    val isCircle: Boolean,
    val color: Color
)

@Composable
fun ConfettiEffect(
    color: Color,
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    val particles = remember(trigger) {
        if (!trigger) emptyList()
        else List(PARTICLE_COUNT) { i ->
            val baseAngle = (2f * Math.PI.toFloat() / PARTICLE_COUNT) * i
            val jitter = Random.nextFloat() * 0.4f - 0.2f
            Particle(
                angle = baseAngle + jitter,
                maxRadius = Random.nextFloat() * 30f + 25f,
                size = Random.nextFloat() * 4f + 2f,
                isCircle = Random.nextBoolean(),
                color = color.copy(
                    red = (color.red + Random.nextFloat() * 0.15f - 0.075f).coerceIn(0f, 1f),
                    green = (color.green + Random.nextFloat() * 0.15f - 0.075f).coerceIn(0f, 1f),
                    blue = (color.blue + Random.nextFloat() * 0.15f - 0.075f).coerceIn(0f, 1f)
                )
            )
        }
    }

    val progress = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger) {
            progress.snapTo(0f)
            alpha.snapTo(0.85f)
            launch {
                progress.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                alpha.animateTo(
                    0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }

    if (particles.isNotEmpty() && alpha.value > 0.01f) {
        Canvas(modifier = modifier) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val currentProgress = progress.value
            val currentAlpha = alpha.value

            particles.forEach { p ->
                val radius = p.maxRadius * currentProgress
                val x = center.x + cos(p.angle) * radius
                val y = center.y + sin(p.angle) * radius
                val drawColor = p.color.copy(alpha = currentAlpha)

                if (p.isCircle) {
                    drawCircle(
                        color = drawColor,
                        radius = p.size,
                        center = Offset(x, y)
                    )
                } else {
                    drawRect(
                        color = drawColor,
                        topLeft = Offset(x - p.size / 2, y - p.size / 2),
                        size = Size(p.size, p.size)
                    )
                }
            }
        }
    }
}
