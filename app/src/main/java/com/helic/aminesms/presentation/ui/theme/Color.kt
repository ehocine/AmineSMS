package com.helic.aminesms.presentation.ui.theme

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.helic.aminesms.utils.Constants.DARK_THEME

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF3700B3)
val Teal200 = Color(0xFF03DAC5)

val primaryColor = Color(0xFF7048B6)
val whiteBackground = Color(0xFFF7F7F7)

val Green = Color(0xFF00C980)
val Yellow = Color(0xFFFFC114)
val DarkGray = Color(0xFF141414)
val MediumGray = Color(0xFF9C9C9C)
val LightGray = Color(0xFFFCFCFC)
val Red = Color(0xFFFF4646)

val Colors.TextColor: Color
    @Composable
    get() = if (!DARK_THEME.value) DarkGray else LightGray

val Colors.phoneMessagesBackground: Color
    @Composable
    get() = if (DARK_THEME.value) Color.White else DarkGray

val Colors.topAppBarBackgroundColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Purple500 else Color.Black


val Colors.topAppBarContentColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Color.White else LightGray


val Colors.ButtonColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Purple500 else Purple700

//
//val Colors.TextColor : Color
//    @Composable
//    get() = if (!isSystemInDarkTheme()) Color.Black else Color.White

val Colors.backgroundColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Color.White else Color.Black

