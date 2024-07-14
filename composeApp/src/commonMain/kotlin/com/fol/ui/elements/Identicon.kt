package com.fol.ui.elements

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

/*
Compose ui identicon inspired by https://github.com/bilthon/BlockiesAndroid and written by chatgpt
*/

@Composable
fun Identicon(
    address: String,
    modifier: Modifier = Modifier,
    blockSize: Int = IdenticonData.DEFAULT_SIZE,
    cornerRadius: Float = 20f,
    hasShadow: Boolean = true
) {
    val identiconData = IdenticonData(address, blockSize)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val blockSizeInPixels = canvasWidth / kotlin.math.sqrt(identiconData.imageData.size.toDouble()).toFloat()

        // Clip Path with Corner Radius
        val clipPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, canvasWidth, canvasHeight),
                    topLeft = CornerRadius(cornerRadius, cornerRadius),
                    topRight = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeft = CornerRadius(cornerRadius, cornerRadius),
                    bottomRight = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }
        clipPath(clipPath, clipOp = ClipOp.Intersect) {
            // Draw Background Color
            drawRect(color = Color(identiconData.bgColor))
        }

        // Draw Blocks
        identiconData.imageData.forEachIndexed { index, value ->
            val x = (blockSizeInPixels * index) % canvasWidth
            val y = floor((blockSizeInPixels * index) / canvasWidth) * blockSizeInPixels
            if (value == 2) {
                drawRect(color = Color(identiconData.spotColor), topLeft = Offset(x, y), size = Size(blockSizeInPixels, blockSizeInPixels))
            } else if (value == 1) {
                drawRect(color = Color(identiconData.color), topLeft = Offset(x, y), size = Size(blockSizeInPixels, blockSizeInPixels))
            }
        }

        if (hasShadow) {
            // Draw Shadow
            val shadowColors = listOf(
                Color(0xC8323232), // Color.argb(200, 50, 50, 50)
                Color(0x64323232), // Color.argb(100, 0, 0, 0)
                Color.Transparent
            )
            drawRect(brush = Brush.linearGradient(shadowColors))

            // Draw Bright
            val brightColors = listOf(
                Color(0x64FFFFFF), // Color.argb(100, 255, 255, 255)
                Color.Transparent
            )
            drawRect(brush = Brush.linearGradient(brightColors))
        }
    }
}


class IdenticonData(seed: String, size: Int = DEFAULT_SIZE) {

    companion object {
        const val DEFAULT_SIZE = 8
    }

    private var randSeed: IntArray = IntArray(4)
    lateinit var imageData: IntArray
        private set
    var color: Int = 0
        private set
    var bgColor: Int = 0
        private set
    var spotColor: Int = 0
        private set

    init {
        val seedLower = seed.lowercase()
        seedRand(seedLower)
        createIcon(size)
    }

    private fun seedRand(seed: String) {
        for (i in seed.indices) {
            randSeed[i % 4] = ((randSeed[i % 4] shl 5) - randSeed[i % 4]) + seed[i].code
        }
    }

    private fun createIcon(size: Int) {
        color = createColor()
        bgColor = createColor()
        spotColor = createColor()
        imageData = createImageData(size)
    }

    private fun rand(): Double {
        val t = randSeed[0] xor (randSeed[0] shl 11)
        randSeed[0] = randSeed[1]
        randSeed[1] = randSeed[2]
        randSeed[2] = randSeed[3]
        randSeed[3] = randSeed[3] xor (randSeed[3] shr 19) xor t xor (t shr 8)
        val num = (randSeed[3] ushr 0).toDouble()
        val den = (1 shl 31).toDouble()
        return kotlin.math.abs(num / den)
    }

    private fun createColor(): Int {
        val h = floor(rand() * 360).toFloat()
        val s = ((rand() * 60) + 40) / 100
        val l = ((rand() + rand() + rand() + rand()) * 25) / 100
        return hslToColor(h, s.toFloat(), l.toFloat())
    }

    private fun hslToColor(h: Float, s: Float, l: Float): Int {
        val c = (1 - kotlin.math.abs(2 * l - 1)) * s
        val x = c * (1 - kotlin.math.abs((h / 60) % 2 - 1))
        val m = l - c / 2

        val (r, g, b) = when {
            h < 60 -> Triple(c, x, 0f)
            h < 120 -> Triple(x, c, 0f)
            h < 180 -> Triple(0f, c, x)
            h < 240 -> Triple(0f, x, c)
            h < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val red = ((r + m) * 255).roundToInt()
        val green = ((g + m) * 255).roundToInt()
        val blue = ((b + m) * 255).roundToInt()

        return (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
    }

    private fun createImageData(size: Int): IntArray {
        val width = size
        val height = size
        val dataWidth = ceil(width / 2.0).toInt()
        val mirrorWidth = width - dataWidth
        val data = ArrayList<Int>()
        for (y in 0 until height) {
            val row = ArrayList<Int>()
            for (x in 0 until dataWidth) {
                val r = rand() * 2.3
                val d = floor(r)
                val add = d.toInt()
                row.add(add)
            }
            val r = row.subList(0, mirrorWidth).reversed()
            row.addAll(r)
            data.addAll(row)
        }
        return data.toIntArray()
    }
}
