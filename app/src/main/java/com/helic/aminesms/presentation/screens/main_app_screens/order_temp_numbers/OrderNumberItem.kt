package com.helic.aminesms.presentation.screens.main_app_screens.order_temp_numbers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.helic.aminesms.data.models.service_state.ServiceState
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.Green
import com.helic.aminesms.presentation.ui.theme.MediumGray
import com.helic.aminesms.presentation.ui.theme.phoneMessagesTextColor
import com.helic.aminesms.utils.dollarToCreditForPurchasingNumbers

@Composable
fun DisplayServiceStateList(
    navController: NavController,
    listOfServiceState: List<ServiceState>,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    val searchText = mainViewModel.searchTextState.value
    val filteredList: List<ServiceState> = if (searchText.isEmpty()) {
        listOfServiceState
    } else {
        listOfServiceState.filter { serviceState ->
            serviceState.name.contains(searchText)
        }
    }
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(filteredList) { serviceState ->
            Content(
                navController = navController,
                serviceState = serviceState,
                mainViewModel = mainViewModel,
                snackbar = snackbar
            )
        }
    }
}


@Composable
fun Content(
    navController: NavController,
    serviceState: ServiceState,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, end = 10.dp, start = 10.dp, bottom = 5.dp)
            .clickable {
                if (serviceState.isAvailable) {
                    // Transfer the selected state to the view model
                    mainViewModel.selectedServiceState.value = serviceState
                    navController.navigate(MainAppScreens.OrderNumberOptions.route)
                } else {
                    snackbar("Service is not available", SnackbarDuration.Short)
                }
            },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = serviceState.name,
                    color = MaterialTheme.colors.phoneMessagesTextColor,
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    text = "${dollarToCreditForPurchasingNumbers(serviceState.price)} credits",
                    color = MediumGray,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    maxLines = 1
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Availability option",
                    modifier = Modifier.size(20.dp),
                    tint = if (serviceState.isAvailable) Green else Color.LightGray
                )
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "")
            }
        }

    }
}



