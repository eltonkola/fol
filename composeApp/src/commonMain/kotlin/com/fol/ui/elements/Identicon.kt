package com.fol.ui.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Identicon(
    publicKeyHash: String,
    userName: String,
    modifier: Modifier = Modifier
) {
    val seed = publicKeyHash.hashCode()
    val random = kotlin.random.Random(seed)

    val baseHue = random.nextFloat() * 360f
    val baseColor = Color.hsl(baseHue, 0.7f, 0.5f)
    val accentColor = Color.hsl((baseHue + 180) % 360, 0.7f, 0.5f)

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Background circle
            drawCircle(baseColor, radius = radius)

            // Dynamic pattern
            val patternCount = random.nextInt(5, 12)
            val patternPath = Path()
            repeat(patternCount) {
                val angle = it * (360f / patternCount)
                val startRadius = radius * random.nextFloat() * 0.4f
                val endRadius = radius * (0.6f + random.nextFloat() * 0.4f)
                patternPath.moveTo(
                    center.x + startRadius * cos(angle.toRadians()),
                    center.y + startRadius * sin(angle.toRadians())
                )
                patternPath.lineTo(
                    center.x + endRadius * cos(angle.toRadians()),
                    center.y + endRadius * sin(angle.toRadians())
                )
            }
            drawPath(patternPath, accentColor, style = Stroke(width = radius * 0.05f))

            // Concentric circles
            val circleCount = random.nextInt(3, 6)
            repeat(circleCount) {
                drawCircle(
                    color = accentColor,
                    radius = radius * (0.2f + it * 0.2f),
                    style = Stroke(width = radius * 0.02f),
                    alpha = 0.3f
                )
            }

            // Dots
            val dotCount = random.nextInt(10, 20)
            repeat(dotCount) {
                val angle = random.nextFloat() * 360f
                val distance = random.nextFloat() * radius * 0.8f
                drawCircle(
                    color = accentColor,
                    radius = radius * 0.05f,
                    center = Offset(
                        center.x + distance * cos(angle.toRadians()),
                        center.y + distance * sin(angle.toRadians())
                    ),
                    alpha = 0.7f
                )
            }

            // Overlay gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x40000000)),
                    center = Offset(size.width * 0.7f, size.height * 0.3f),
                    radius = radius * 1.2f
                ),
                radius = radius
            )
        }

        // User's initial
        Text(
            text = userName.firstOrNull()?.uppercase() ?: "",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun Float.toRadians() = this * PI.toFloat() / 180f
