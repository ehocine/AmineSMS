package com.helic.aminesms.presentation.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.helic.aminesms.utils.Constants.DARK_THEME

//val Purple200 = Color(0xFFBB86FC)
//val Purple500 = Color(0xFF6200EE)
//val Purple700 = Color(0xFF3700B3)
//val Teal200 = Color(0xFF03DAC5)

//val Purple500 = Color(0xFFf44648)

val Purple200 = Color(0xFFBB86FC)
val Purple500 = Color(0xFF6200EE)
val Purple700 = Color(0xFF6200EE)
val Teal200 = Color(0xFF03DAC5)

val primaryColor = Color(0xFF7048B6)


private val lightPrimaryColor = Color(0xFF000000)
private val darkPrimaryColor = Color(0xFFFFFFFF)

private val lightErrorColor = Color(0xFFEB5757)
private val lightWarningColor = Color(0xFFFFA93B)
private val lightSuccessColor = Color(0xFF6FCF97)
private val lightInformationColor = Color(0xFF006AF6)


val lightBackgroundColor = Color(0xFFF6F9FF)
val darkBackgroundColor = Color(0xFF0C1B3A)

val Green = Color(0xFF00C980)
val Yellow = Color(0xFFFFC114)
val DarkGray = Color(0xFF141414)
val MediumGray = Color(0xFF9C9C9C)
val LightGray = Color(0xFFFCFCFC)
val Red = Color(0xFFFF4646)

val card = Color(0xFFFFFFFF)
val cardNight = Color(0xFF162544)

val Colors.HomeCard: Color
    @Composable
    get() = if (!DARK_THEME.value) lightInformationColor else lightInformationColor

val Colors.HomeCardTextColor: Color
    @Composable
    get() = if (!DARK_THEME.value) LightGray else DarkGray

val Colors.TextColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Color.Black else Color.White

val Colors.topAppBarBackgroundColor: Color
    @Composable
    get() = if (!DARK_THEME.value) lightBackgroundColor else darkBackgroundColor

val Colors.topAppBarContentColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Color.Black else Color.White

val Colors.ButtonColor: Color
    @Composable
    get() = if (!DARK_THEME.value) lightPrimaryColor else darkPrimaryColor

val Colors.ButtonTextColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Color.White else Color.Black

val Colors.ShopItemColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Purple500 else MaterialTheme.colors.primary

val Colors.backgroundColor: Color
    @Composable
    get() = if (!DARK_THEME.value) lightBackgroundColor else darkBackgroundColor

val Colors.DialogNoText: Color
    @Composable
    get() = if (!DARK_THEME.value) Color.Black else Color.White

val Colors.ProgressIndicatorColor: Color
    @Composable
    get() = if (!DARK_THEME.value) DarkGray else LightGray

val Colors.CardColor: Color
    @Composable
    get() = if (!DARK_THEME.value) card else cardNight

val Colors.PasswordEyeColor: Color
    @Composable
    get() = if (!DARK_THEME.value) Color.Black else Color.White

