package com.example.perudo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.perudo.DieItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class CubeFaceValues(
    val front: Int,
    val back: Int,
    val top: Int,
    val bottom: Int,
    val right: Int,
    val left: Int
)

private fun getCubeFaceValues(target: Int): CubeFaceValues {
    return when (target) {
        1 -> CubeFaceValues(front = 1, back = 6, top = 2, bottom = 5, right = 3, left = 4)
        2 -> CubeFaceValues(front = 2, back = 5, top = 1, bottom = 6, right = 3, left = 4)
        3 -> CubeFaceValues(front = 3, back = 4, top = 1, bottom = 6, right = 2, left = 5)
        4 -> CubeFaceValues(front = 4, back = 3, top = 1, bottom = 6, right = 5, left = 2)
        5 -> CubeFaceValues(front = 5, back = 2, top = 1, bottom = 6, right = 4, left = 3)
        6 -> CubeFaceValues(front = 6, back = 1, top = 5, bottom = 2, right = 3, left = 4)
        else -> CubeFaceValues(front = 1, back = 6, top = 2, bottom = 5, right = 3, left = 4)
    }
}

@Composable
fun DiceView(
    die: DieItem,
    isRolling: Boolean,
    staggerIndex: Int,
    onClickToRemove: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    var displayedValue by remember { mutableIntStateOf(die.value) }

    // 3D Animatable properties (flat 0f when resting)
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val rotZ = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val translationY = remember { Animatable(0f) }

    // Keep displayed value synced when not rolling
    LaunchedEffect(die.value) {
        if (!isRolling) {
            displayedValue = die.value
        }
    }

    // Trigger 3D tumble rolling animations ONLY when rolling
    LaunchedEffect(isRolling) {
        if (isRolling) {
            val delayMs = (staggerIndex * 80L).coerceAtMost(400L)
            delay(delayMs)

            // Random face cycling coroutine while rolling
            launch {
                val startTime = System.currentTimeMillis()
                val duration = 520L
                while (System.currentTimeMillis() - startTime < duration) {
                    displayedValue = Random.nextInt(1, 7)
                    delay(45L)
                }
                displayedValue = die.value
            }

            // 3D Motion & Tumble coroutines
            launch {
                // High 3D Jump & bouncy landing
                launch {
                    translationY.animateTo(
                        targetValue = -70f,
                        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                    )
                    translationY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                // 3D Scale expansion in air
                launch {
                    scale.animateTo(
                        targetValue = 1.35f,
                        animationSpec = tween(durationMillis = 250)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }

                // 3D rotations on X, Y, and Z axes
                launch {
                    rotX.animateTo(
                        targetValue = 720f,
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
                    )
                    rotX.snapTo(0f)
                }
                launch {
                    rotY.animateTo(
                        targetValue = 1080f,
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
                    )
                    rotY.snapTo(0f)
                }
                launch {
                    rotZ.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
                    )
                    rotZ.snapTo(0f)
                }
            }
        } else {
            // Ensure flat resting state when not rolling
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            rotZ.snapTo(0f)
            scale.snapTo(1f)
            translationY.snapTo(0f)
            displayedValue = die.value
        }
    }

    val rxRad = Math.toRadians(rotX.value.toDouble())
    val ryRad = Math.toRadians(rotY.value.toDouble())

    // Normal Z vector calculation towards camera for 3D face visibility
    val frontZ = cos(rxRad) * cos(ryRad)
    val backZ = -cos(rxRad) * cos(ryRad)
    val rightZ = cos(rxRad) * sin(ryRad)
    val leftZ = -cos(rxRad) * sin(ryRad)
    val topZ = -sin(rxRad)
    val bottomZ = sin(rxRad)

    val faceValues = getCubeFaceValues(displayedValue)

    val baseFaceColor = MaterialTheme.colorScheme.surfaceVariant
    val topLightFaceColor = MaterialTheme.colorScheme.surface
    val sideShadowFaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                this.rotationX = rotX.value
                this.rotationY = rotY.value
                this.rotationZ = rotZ.value
                this.scaleX = scale.value
                this.scaleY = scale.value
                this.translationY = translationY.value
                // Lower camera distance gives dramatic 3D perspective during rotation
                this.cameraDistance = 8 * density
            }
            .clickable(enabled = !isRolling, onClick = onClickToRemove),
        contentAlignment = Alignment.Center
    ) {
        // 1. Front Face
        CubeFace(
            value = faceValues.front,
            size = size,
            containerColor = baseFaceColor,
            visible = frontZ > -0.05f,
            transform = {
                transformOrigin = TransformOrigin.Center
                rotationY = 0f
            }
        )

        // 2. Back Face
        CubeFace(
            value = faceValues.back,
            size = size,
            containerColor = sideShadowFaceColor,
            visible = backZ > -0.05f,
            transform = {
                transformOrigin = TransformOrigin.Center
                rotationY = 180f
            }
        )

        // 3. Right Face (Folded 90° on right edge)
        CubeFace(
            value = faceValues.right,
            size = size,
            containerColor = sideShadowFaceColor,
            visible = rightZ > -0.05f,
            transform = {
                transformOrigin = TransformOrigin(1f, 0.5f)
                rotationY = 90f
            }
        )

        // 4. Left Face (Folded -90° on left edge)
        CubeFace(
            value = faceValues.left,
            size = size,
            containerColor = sideShadowFaceColor,
            visible = leftZ > -0.05f,
            transform = {
                transformOrigin = TransformOrigin(0f, 0.5f)
                rotationY = -90f
            }
        )

        // 5. Top Face (Folded -90° on top edge)
        CubeFace(
            value = faceValues.top,
            size = size,
            containerColor = topLightFaceColor,
            visible = topZ > -0.05f,
            transform = {
                transformOrigin = TransformOrigin(0.5f, 0f)
                rotationX = -90f
            }
        )

        // 6. Bottom Face (Folded 90° on bottom edge)
        CubeFace(
            value = faceValues.bottom,
            size = size,
            containerColor = sideShadowFaceColor,
            visible = bottomZ > -0.05f,
            transform = {
                transformOrigin = TransformOrigin(0.5f, 1f)
                rotationX = 90f
            }
        )
    }
}

