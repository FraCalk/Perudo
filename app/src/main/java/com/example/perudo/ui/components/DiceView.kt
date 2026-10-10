package com.example.perudo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.perudo.DieItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

/**
 * Mappa dei dati 3D per le facce di un dado (facce opposte sommano a 7).
 */
private data class CubeFaceValues(
    val front: Int,
    val back: Int,
    val top: Int,
    val bottom: Int,
    val right: Int,
    val left: Int,
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

// Strutture matematiche 3D
private data class Point3D(val x: Float, val y: Float, val z: Float)
private data class Point2D(val x: Float, val y: Float, val scale: Float)

private fun rotatePoint(p: Point3D, rotXDeg: Float, rotYDeg: Float, rotZDeg: Float): Point3D {
    val rx = Math.toRadians(rotXDeg.toDouble()).toFloat()
    val ry = Math.toRadians(rotYDeg.toDouble()).toFloat()
    val rz = Math.toRadians(rotZDeg.toDouble()).toFloat()

    // Rotazione attorno a X
    val y1 = (p.y * cos(rx)) - (p.z * sin(rx))
    val z1 = (p.y * sin(rx)) + (p.z * cos(rx))

    // Rotazione attorno a Y
    val x2 = (p.x * cos(ry)) + (z1 * sin(ry))
    val z2 = (-p.x * sin(ry)) + (z1 * cos(ry))

    // Rotazione attorno a Z
    val x3 = (x2 * cos(rz)) - (y1 * sin(rz))
    val y3 = (x2 * sin(rz)) + (y1 * cos(rz))

    return Point3D(x3, y3, z2)
}

/**
 * Proiezione ortogonale con lieve prospettiva naturale (focale bilanciata per zero distorsioni).
 */
private fun projectPoint(p: Point3D, center: Offset, focalLength: Float = 750f): Point2D {
    val cameraDistance = 750f
    val scale = focalLength / (cameraDistance + p.z)
    return Point2D(
        x = center.x + (p.x * scale),
        y = center.y + (p.y * scale),
        scale = scale,
    )
}

/**
 * Renderer pulito ed elegante per il dado con modello 3D coerente e senza cambi improvvisi di faccia.
 */
@Composable
fun DiceView(
    die: DieItem,
    isRolling: Boolean,
    staggerIndex: Int,
    onClickToRemove: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
) {
    var displayedValue by remember { mutableIntStateOf(die.value) }

    // Animazioni pulite e coerenti per la capovolta 3D
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val translationY = remember { Animatable(0f) }

    LaunchedEffect(die.value) {
        if (!isRolling) {
            displayedValue = die.value
        }
    }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            val delayMs = (staggerIndex * 70L).coerceAtMost(350L)
            delay(delayMs.milliseconds)

            // Fissa fin dall'inizio il valore effettivo del dado per garantire coerenza totale
            displayedValue = die.value

            // Animazione fisica pulita: salto verticale e capovolta 3D coerente
            launch {
                // Traiettoria d'arco verticale pulita con impatto morbido
                launch {
                    translationY.animateTo(
                        targetValue = -55f,
                        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                    )
                    translationY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    )
                }

                // Espansione di scala proporzionale
                launch {
                    scale.animateTo(
                        targetValue = 1.22f,
                        animationSpec = tween(durationMillis = 220),
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                }

                // Capovolta 3D coerente che termina esattamente sul valore finale die.value
                launch {
                    rotX.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                    )
                    rotX.snapTo(0f)
                }
                launch {
                    rotY.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
                    )
                    rotY.snapTo(0f)
                }
            }
        } else {
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            scale.snapTo(1f)
            translationY.snapTo(0f)
            displayedValue = die.value
        }
    }

    val defaultPipColor = MaterialTheme.colorScheme.onSurfaceVariant
    val jollyPipColor = MaterialTheme.colorScheme.primary
    val faceBaseColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    // Ombra dinamica pulita sul terreno
    val shadowAlpha = (0.50f - (-translationY.value / 120f)).coerceIn(0.15f, 0.50f)
    val shadowScale = (1f + (-translationY.value / 100f)).coerceIn(1f, 1.3f)

    Box(
        modifier = modifier
            .size(size)
            .clickable(enabled = !isRolling, onClick = onClickToRemove),
        contentAlignment = Alignment.Center,
    ) {
        // Ombra 3D proiettata sul piano d'appoggio
        Canvas(
            modifier = Modifier
                .size(size)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    scaleX = shadowScale
                    scaleY = shadowScale * 0.3f
                    this.translationY = (size.value * 0.45f).dp.toPx()
                    alpha = shadowAlpha
                },
        ) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.35f),
                radius = this.size.minDimension / 2.2f,
            )
        }

        // Canvas di rendering uniforme del dado
        Canvas(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize(),
        ) {
            val center = Offset(
                x = (size.toPx() / 2f),
                y = (size.toPx() / 2f) + translationY.value.dp.toPx(),
            )

            val cubeHalfSize = size.toPx() * 0.35f * scale.value

            if ((!isRolling) || ((rotX.value == 0f) && (rotY.value == 0f))) {
                // A RIPOSO: Faccia frontale perfettamente uniforme
                renderFlatFrontFace(
                    center = center,
                    cubeHalfSize = cubeHalfSize,
                    value = displayedValue,
                    faceBaseColor = faceBaseColor,
                    borderColor = borderColor,
                    defaultPipColor = defaultPipColor,
                    jollyPipColor = jollyPipColor,
                )
            } else {
                // IN MOVIMENTO: Cubo 3D in rotolamento fluido con spigoli smussati
                render3DCubeRolling(
                    center = center,
                    cubeHalfSize = cubeHalfSize,
                    displayedValue = displayedValue,
                    rotX = rotX.value,
                    rotY = rotY.value,
                    rotZ = 0f,
                    faceBaseColor = faceBaseColor,
                    borderColor = borderColor,
                    defaultPipColor = defaultPipColor,
                    jollyPipColor = jollyPipColor,
                )
            }
        }
    }
}

