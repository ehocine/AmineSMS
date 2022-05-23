package com.helic.aminesms.presentation.screens.main_app_screens.messages.rental_numbers_messages_screen

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
import com.helic.aminesms.data.models.number_data.RentalNumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.Green
import com.helic.aminesms.presentation.ui.theme.MediumGray
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.presentation.ui.theme.TextColor
import com.helic.aminesms.utils.AddOrRemoveNumberAction
import com.helic.aminesms.utils.NumberState
import com.helic.aminesms.utils.addOrRemoveRentalNumberFromFirebase
import com.helic.aminesms.utils.calculatingRemainingDaysForRentals

@Composable
fun RentalNumberMessageItem(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    listOfRentalNumbers: List<RentalNumberData>,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    if (listOfRentalNumbers.isEmpty()) {
        NoNumbersFound()
    } else {
        DisplayNumbers(
            context = context,
            navController = navController,
            mainViewModel = mainViewModel,
            listOfPhoneNumbersData = listOfRentalNumbers,
            showSnackbar = showSnackbar
        )
    }
}

@Composable
fun DisplayNumbers(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    listOfPhoneNumbersData: List<RentalNumberData>,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(listOfPhoneNumbersData) { phoneNumber ->
            Content(
                context = context,
                navController = navController,
                mainViewModel = mainViewModel,
                rentalNumberData = phoneNumber,
                snackbar = showSnackbar
            )
        }
    }
}

@Composable
fun Content(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    rentalNumberData: RentalNumberData,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, end = 10.dp, start = 10.dp, bottom = 5.dp)
            .clickable {
                //Even if the number has expired, the user can enter it to renew. when the system deletes it, it will automatically
                // not shown since the system's response (number is not present) must be compared to firebase
                mainViewModel.selectedRentalNumber.value = rentalNumberData
                mainViewModel.getRentalNumberMessages(
                    rentalId = rentalNumberData.rentalId,
                    snackbar = snackbar
                )
                navController.navigate(MainAppScreens.RentalMessageDetails.route) {
                    launchSingleTop = true
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
                rentalNumberData.number?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.TextColor,
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
//                Spacer(modifier = Modifier.padding(5.dp))
                Text(
                    text = rentalNumberData.rentalServiceName,
                    color = MaterialTheme.colors.TextColor,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    maxLines = 1
                )
//                Spacer(modifier = Modifier.padding(5.dp))
                rentalNumberData.expiresAt?.let {
                    Text(
                        text = "Number expires in: ${calculatingRemainingDaysForRentals(it.toLong())} days",
                        color = if (calculatingRemainingDaysForRentals(it.toLong()) < 1) Red else MaterialTheme.colors.TextColor,
                        fontSize = MaterialTheme.typography.subtitle1.fontSize,
                        maxLines = 1
                    )
                }
//                Spacer(modifier = Modifier.padding(5.dp))
                //TODO: Check the number state changes (live or expired) from the list, this info must be got from the server.
                //It seems we are getting the list directly from the server, we filter it using the rental Id from firebase and we display it. So no issue at first glance.
                Text(
                    text = "Number state: ${rentalNumberData.state}",
                    color = MediumGray,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    maxLines = 1
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {

                when {
                    rentalNumberData.state == NumberState.live.toString()
                            && rentalNumberData.expiresAt?.let {
                        calculatingRemainingDaysForRentals(
                            it.toLong()
                        )
                    }!! > 0 -> {
                        Canvas(modifier = Modifier.size(15.dp)) {
                            drawCircle(color = Green)
                        }
                    }
                    else -> {
                        Canvas(modifier = Modifier.size(15.dp)) {
                            drawCircle(color = Red)
                        }
                    }
                }
                when {
                    rentalNumberData.state == NumberState.live.toString() && rentalNumberData.expiresAt?.let {
                        calculatingRemainingDaysForRentals(
                            it.toLong()
                        )
                    }!! > 0 -> {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = ""
                        )
                    }
                    //TODO: Removing expired numbers is not working
                    else -> IconButton(onClick = {

                        //Since in the firebase the object (change in sms field) is not the same as the one in the server's response,
                        // we check the numbers with same IDs from the server and the firebase (obviously the same number)

                        val selectedRentalNumber =
                            mainViewModel.orderedRentalNumbers.value.find { it.rentalId == rentalNumberData.rentalId }

                        addOrRemoveRentalNumberFromFirebase(
                            context = context,
                            snackbar = snackbar,
                            action = AddOrRemoveNumberAction.REMOVE,
                            rentalNumberData = selectedRentalNumber
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