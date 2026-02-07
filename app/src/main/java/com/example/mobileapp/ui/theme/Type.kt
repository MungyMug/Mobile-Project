package com.example.mobileapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mobileapp.R

// Your Bebas Neue font
val Bebas = FontFamily(
    Font(R.font.bebasneue_regular, FontWeight.Normal)
)

// Custom Typography for the whole app
val Typography = Typography(

    // BIG titles (ZOODEX)
    headlineLarge = TextStyle(
        fontFamily = Bebas,
        fontSize = 56.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 2.sp
    ),

    // Screen headers
    headlineMedium = TextStyle(
        fontFamily = Bebas,
        fontSize = 36.sp
    ),

    // Buttons (Capture / Pets / Exit)
    titleLarge = TextStyle(
        fontFamily = Bebas,
        fontSize = 22.sp
    ),

    // Normal text
    bodyLarge = TextStyle(
        fontFamily = Bebas,
        fontSize = 16.sp
    )
)
