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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.perudo.DieItem
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

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
 * Proiezione ortogonale con lieve prospettiva naturale.
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
 * Renderer per il dado che riceve fin dal primo frame del lancio il nuovo valore target,
 * ruotando e atterrando già sul numero definitivo estratto.
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

    // Animazioni per la rotazione 3D e traiettoria
    val rotX = remember { Animatable(0f) }
    val rotY = remember { Animatable(0f) }
    val rotZ = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val translationY = remember { Animatable(0f) }

    LaunchedEffect(die.value) {
        displayedValue = die.value
    }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            // Assegna immediatamente il nuovo valore definitivo prima del salto e rotazione
            displayedValue = die.value

            val spinDir = if ((staggerIndex % 2) == 0) 1f else -1f

            // Animazione fisica a trottola 3D veloce
            launch {
                // Arco di salto verticale
                launch {
                    translationY.animateTo(
                        targetValue = -50f,
                        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                    )
                    translationY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow,
                        ),
                    )
                }

                // Variazione di scala
                launch {
                    scale.animateTo(
                        targetValue = 1.20f,
                        animationSpec = tween(durationMillis = 140),
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    )
                }

                // Inclinazione d'asse trottola
                launch {
                    rotX.animateTo(
                        targetValue = 28f,
                        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
                    )
                    rotX.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    )
                    rotX.snapTo(0f)
                }

                // Rotazione circolare veloce a trottola (1080 gradi in 320ms)
                launch {
                    rotZ.animateTo(
                        targetValue = 1080f * spinDir,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    )
                    rotZ.snapTo(0f)
                }

                launch {
                    rotY.animateTo(
                        targetValue = 360f * spinDir,
                        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    )
                    rotY.snapTo(0f)
                }
            }
        } else {
            rotX.snapTo(0f)
            rotY.snapTo(0f)
            rotZ.snapTo(0f)
            scale.snapTo(1f)
            translationY.snapTo(0f)
            displayedValue = die.value
        }
    }

    val defaultPipColor = MaterialTheme.colorScheme.onSurfaceVariant
    val jollyPipColor = MaterialTheme.colorScheme.primary
    val faceBaseColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .size(size)
            .clickable(enabled = !isRolling, onClick = onClickToRemove),
        contentAlignment = Alignment.Center,
    ) {
        // Canvas unico per ombra sul terreno, Motion Blur 3D su tutte le facce e dado
        Canvas(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxSize(),
        ) {
            val centerPx = size.toPx() / 2f
            val cubeHalfSize = size.toPx() * 0.35f * scale.value

            // 1. Ombra posizionata sul terreno sotto la base del dado
            val floorY = centerPx + cubeHalfSize + 6.dp.toPx()
            val heightOffGround = -translationY.value.dp.toPx()

            val shadowScale = (1f + (heightOffGround / 120f)).coerceIn(1f, 1.35f)
            val shadowAlpha = (0.45f - (heightOffGround / 140f)).coerceIn(0.10f, 0.45f)

            val shadowWidth = cubeHalfSize * 2.2f * shadowScale
            val shadowHeight = cubeHalfSize * 0.45f * shadowScale

            drawOval(
                color = Color.Black.copy(alpha = shadowAlpha),
                topLeft = Offset(
                    x = centerPx - (shadowWidth / 2f),
                    y = floorY - (shadowHeight / 2f),
                ),
                size = Size(width = shadowWidth, height = shadowHeight),
            )

            // 2. Disegna il dado 3D che si solleva in altezza
            val dieCenter = Offset(
                x = centerPx,
                y = centerPx + translationY.value.dp.toPx(),
            )

            if ((!isRolling) || ((rotX.value == 0f) && (rotY.value == 0f) && (rotZ.value == 0f))) {
                // A RIPOSO O AD ATTERRAGGIO COMPLETATO: Nitidezza perfetta sulla singola faccia
                renderFlatFrontFace(
                    center = dieCenter,
                    cubeHalfSize = cubeHalfSize,
                    value = displayedValue,
                    faceBaseColor = faceBaseColor,
                    borderColor = borderColor,
                    defaultPipColor = defaultPipColor,
                    jollyPipColor = jollyPipColor,
                )
            } else {
                // IN MOVIMENTO: Cubo 3D con Motion Blur dinamico
                renderSolid3DCubeWithMotionBlur(
                    center = dieCenter,
                    cubeHalfSize = cubeHalfSize,
                    displayedValue = displayedValue,
                    rotX = rotX.value,
                    rotY = rotY.value,
                    rotZ = rotZ.value,
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
 * Renderizza la faccia frontale con il valore definitivo del dado.
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

    // Pallini nitidi della faccia
    drawFlatPipsForFace(
        center = center,
        cubeHalfSize = cubeHalfSize,
        value = value,
        defaultPipColor = defaultPipColor,
        jollyPipColor = jollyPipColor,
    )
}

/**
 * Disegna i pallini per la faccia a riposo.
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
 * Renderizza il cubo 3D unito applicando scie di Motion Blur durante la rotazione veloce.
 */
private fun DrawScope.renderSolid3DCubeWithMotionBlur(
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
    // Scie di Motion Blur 3D durante la rotazione veloce
    // Scia Motion Blur 2 (sfumata)
    renderSolid3DCubeMesh(
        center = center,
        cubeHalfSize = cubeHalfSize * 0.97f,
        displayedValue = displayedValue,
        rotX = rotX - 22f,
        rotY = rotY - 22f,
        rotZ = rotZ - 12f,
        faceBaseColor = faceBaseColor.copy(alpha = 0.18f),
        borderColor = borderColor.copy(alpha = 0.22f),
        defaultPipColor = defaultPipColor.copy(alpha = 0.18f),
        jollyPipColor = jollyPipColor.copy(alpha = 0.18f),
    )

    // Scia Motion Blur 1 (vicina)
    renderSolid3DCubeMesh(
        center = center,
        cubeHalfSize = cubeHalfSize * 0.985f,
        displayedValue = displayedValue,
        rotX = rotX - 10f,
        rotY = rotY - 10f,
        rotZ = rotZ - 5f,
        faceBaseColor = faceBaseColor.copy(alpha = 0.35f),
        borderColor = borderColor.copy(alpha = 0.40f),
        defaultPipColor = defaultPipColor.copy(alpha = 0.35f),
        jollyPipColor = jollyPipColor.copy(alpha = 0.35f),
    )

    // Cubo 3D principale
    renderSolid3DCubeMesh(
        center = center,
        cubeHalfSize = cubeHalfSize,
        displayedValue = displayedValue,
        rotX = rotX,
        rotY = rotY,
        rotZ = rotZ,
        faceBaseColor = faceBaseColor,
        borderColor = borderColor,
        defaultPipColor = defaultPipColor,
        jollyPipColor = jollyPipColor,
    )
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

/**
 * Renderizza un cubo 3D unito e solido condividendo l'array dei vertici proiettati.
 */
private fun DrawScope.renderSolid3DCubeMesh(
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
    // 8 vertici del cubo 3D
    val v3D = listOf(
        Point3D(-cubeHalfSize, -cubeHalfSize, -cubeHalfSize), // 0
        Point3D(+cubeHalfSize, -cubeHalfSize, -cubeHalfSize), // 1
        Point3D(+cubeHalfSize, +cubeHalfSize, -cubeHalfSize), // 2
        Point3D(-cubeHalfSize, +cubeHalfSize, -cubeHalfSize), // 3
        Point3D(-cubeHalfSize, -cubeHalfSize, +cubeHalfSize), // 4
        Point3D(+cubeHalfSize, -cubeHalfSize, +cubeHalfSize), // 5
        Point3D(+cubeHalfSize, +cubeHalfSize, +cubeHalfSize), // 6
        Point3D(-cubeHalfSize, +cubeHalfSize, +cubeHalfSize), // 7
    )

    // Proietta i vertici 3D condivisi
    val rotVerts = v3D.map { rotatePoint(it, rotX, rotY, rotZ) }
    val projVerts = rotVerts.map { projectPoint(it, center) }

    val (frontVal, backVal, topVal, bottomVal, rightVal, leftVal) = getCubeFaceValues(displayedValue)

    val lightX = -0.3f
    val lightY = -0.6f
    val lightZ = 0.74f

    data class SolidFaceDef(
        val value: Int,
        val vertIndices: List<Int>,
        val norm: Point3D,
        val uVec: Point3D,
        val vVec: Point3D,
        val center3D: Point3D,
    )

    val faceDefs = listOf(
        // Front
        SolidFaceDef(frontVal, listOf(4, 5, 6, 7), Point3D(0f, 0f, 1f), Point3D(1f, 0f, 0f), Point3D(0f, 1f, 0f), Point3D(0f, 0f, cubeHalfSize)),
        // Back
        SolidFaceDef(backVal, listOf(1, 0, 3, 2), Point3D(0f, 0f, -1f), Point3D(-1f, 0f, 0f), Point3D(0f, 1f, 0f), Point3D(0f, 0f, -cubeHalfSize)),
        // Right
        SolidFaceDef(rightVal, listOf(5, 1, 2, 6), Point3D(1f, 0f, 0f), Point3D(0f, 0f, -1f), Point3D(0f, 1f, 0f), Point3D(cubeHalfSize, 0f, 0f)),
        // Left
        SolidFaceDef(leftVal, listOf(0, 4, 7, 3), Point3D(-1f, 0f, 0f), Point3D(0f, 0f, 1f), Point3D(0f, 1f, 0f), Point3D(-cubeHalfSize, 0f, 0f)),
        // Top
        SolidFaceDef(topVal, listOf(0, 1, 5, 4), Point3D(0f, -1f, 0f), Point3D(1f, 0f, 0f), Point3D(0f, 0f, 1f), Point3D(0f, -cubeHalfSize, 0f)),
        // Bottom
        SolidFaceDef(bottomVal, listOf(7, 6, 2, 3), Point3D(0f, 1f, 0f), Point3D(1f, 0f, 0f), Point3D(0f, 0f, -1f), Point3D(0f, cubeHalfSize, 0f)),
    )

    val renderFaces = mutableListOf<RenderFace>()

    for ((valNum, vertIndices, norm, uVec, vVec, center3D) in faceDefs) {
        val rotNorm = rotatePoint(norm, rotX, rotY, rotZ)
        val (nx, ny, nz) = rotNorm
        val rotCenter = rotatePoint(center3D, rotX, rotY, rotZ)
        val rotU = rotatePoint(uVec, rotX, rotY, rotZ)
        val rotV = rotatePoint(vVec, rotX, rotY, rotZ)

        val pts2D = vertIndices.map { projVerts[it] }
        val avgZ = vertIndices.asSequence().map { rotVerts[it].z }.average().toFloat()

        val dot = (nx * lightX) + (ny * lightY) + (nz * lightZ)
        val lightFactor = (0.72f + (0.28f * dot)).coerceIn(0.58f, 1.0f)

        if (nz > 0.02f) {
            renderFaces.add(
                RenderFace(
                    value = valNum,
                    avgZ = avgZ,
                    normalZ = nz,
                    points2D = pts2D,
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

        // Poligono della faccia 3D unito
        val roundedPath = createRoundedPolygonPath(pts)

        val litColor = blendColorWithFactor(faceBaseColor, face.lightingFactor)

        // Disegna il riempimento solido senza fessure
        drawPath(path = roundedPath, color = litColor)

        // Disegna la struttura di bordo 3D continua ed unita
        drawPath(
            path = roundedPath,
            color = borderColor,
            style = Stroke(
                width = (cubeHalfSize * 0.10f).coerceAtLeast(3f),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Disegna i pallini proiettati in prospettiva 3D su ciascuna faccia
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
 * Disegna i pallini proiettati in 3D sulla superficie di ciascuna faccia del cubo.
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
