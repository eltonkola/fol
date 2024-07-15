package com.fol.com.fol.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyboardUi(
    query: String,
    onQueryChange: (String) -> Unit,
    onDone: () -> Unit,
    actionName: String = "Done",
    validInput: Boolean = true,
    modifier : Modifier = Modifier
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
                    row.forEachIndexed { indexRow,  key ->
                        if(index == 1 && isLetterKeyboard && indexRow == 0){
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                        val displayKey = if (isLetterKeyboard && !isCapsOn) key.lowercase() else key
                        KeyButton(
                            text = displayKey,
                            onClick = { onQueryChange(query + displayKey) },
                            modifier = Modifier.weight(1f)
                        )
                        if(index == 1  && isLetterKeyboard && indexRow == row.size - 1){
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                    }
                    if (index == 2) {
                        KeyButton(
                            icon = Icons.Default.KeyboardBackspace,
                            onClick = { onQueryChange(query.dropLast(1)) },
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
                    text = "\\__________/",
                    onClick = { onQueryChange(query + " ") },
                    modifier = Modifier.weight(4f)
                )
                KeyButton(
                    text = actionName,
                    onClick = onDone,
                    modifier = Modifier.weight(1.5f),
                    enabled = validInput
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
        contentPadding =  PaddingValues(
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