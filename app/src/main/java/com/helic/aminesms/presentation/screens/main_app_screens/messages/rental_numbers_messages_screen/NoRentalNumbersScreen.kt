package com.helic.aminesms.presentation.screens.main_app_screens.messages.rental_numbers_messages_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.helic.aminesms.R
import com.helic.aminesms.presentation.ui.theme.MediumGray
import com.helic.aminesms.presentation.ui.theme.backgroundColor

@Composable
fun NoNumbersFound() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.backgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(120.dp),
            painter = painterResource(id = R.drawable.ic_sentiment),
            contentDescription = "", tint = MediumGray
        )
        Text(
            text = stringResource(R.string.no_numbers_found),
            color = MediumGray,
            fontSize = MaterialTheme.typography.h6.fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}