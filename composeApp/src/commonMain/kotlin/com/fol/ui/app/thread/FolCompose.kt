package com.fol.ui.app.thread



import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class TextFieldState(
    val text: String,
    val cursorPosition: Int
)

@Composable
fun CustomTextFieldWithKeyboard(
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = ""
) {
    var textFieldState by remember { mutableStateOf(TextFieldState("", 0)) }

    Column(modifier = modifier.fillMaxSize()) {
        CustomTextField(
            state = textFieldState,
            onPositionChange = { newPosition ->
                textFieldState = textFieldState.copy(cursorPosition = newPosition)
            },
            label = label,
            placeholder = placeholder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        KeyboardUi(
            onKeyPress = { key ->
                val newText = textFieldState.text.substring(0, textFieldState.cursorPosition) +
                        key +
                        textFieldState.text.substring(textFieldState.cursorPosition)
                textFieldState = TextFieldState(newText, textFieldState.cursorPosition + 1)
            },
            onBackspace = {
                if (textFieldState.cursorPosition > 0) {
                    val newText = textFieldState.text.substring(0, textFieldState.cursorPosition - 1) +
                            textFieldState.text.substring(textFieldState.cursorPosition)
                    textFieldState = TextFieldState(newText, textFieldState.cursorPosition - 1)
                }
            },
            onSpace = {
                val newText = textFieldState.text.substring(0, textFieldState.cursorPosition) +
                        " " +
                        textFieldState.text.substring(textFieldState.cursorPosition)
                textFieldState = TextFieldState(newText, textFieldState.cursorPosition + 1)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun CustomTextField(
    state: TextFieldState,
    onPositionChange: (Int) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val textStyle = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.Monospace)
    val density = LocalDensity.current
    var textFieldWidth by remember { mutableStateOf(0f) }
    var textFieldHeight by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition()
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .padding(16.dp)
            .onGloballyPositioned { coordinates ->
                textFieldWidth = coordinates.size.width.toFloat()
                textFieldHeight = coordinates.size.height.toFloat()
            }
    ) {
        if (state.text.isEmpty()) {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = textStyle
            )
        }

        Text(
            text = state.text,
            color = MaterialTheme.colorScheme.onSurface,
            style = textStyle,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    if (state.cursorPosition in 0..state.text.length) {
                        val charWidth = textStyle.fontSize.toPx()
                        val cursorX = charWidth * state.cursorPosition
                        drawLine(
                            Color.Yellow.copy(alpha = cursorAlpha),
                            start = Offset(cursorX, 0f),
                            end = Offset(cursorX, size.height),
                            strokeWidth = 2f
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val charWidth = textStyle.fontSize.toPx()
                        val position = (offset.x / charWidth).toInt().coerceIn(0, state.text.length)
                        onPositionChange(position)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val charWidth = textStyle.fontSize.toPx()
                        val position = (change.position.x / charWidth).toInt().coerceIn(0, state.text.length)
                        onPositionChange(position)
                    }
                }
        )

        Text(
            text = label,
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(y = (-24).dp)
        )
    }
}


@Composable
fun KeyboardUi(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLetterKeyboard by remember { mutableStateOf(true) }
    var isCapsOn by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 0.dp)
        ) {
            val keys = if (isLetterKeyboard) {
                listOf(
                    listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
                    listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                    listOf("Z", "X", "C", "V", "B", "N", "M")
                )
            } else {
                listOf(
                    listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                    listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
                    listOf("*", "\"", "'", ":", ";", "!", "?")
                )
            }

            keys.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (index == 2 && isLetterKeyboard) {
                        KeyButton(
                            icon = Icons.Default.KeyboardArrowUp,
                            onClick = { isCapsOn = !isCapsOn },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                    row.forEachIndexed { indexRow, key ->
                        if (index == 1 && isLetterKeyboard && indexRow == 0) {
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                        val displayKey = if (isLetterKeyboard && !isCapsOn) key.lowercase() else key
                        KeyButton(
                            text = displayKey,
                            onClick = { onKeyPress(displayKey) },
                            modifier = Modifier.weight(1f)
                        )
                        if (index == 1 && isLetterKeyboard && indexRow == row.size - 1) {
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                    if (index == 2) {
                        KeyButton(
                            icon = Icons.Default.KeyboardBackspace,
                            onClick = onBackspace,
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KeyButton(
                    text = if (isLetterKeyboard) "123" else "ABC",
                    onClick = { isLetterKeyboard = !isLetterKeyboard },
                    modifier = Modifier.weight(1.5f)
                )
                KeyButton(
                    text = "Space",
                    onClick = onSpace,
                    modifier = Modifier.weight(4f)
                )
                KeyButton(
                    text = "Done",
                    onClick = { /* Handle done action */ },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}

@Composable
fun KeyButton(
    text: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(
            start = 2.dp,
            top = 2.dp,
            end = 2.dp,
            bottom = 2.dp
        ),
    ) {
        if (text != null) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}


