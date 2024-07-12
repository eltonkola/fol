package com.fol.com.fol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fol.com.fol.db.ThemeState

private val DarkColorScheme = darkColors(
    primary = Color(0xFFBB86FC),
    onPrimary =  Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6)
)

private val LightColorScheme = lightColors(
    primary = Color(0xFF6200EE),
    onPrimary = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6)
)

@Composable
fun AppTheme(
    themeState: ThemeState,
    content: @Composable () -> Unit
) {

    val isDark = if(themeState.system){
        isSystemInDarkTheme()
    }else{
        themeState.dark
    }

    val colors = if (isDark) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colors = colors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}
