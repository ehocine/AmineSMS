package com.helic.aminesms.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun CustomDivider() {
    Divider(color = if (!isSystemInDarkTheme()) Color(0xFFEEEEEE) else Color(0xFF333333))
}