/**
 * Renderizza la faccia frontale con la stessa identica geometria e stile del cubo 3D.
 */
private fun DrawScope.renderFlatFrontFace(
    center: Offset,
    cubeHalfSize: Float,
    value: Int,
    faceBaseColor: Color,
    borderColor: Color,
    defaultPipColor: Color,
    jollyPipColor: Color,
) {
    val rectLeft = center.x - cubeHalfSize
    val rectTop = center.y - cubeHalfSize
    val rectSize = cubeHalfSize * 2f
    val cornerRadius = cubeHalfSize * 0.28f

    // Sfondo della faccia
    drawRoundRect(
        color = faceBaseColor,
        topLeft = Offset(rectLeft, rectTop),
        size = Size(rectSize, rectSize),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
    )

    // Bordo della faccia
    drawRoundRect(
        color = borderColor,
        topLeft = Offset(rectLeft, rectTop),
        size = Size(rectSize, rectSize),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = (cubeHalfSize * 0.10f).coerceAtLeast(3f)),
    )

    // Pallini della faccia
    drawFlatPipsForFace(
        center = center,
        cubeHalfSize = cubeHalfSize,
        value = value,
        defaultPipColor = defaultPipColor,
        jollyPipColor = jollyPipColor,
    )
}

/**
 * Disegna i pallini per la faccia a riposo con le stesse distanze esatte del modello 3D.
 */
private fun DrawScope.drawFlatPipsForFace(
    center: Offset,
    cubeHalfSize: Float,
    value: Int,
    defaultPipColor: Color,
    jollyPipColor: Color,
) {
    val d = cubeHalfSize * 0.48f
    val pipRadiusBase = cubeHalfSize * 0.16f

    fun drawPip(u: Float, v: Float, isJolly: Boolean = false) {
        val cx = center.x + (u * d)
        val cy = center.y + (v * d)
        val radius = if (isJolly) pipRadiusBase * 1.25f else pipRadiusBase
        val color = if (isJolly) jollyPipColor else defaultPipColor

        drawCircle(
            color = color,
            radius = radius,
            center = Offset(cx, cy),
        )
    }

    when (value) {
        1 -> drawPip(0f, 0f, isJolly = true)
        2 -> {
            drawPip(-1f, -1f)
            drawPip(1f, 1f)
        }
        3 -> {
            drawPip(-1f, -1f)
            drawPip(0f, 0f)
            drawPip(1f, 1f)
        }
        4 -> {
            drawPip(-1f, -1f)
            drawPip(1f, -1f)
            drawPip(-1f, 1f)
            drawPip(1f, 1f)
        }
        5 -> {
            drawPip(-1f, -1f)
            drawPip(1f, -1f)
            drawPip(0f, 0f)
            drawPip(-1f, 1f)
            drawPip(1f, 1f)
        }
        6 -> {
            drawPip(-1f, -1f)
            drawPip(1f, -1f)
            drawPip(-1f, 0f)
            drawPip(1f, 0f)
            drawPip(-1f, 1f)
            drawPip(1f, 1f)
        }
    }
}

/**
 * Struttura di rendering per ordinare e disegnare le facce del cubo 3D durante il rotolamento.
 */
private data class RenderFace(
    val value: Int,
    val avgZ: Float,
    val normalZ: Float,
    val points2D: List<Point2D>,
    val center3D: Point3D,
    val unitU3D: Point3D,
    val unitV3D: Point3D,
    val lightingFactor: Float,
)

