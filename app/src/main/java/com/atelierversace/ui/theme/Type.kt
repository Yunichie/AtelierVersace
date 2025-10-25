package com.atelierversace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.atelierversace.R

// Note: Add these font files to res/font/ directory:
// - playfair_display_regular.ttf
// - playfair_display_bold.ttf
// - raleway_regular.ttf
// - raleway_medium.ttf
// - raleway_bold.ttf

val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_bold, FontWeight.Bold)
)

val Raleway = FontFamily(
    Font(R.font.raleway_regular, FontWeight.Normal),
    Font(R.font.raleway_medium, FontWeight.Medium),
    Font(R.font.raleway_bold, FontWeight.Bold)
)

fun Typography(): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 57.sp
        ),
        displayMedium = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 45.sp
        ),
        displaySmall = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        ),
        titleLarge = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        ),
        titleMedium = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        ),
        titleSmall = TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = Raleway,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = Raleway,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        ),
        bodySmall = TextStyle(
            fontFamily = Raleway,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp
        ),
        labelLarge = TextStyle(
            fontFamily = Raleway,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        labelMedium = TextStyle(
            fontFamily = Raleway,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        ),
        labelSmall = TextStyle(
            fontFamily = Raleway,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
    )
}
