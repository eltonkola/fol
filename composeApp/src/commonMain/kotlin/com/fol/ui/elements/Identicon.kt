package com.fol.ui.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun Identicon(
    publicKeyHash: String,
    userName: String,
    modifier: Modifier = Modifier
) {
    val identiconData = remember(publicKeyHash, userName) {
        generateConstellationData(publicKeyHash, userName)
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.aspectRatio(1f)) {
        drawConstellation(identiconData, textMeasurer)
    }
}

data class Star(val x: Float, val y: Float, val size: Float)
data class ConstellationData(
    val stars: List<Star>,
    val connections: List<Pair<Int, Int>>,
    val baseColor: Color,
    val accentColor: Color,
    val initial: String
)

fun generateConstellationData(publicKeyHash: String, userName: String): ConstellationData {
    val random = kotlin.random.Random(publicKeyHash.hashCode())

    val starCount = random.nextInt(7, 13)
    val stars = List(starCount) {
        Star(
            x = random.nextFloat(),
            y = random.nextFloat(),
            size = 0.01f + random.nextFloat() * 0.03f
        )
    }

    val connectionCount = random.nextInt(starCount - 1, starCount + 3)
    val connections = List(connectionCount) {
        Pair(random.nextInt(starCount), random.nextInt(starCount))
    }.distinct()

    val hue = random.nextFloat() * 360f
    val baseColor = Color.hsl(hue, 0.7f, 0.2f)
    val accentColor = Color.hsl((hue + 180) % 360, 0.7f, 0.7f)

    val initial = userName.firstOrNull()?.uppercase() ?: ""

    return ConstellationData(stars, connections, baseColor, accentColor, initial)
}

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawConstellation(data: ConstellationData, textMeasurer: TextMeasurer) {
    // Draw background
    drawRect(data.baseColor, size = size)

    // Draw star connections
    data.connections.forEach { (start, end) ->
        drawLine(
            color = data.accentColor.copy(alpha = 0.3f),
            start = Offset(data.stars[start].x * size.width, data.stars[start].y * size.height),
            end = Offset(data.stars[end].x * size.width, data.stars[end].y * size.height),
            strokeWidth = size.minDimension * 0.005f
        )
    }

    // Draw stars
    data.stars.forEach { star ->
        drawCircle(
            color = data.accentColor,
            radius = star.size * size.minDimension,
            center = Offset(star.x * size.width, star.y * size.height)
        )
        drawCircle(
            color = Color.White,
            radius = star.size * size.minDimension * 0.5f,
            center = Offset(star.x * size.width, star.y * size.height)
        )
    }

    // Draw initial
    val textStyle = TextStyle(
        fontSize = (size.minDimension * 0.2f).sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    val textLayoutResult = textMeasurer.measure(data.initial, textStyle)

    // Create a glow effect for the text
    for (i in 10 downTo 0) {
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                size.width / 2 - textLayoutResult.size.width / 2,
                size.height / 2 - textLayoutResult.size.height / 2
            ),
            color = data.accentColor.copy(alpha = i * 0.01f),

//            size = Size(
//                textLayoutResult.size.width + i * 2,
//                textLayoutResult.size.height + i * 2
//            )
        )
    }

    // Draw the actual text
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            size.width / 2 - textLayoutResult.size.width / 2,
            size.height / 2 - textLayoutResult.size.height / 2
        ),
        color = Color.White
    )
}