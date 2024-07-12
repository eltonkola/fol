package com.fol.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import fol.composeapp.generated.resources.Res
import fol.composeapp.generated.resources.teko_bold
import fol.composeapp.generated.resources.teko_light
import fol.composeapp.generated.resources.teko_medium
import fol.composeapp.generated.resources.teko_regular
import fol.composeapp.generated.resources.teko_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun TekoFontFamily() = FontFamily(
    Font(Res.font.teko_bold, weight = FontWeight.Bold),
    Font(Res.font.teko_light, weight = FontWeight.Light),
    Font(Res.font.teko_medium, weight = FontWeight.Medium),
    Font(Res.font.teko_regular, weight = FontWeight.Normal),
    Font(Res.font.teko_semibold, weight = FontWeight.SemiBold),
)

//val bodyFontFamily = FontFamily(
//    Font(
//        Res.font. = GoogleFont("Roboto Flex"),
//        fontProvider = provider,
//    )
//)


// Default Material 3 typography values
val baseline = Typography()

@Composable
fun AppTypography() = Typography().run {
    val displayFontFamily = TekoFontFamily()
    val bodyFontFamily = displayFontFamily

    copy(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    )
}