/**
 * Crea un percorso 2D smussato con angoli arrotondati per le facce proiettate in 3D.
 */
private fun createRoundedPolygonPath(pts: List<Point2D>): Path {
    val path = Path()
    val n = pts.size
    if (n < 3) return path

    val cornerRadiusFactor = 0.22f
    val starts = Array(n) { Offset.Zero }
    val ends = Array(n) { Offset.Zero }

    for (i in 0 until n) {
        val pCurrent = Offset(pts[i].x, pts[i].y)
        val pNext = Offset(pts[(i + 1) % n].x, pts[(i + 1) % n].y)

        val dx = pNext.x - pCurrent.x
        val dy = pNext.y - pCurrent.y

        starts[i] = Offset(pCurrent.x + (dx * cornerRadiusFactor), pCurrent.y + (dy * cornerRadiusFactor))
        ends[i] = Offset(pNext.x - (dx * cornerRadiusFactor), pNext.y - (dy * cornerRadiusFactor))
    }

    path.moveTo(starts[0].x, starts[0].y)
    for (i in 0 until n) {
        val nextIdx = (i + 1) % n
        val controlPoint = Offset(pts[nextIdx].x, pts[nextIdx].y)
        path.lineTo(ends[i].x, ends[i].y)
        path.quadraticTo(controlPoint.x, controlPoint.y, starts[nextIdx].x, starts[nextIdx].y)
    }
    path.close()
    return path
}