@Composable
private fun CubeFace(
    value: Int,
    size: Dp,
    containerColor: Color,
    visible: Boolean,
    transform: GraphicsLayerScope.() -> Unit
) {
    val defaultPipColor = MaterialTheme.colorScheme.onSurfaceVariant
    val jollyPipColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .size(size)
            .alpha(if (visible) 1f else 0f)
            .graphicsLayer {
                transform()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            DicePipsFace(
                value = value,
                defaultPipColor = defaultPipColor,
                jollyPipColor = jollyPipColor
            )
        }
    }
}

@Composable
private fun DicePipsFace(
    value: Int,
    defaultPipColor: Color,
    jollyPipColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 12f
            val width = size.width
            val height = size.height

            val left = width * 0.25f
            val centerX = width * 0.5f
            val right = width * 0.75f

            val top = height * 0.25f
            val centerY = height * 0.5f
            val bottom = height * 0.75f

            fun drawPip(x: Float, y: Float, isJolly: Boolean = false) {
                drawCircle(
                    color = if (isJolly) jollyPipColor else defaultPipColor,
                    radius = if (isJolly) radius * 1.25f else radius,
                    center = Offset(x, y)
                )
            }

            when (value) {
                1 -> {
                    // Jolly 1 is highlighted in primary color
                    drawPip(centerX, centerY, isJolly = true)
                }
                2 -> {
                    drawPip(left, top)
                    drawPip(right, bottom)
                }
                3 -> {
                    drawPip(left, top)
                    drawPip(centerX, centerY)
                    drawPip(right, bottom)
                }
                4 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                5 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(centerX, centerY)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                6 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, centerY)
                    drawPip(right, centerY)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                else -> {
                    drawPip(centerX, centerY)
                }
            }
        }
    }
}
