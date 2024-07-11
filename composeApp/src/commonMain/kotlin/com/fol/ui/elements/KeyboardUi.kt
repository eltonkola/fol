package com.fol.com.fol.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyboardUi(
    query: String,
    onQueryChange: (String) -> Unit,
    onDone: () -> Unit
) {
    var isLetterKeyboard by remember { mutableStateOf(true) }
    var isCapsOn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
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
                    row.forEach { key ->
                        val displayKey = if (isLetterKeyboard && !isCapsOn) key.lowercase() else key
                        KeyButton(
                            text = displayKey,
                            onClick = { onQueryChange(query + displayKey) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (index == 2) {
                        KeyButton(
                            icon = Icons.Default.Delete,
                            onClick = { onQueryChange(query.dropLast(1)) },
                            modifier = Modifier.weight(1.5f)
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
                    onClick = { onQueryChange(query + " ") },
                    modifier = Modifier.weight(4f)
                )
                KeyButton(
                    text = "Done",
                    onClick = onDone,
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
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(horizontal = 2.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        if (text != null) {
            Text(
                text = text,
                fontSize = 16.sp,
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