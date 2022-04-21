package com.helic.aminesms.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.helic.aminesms.R
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.MediumGray


@Composable
fun LoadingList() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colors.ButtonColor)
    }
}

@Composable
fun ErrorLoadingResults() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(120.dp),
            painter = painterResource(id = R.drawable.ic_sentiment),
            contentDescription = "", tint = MediumGray
        )
        Text(
            text = stringResource(R.string.error_loading_data),
            color = MediumGray,
            fontSize = MaterialTheme.typography.h6.fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NoResults() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(120.dp),
            painter = painterResource(id = R.drawable.ic_sentiment),
            contentDescription = "", tint = MediumGray
        )
        Text(
            text = stringResource(R.string.no_data),
            color = MediumGray,
            fontSize = MaterialTheme.typography.h6.fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}