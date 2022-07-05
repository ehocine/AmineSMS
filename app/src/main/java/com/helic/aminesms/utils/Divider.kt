package com.helic.aminesms.utils

import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.helic.aminesms.utils.Constants.DARK_THEME

@Composable
fun CustomDivider() {
    Divider(color = if (!DARK_THEME.value) Color(0xFFEEEEEE) else Color(0xFF333333))
}