private fun DrawScope.render3DCubeRolling(
    center: Offset,
    cubeHalfSize: Float,
    displayedValue: Int,
    rotX: Float,
    rotY: Float,
    rotZ: Float,
    faceBaseColor: Color,
    borderColor: Color,
    defaultPipColor: Color,
    jollyPipColor: Color,
) {
    val v0 = Point3D(-cubeHalfSize, -cubeHalfSize, -cubeHalfSize)
    val v1 = Point3D(+cubeHalfSize, -cubeHalfSize, -cubeHalfSize)
    val v2 = Point3D(+cubeHalfSize, +cubeHalfSize, -cubeHalfSize)
    val v3 = Point3D(-cubeHalfSize, +cubeHalfSize, -cubeHalfSize)
    val v4 = Point3D(-cubeHalfSize, -cubeHalfSize, +cubeHalfSize)
    val v5 = Point3D(+cubeHalfSize, -cubeHalfSize, +cubeHalfSize)
    val v6 = Point3D(+cubeHalfSize, +cubeHalfSize, +cubeHalfSize)
    val v7 = Point3D(-cubeHalfSize, +cubeHalfSize, +cubeHalfSize)

    val (frontVal, backVal, topVal, bottomVal, rightVal, leftVal) = getCubeFaceValues(displayedValue)

    val lightX = -0.3f
    val lightY = -0.6f
    val lightZ = 0.74f

    data class FaceDef(
        val value: Int,
        val verts: List<Point3D>,
        val norm: Point3D,
        val uVec: Point3D,
        val vVec: Point3D,
        val center3D: Point3D,
    )

    val faceDefs = listOf(
        // Front (Z = +S)
        FaceDef(frontVal, listOf(v4, v5, v6, v7), Point3D(0f, 0f, 1f), Point3D(1f, 0f, 0f), Point3D(0f, 1f, 0f), Point3D(0f, 0f, cubeHalfSize)),
        // Back (Z = -S)
        FaceDef(backVal, listOf(v1, v0, v3, v2), Point3D(0f, 0f, -1f), Point3D(-1f, 0f, 0f), Point3D(0f, 1f, 0f), Point3D(0f, 0f, -cubeHalfSize)),
        // Right (X = +S)
        FaceDef(rightVal, listOf(v5, v1, v2, v6), Point3D(1f, 0f, 0f), Point3D(0f, 0f, -1f), Point3D(0f, 1f, 0f), Point3D(cubeHalfSize, 0f, 0f)),
        // Left (X = -S)
        FaceDef(leftVal, listOf(v0, v4, v7, v3), Point3D(-1f, 0f, 0f), Point3D(0f, 0f, 1f), Point3D(0f, 1f, 0f), Point3D(-cubeHalfSize, 0f, 0f)),
        // Top (Y = -S)
        FaceDef(topVal, listOf(v0, v1, v5, v4), Point3D(0f, -1f, 0f), Point3D(1f, 0f, 0f), Point3D(0f, 0f, 1f), Point3D(0f, -cubeHalfSize, 0f)),
        // Bottom (Y = +S)
        FaceDef(bottomVal, listOf(v7, v6, v2, v3), Point3D(0f, 1f, 0f), Point3D(1f, 0f, 0f), Point3D(0f, 0f, -1f), Point3D(0f, cubeHalfSize, 0f)),
    )

    val renderFaces = mutableListOf<RenderFace>()

    for ((valNum, verts, norm, uVec, vVec, center3D) in faceDefs) {
        val rotVerts = verts.map { rotatePoint(it, rotX, rotY, rotZ) }
        val (nx, ny, nz) = rotatePoint(norm, rotX, rotY, rotZ)
        val rotCenter = rotatePoint(center3D, rotX, rotY, rotZ)
        val rotU = rotatePoint(uVec, rotX, rotY, rotZ)
        val rotV = rotatePoint(vVec, rotX, rotY, rotZ)

        val avgZ = rotVerts.asSequence().map { (_, _, zVal) -> zVal }.average().toFloat()

        val dot = (nx * lightX) + (ny * lightY) + (nz * lightZ)
        val lightFactor = (0.72f + (0.28f * dot)).coerceIn(0.58f, 1.0f)

        if (nz > 0.02f) {
            val points2D = rotVerts.map { projectPoint(it, center) }
            renderFaces.add(
                RenderFace(
                    value = valNum,
                    avgZ = avgZ,
                    normalZ = nz,
                    points2D = points2D,
                    center3D = rotCenter,
                    unitU3D = rotU,
                    unitV3D = rotV,
                    lightingFactor = lightFactor,
                ),
            )
        }
    }

    renderFaces.sortByDescending { (_, avgZ) -> avgZ }

    for (face in renderFaces) {
        val pts = face.points2D
        if (pts.size < 4) continue

        // Poligono della faccia 3D con angoli smussati
        val roundedPath = createRoundedPolygonPath(pts)

        val litColor = blendColorWithFactor(faceBaseColor, face.lightingFactor)

        // Disegna la faccia del cubo con angoli arrotondati
        drawPath(path = roundedPath, color = litColor)

        // Disegna il bordo smussato del cubo
        drawPath(
            path = roundedPath,
            color = borderColor,
            style = Stroke(
                width = (cubeHalfSize * 0.10f).coerceAtLeast(3f),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Disegna i pallini proiettati in prospettiva 3D sulla superficie della faccia
        draw3DPipsForFace(
            face = face,
            center = center,
            cubeHalfSize = cubeHalfSize,
            defaultPipColor = defaultPipColor,
            jollyPipColor = jollyPipColor,
        )
    }
}

/**
 * Disegna i pallini proiettati in 3D sulla superficie della faccia durante il rotolamento.
 */
private fun DrawScope.draw3DPipsForFace(
    face: RenderFace,
    center: Offset,
    cubeHalfSize: Float,
    defaultPipColor: Color,
    jollyPipColor: Color,
) {
    val d = cubeHalfSize * 0.48f
    val pipRadiusBase = cubeHalfSize * 0.16f

    fun getPipCenter3D(u: Float, v: Float): Point3D {
        return Point3D(
            x = face.center3D.x + (u * d * face.unitU3D.x) + (v * d * face.unitV3D.x),
            y = face.center3D.y + (u * d * face.unitU3D.y) + (v * d * face.unitV3D.y),
            z = face.center3D.z + (u * d * face.unitU3D.z) + (v * d * face.unitV3D.z),
        )
    }

    fun drawPip3D(u: Float, v: Float, isJolly: Boolean = false) {
        val pip3D = getPipCenter3D(u, v)
        val (px, py, pScale) = projectPoint(pip3D, center)
        val finalRadius = if (isJolly) pipRadiusBase * 1.25f * pScale else pipRadiusBase * pScale
        val color = if (isJolly) jollyPipColor else defaultPipColor

        drawCircle(
            color = color,
            radius = finalRadius,
            center = Offset(px, py),
        )
    }

    when (face.value) {
        1 -> {
            drawPip3D(0f, 0f, isJolly = true)
        }
        2 -> {
            drawPip3D(-1f, -1f)
            drawPip3D(1f, 1f)
        }
        3 -> {
            drawPip3D(-1f, -1f)
            drawPip3D(0f, 0f)
            drawPip3D(1f, 1f)
        }
        4 -> {
            drawPip3D(-1f, -1f)
            drawPip3D(1f, -1f)
            drawPip3D(-1f, 1f)
            drawPip3D(1f, 1f)
        }
        5 -> {
            drawPip3D(-1f, -1f)
            drawPip3D(1f, -1f)
            drawPip3D(0f, 0f)
            drawPip3D(-1f, 1f)
            drawPip3D(1f, 1f)
        }
        6 -> {
            drawPip3D(-1f, -1f)
            drawPip3D(1f, -1f)
            drawPip3D(-1f, 0f)
            drawPip3D(1f, 0f)
            drawPip3D(-1f, 1f)
            drawPip3D(1f, 1f)
        }
    }
}

private fun blendColorWithFactor(color: Color, factor: Float): Color {
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = color.alpha,
    )
}
