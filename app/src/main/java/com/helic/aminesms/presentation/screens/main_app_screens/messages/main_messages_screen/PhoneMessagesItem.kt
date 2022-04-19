package com.helic.aminesms.presentation.screens.main_app_screens.messages.main_messages_screen

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.helic.aminesms.data.models.order_number.OrderedNumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.Green
import com.helic.aminesms.presentation.ui.theme.MediumGray
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.presentation.ui.theme.phoneMessagesTextColor
import com.helic.aminesms.utils.AddOrRemoveNumberAction
import com.helic.aminesms.utils.NumberState
import com.helic.aminesms.utils.addOrRemoveNumberFromFirebase

@Composable
fun PhoneMessageItem(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    listOfPhoneNumbers: List<OrderedNumberData>,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    if (listOfPhoneNumbers.isEmpty()) {
        NoNumbersFound()
    } else {
        DisplayNumbers(
            context = context,
            navController = navController,
            mainViewModel = mainViewModel,
            listOfPhoneNumbersData = listOfPhoneNumbers,
            showSnackbar = showSnackbar
        )
    }
}

@Composable
fun DisplayNumbers(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    listOfPhoneNumbersData: List<OrderedNumberData>,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(listOfPhoneNumbersData) { phoneNumber ->
            Content(
                context = context,
                navController = navController,
                mainViewModel = mainViewModel,
                phoneNumberData = phoneNumber,
                showSnackbar = showSnackbar
            )
        }
    }
}


@Composable
fun Content(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    phoneNumberData: OrderedNumberData,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, end = 10.dp, start = 10.dp, bottom = 5.dp)
            .clickable {
                if (phoneNumberData.state != NumberState.Expired.toString()
                    && phoneNumberData.state != NumberState.Canceled.toString()
                ) {
                    mainViewModel.selectedNumber.value = phoneNumberData
                    mainViewModel.checkForMessages(
                        context = context,
                        temporaryNumberId = phoneNumberData.temporaryNumberId,
                        snackbar = showSnackbar
                    )
                    navController.navigate(MainAppScreens.MessageDetails.route) {
                        launchSingleTop = true
                    }
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
                    text = phoneNumberData.number,
                    color = MaterialTheme.colors.phoneMessagesTextColor,
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    text = phoneNumberData.state.toString(),
                    color = MediumGray,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    maxLines = 1
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (phoneNumberData.state) {
                    NumberState.Pending.toString() -> {
                        Canvas(modifier = Modifier.size(15.dp)) {
                            drawCircle(color = Green)
                        }
                    }
                    NumberState.Expired.toString(), NumberState.Canceled.toString() -> {
                        Canvas(modifier = Modifier.size(15.dp)) {
                            drawCircle(color = Red)
                        }
                    }
                    else -> {
                        Canvas(modifier = Modifier.size(15.dp)) {
                            drawCircle(color = MediumGray)
                        }
                    }
                }
                when (phoneNumberData.state) {
                    NumberState.Pending.toString() -> {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                    else -> {
                        IconButton(onClick = {
                            addOrRemoveNumberFromFirebase(
                                context = context,
                                snackbar = showSnackbar,
                                AddOrRemoveNumberAction.REMOVE,
                                phoneNumberData
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete number from database button"
                            )

                        }
                    }
                }

            }

        }

    }
}