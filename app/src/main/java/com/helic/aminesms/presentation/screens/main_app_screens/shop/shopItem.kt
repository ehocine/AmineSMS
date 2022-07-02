package com.helic.aminesms.presentation.screens.main_app_screens.shop

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.ui.theme.ShopItemColor
import com.helic.aminesms.utils.dollarToCreditForPurchasingCurrency
import kotlinx.coroutines.launch

@Composable
fun ShopItem(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    selected: Int,
    title: Int,
    titleColor: Color =
        if (selected == title) MaterialTheme.colors.ShopItemColor
        else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
    priceColor: Color =
        if (selected == title) Color.Red
        else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
    titleSize: TextUnit = MaterialTheme.typography.h6.fontSize,
    titleWeight: FontWeight = FontWeight.Normal,
    borderWidth: Dp = 1.dp,
    borderColor: Color =
        if (selected == title) MaterialTheme.colors.ShopItemColor
        else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
    borderShape: Shape = RoundedCornerShape(10.dp),
    icon: ImageVector = Icons.Default.CheckCircle,
    iconColor: Color =
        if (selected == title) MaterialTheme.colors.ShopItemColor
        else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
    onClick: () -> Unit
) {
    val scaleA = remember { Animatable(initialValue = 1f) }
    val scaleB = remember { Animatable(initialValue = 1f) }
    LaunchedEffect(key1 = selected) {
        if (selected == title) {
            launch {
                scaleA.animateTo(
                    targetValue = 0.3f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
                scaleA.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                scaleB.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(
                        durationMillis = 50
                    )
                )
                scaleB.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }
    Card(
        modifier = modifier
            .padding(top = 5.dp, end = 10.dp, start = 10.dp, bottom = 5.dp)
            .scale(scale = scaleB.value)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = borderShape
            )
            .fillMaxWidth()
            .clip(borderShape)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Text(
                        text = "Buy ",
                        style = TextStyle(
                            color = titleColor,
                            fontSize = titleSize,
                            fontWeight = titleWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${dollarToCreditForPurchasingCurrency(title.toDouble(), mainViewModel = mainViewModel)} ",
                        style = TextStyle(
                            color = titleColor,
                            fontSize = titleSize,
                            fontWeight = titleWeight,
                        ),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "credits for ",
                        style = TextStyle(
                            color = titleColor,
                            fontSize = titleSize,
                            fontWeight = titleWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$$title",
                        style = TextStyle(
                            color = priceColor,
                            fontSize = titleSize,
                            fontWeight = titleWeight
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    modifier = Modifier.scale(scale = scaleA.value),
                    onClick = onClick
                ) {
                    Icon(imageVector = icon, contentDescription = "Shop Item", tint = iconColor)
                }
                Spacer(modifier = Modifier.padding(20.dp))
            }

        }
    }
}