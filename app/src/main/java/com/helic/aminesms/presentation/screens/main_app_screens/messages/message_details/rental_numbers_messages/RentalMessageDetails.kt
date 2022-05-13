package com.helic.aminesms.presentation.screens.main_app_screens.messages.message_details.rental_numbers_messages

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.helic.aminesms.R
import com.helic.aminesms.data.models.number_data.RentalNumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.backgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.*
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RentalMessageDetails(
    navController: NavController,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current

    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val rentalNumbersMessagesList = mainViewModel.rentalNumbersMessagesList.value
    val rentalNumber = mainViewModel.selectedRentalNumber.value
    val state by mainViewModel.checkingRentalMessagesLoadingState.collectAsState()

    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = showSnackbar)
    }
    val userBalance = mainViewModel.userBalance.collectAsState().value
    val activationState by mainViewModel.activatingRentalNumberLoadingState.collectAsState()


    //This is added to auto-check the incoming messages every TIME_BETWEEN_AUTO_REFRESH,
    // we added the variable counter to autoupdate the LaunchedEffect
    // we added the condition to reduce resources usage when there is a message

    if (hasInternetConnection(context = context)) {
        var counter by remember { mutableStateOf(0) }
        LaunchedEffect(key1 = counter) {
            delay(timeMillis = Constants.TIME_BETWEEN_AUTO_REFRESH)
            counter += 1
            mainViewModel.autoCheckRentalMessage(
                rentalId = rentalNumber.rentalId,
                snackbar = showSnackbar
            )
        }
    }

    Scaffold(topBar = {
        RentalMessageDetailsTopAppBar(
            context = context,
            navController = navController,
            mainViewModel = mainViewModel,
            rentalNumber = rentalNumber,
            showSnackbar = showSnackbar
        )
    }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
//                mainViewModel.refreshMessageCheck(
//                    context = context,
//                    temporaryNumberId = temporaryNumber.temporaryNumberId,
//                    snackbar = showSnackbar
//                )
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.backgroundColor
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxSize(),
                ) {
                    when (state) {
                        LoadingState.LOADING -> Column(
                            modifier = Modifier.align(Alignment.TopCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) { LoadingList() }
                        LoadingState.ERROR -> Column(
                            modifier = Modifier.align(Alignment.TopCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) { ErrorLoadingResults() }
                        else -> {
                            Column(
                                modifier = Modifier.align(Alignment.TopCenter),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (rentalNumbersMessagesList.isNotEmpty()) {
                                    MessageDetailItem(listOfMessages = rentalNumbersMessagesList)
                                } else {
                                    NoResults()
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.BottomCenter),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                ActivateButton(state = activationState) {
                                    mainViewModel.activateRentalNumber(
                                        rentalId = rentalNumber.rentalId,
                                        snackbar = showSnackbar
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RentalMessageDetailsTopAppBar(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    rentalNumber: RentalNumberData,
    showSnackbar: (String, SnackbarDuration) -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }

            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Arrow",
                    tint = MaterialTheme.colors.topAppBarContentColor
                )
            }
        },
        title = { mainViewModel.selectedRentalNumber.value.number?.let { Text(text = it) } },
        actions = {
            ExistingTaskAppBarActions(
                navController = navController,
                rentalNumber = rentalNumber,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun ActivateButton(state: LoadingState, onClick: () -> Unit) {

    Button(
        onClick = {
            onClick()
        },
        enabled = state != LoadingState.LOADING,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor)
    ) {
        if (state == LoadingState.LOADING) {
            CircularProgressIndicator(color = MaterialTheme.colors.ButtonColor)
        } else {
            Text(
                text = "Activate",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun ExistingTaskAppBarActions(
    navController: NavController,
    rentalNumber: RentalNumberData,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    var openCancelDialog by remember { mutableStateOf(false) }
    var openRenewDialog by remember { mutableStateOf(false) }
    DropMenu(
        onRenewClicked = { openRenewDialog = true },
        onCancelClicked = { openCancelDialog = true }
    )

    DisplayAlertDialog(
        title = "Cancel ${rentalNumber.number}",
        message = {
            Column() {
                Text(
                    text = stringResource(R.string.renew_rental_numbers_notice),
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.renew_rental_number_decision),
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    fontWeight = FontWeight.Normal
                )
            }
        },
        openDialog = openCancelDialog,
        closeDialog = { openCancelDialog = false },
        onYesClicked = {
            mainViewModel.requestRefundRentalNumber(
                rentalNumberData = rentalNumber,
                snackbar = showSnackbar,
                navController = navController
            )
        }
    )

    DisplayAlertDialog(
        title = "Renew ${rentalNumber.number}",
        message = {
            Text(
                text = "Only 30 day rental numbers can be extended/renewed! \nRenew this number for ${
                    dollarToCreditForPurchasingNumbers(
                        rentalNumber.price
                    )
                } credits, continue?", fontSize = MaterialTheme.typography.subtitle1.fontSize
            )
        },
        openDialog = openRenewDialog,
        closeDialog = { openRenewDialog = false },
        onYesClicked = {
            mainViewModel.renewRentalNumber(
                rentalNumberData = rentalNumber,
                snackbar = showSnackbar,
                navController = navController
            )
        }
    )

//    CancelAction(onCancelClicked = { openDialog = true })
//    RefreshAction(onRefreshClicked = {
////        mainViewModel.refreshMessageCheck(
////            context = context,
////            temporaryNumberId = tempNumber.temporaryNumberId,
////            snackbar = showSnackbar
////        )
//    })
}

@Composable
fun DropMenu(onRenewClicked: () -> Unit, onCancelClicked: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            painterResource(id = R.drawable.ic_more_vert),
            contentDescription = "Menu",
            tint = MaterialTheme.colors.topAppBarContentColor
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(onClick = {
                expanded = false
                onRenewClicked()
            }) {
                Text(
                    text = "Renew number",
                    modifier = Modifier.padding(start = 5.dp),
//                    fontSize = MaterialTheme.typography.subtitle2.fontSize
                )
            }
            DropdownMenuItem(onClick = {
                expanded = false
                onCancelClicked()
            }) {
                Text(
                    text = "Cancel number",
                    modifier = Modifier.padding(start = 5.dp),
//                    fontSize = MaterialTheme.typography.subtitle2.fontSize
                )
            }
        }
    }
}

//@Composable
//fun CancelAction(
//    onCancelClicked: () -> Unit
//) {
//    IconButton(onClick = { onCancelClicked() }) {
//        Icon(
//            imageVector = Icons.Default.Close, contentDescription = "Cancel Button",
//            tint = MaterialTheme.colors.topAppBarContentColor
//        )
//    }
//}

//@Composable
//fun RefreshAction(
//    onRefreshClicked: () -> Unit
//) {
//    IconButton(onClick = { onRefreshClicked() }) {
//        Icon(
//            imageVector = Icons.Default.Refresh, contentDescription = "Refresh Button",
//            tint = MaterialTheme.colors.topAppBarContentColor
//        )
//    }
//}