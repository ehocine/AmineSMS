package com.helic.aminesms.presentation.screens.main_app_screens.messages.message_details.rental_numbers_messages

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.helic.aminesms.data.models.number_data.RentalNumberData
import com.helic.aminesms.data.models.number_data.TempNumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.*
import com.helic.aminesms.utils.Constants.REUSE_DISCOUNT_PERCENT
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
            Column(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    Text(
//                        text =
//                        when {
//                            remainingExpirationTime > 0 -> "Expires in ${
//                                convertSeconds(
//                                    remainingExpirationTime
//                                )
//                            }"
//                            else -> temporaryNumber.state
//                        },
//                        fontWeight = FontWeight.Bold,
//                        color = MaterialTheme.colors.TextColor
//                    )
                    Spacer(modifier = Modifier.padding(20.dp))
                    when (state) {
                        LoadingState.LOADING -> LoadingList()
                        LoadingState.ERROR -> ErrorLoadingResults()
                        else -> {
                            if (rentalNumbersMessagesList.isNotEmpty()) {
                                MessageDetailItem(listOfMessages = rentalNumbersMessagesList)
                            } else {
                                NoResults()
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
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
                context = context,
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
fun ReuseNumber(
    mainViewModel: MainViewModel,
    temporaryTempNumber: TempNumberData,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    navController: NavController
) {
    var openDialog by remember { mutableStateOf(false) }
//    ActivateButton { openDialog = true }

    DisplayAlertDialog(
        title = "Reuse Number ${temporaryTempNumber.number}",
        message = "You can reuse this number for a $REUSE_DISCOUNT_PERCENT% off, Are you sure you want to continue?",
        openDialog = openDialog,
        closeDialog = { openDialog = false },
        onYesClicked = {
            mainViewModel.reuseNumber(
                temporaryNumberId = temporaryTempNumber.temporaryNumberId,
                snackbar = showSnackbar,
                navController = navController
            )
        }
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
    context: Context,
    navController: NavController,
    rentalNumber: RentalNumberData,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    DisplayAlertDialog(
        title = "Cancel ${rentalNumber.number}",
        message = "Are you sure you want to cancel this number?",
        openDialog = openDialog,
        closeDialog = { openDialog = false },
        onYesClicked = {
            mainViewModel.requestRefundRentalNumber(
                rentalNumberData = rentalNumber,
                snackbar = showSnackbar,
                navController = navController
            )
//            if (rentalNumber.state == NumberState.Pending.toString()) {
//                mainViewModel.cancelTempNumber(
//                    context = context,
//                    temporaryNumberId = RentalNumber.rentalId,
//                    snackbar = showSnackbar,
//                    navController = navController
//                )
//            } else {
//                showSnackbar(
//                    context.getString(R.string.cant_cancel_number),
//                    SnackbarDuration.Short
//                )
//            }

        }
    )

    CancelAction(onCancelClicked = { openDialog = true })
    RefreshAction(onRefreshClicked = {
//        mainViewModel.refreshMessageCheck(
//            context = context,
//            temporaryNumberId = tempNumber.temporaryNumberId,
//            snackbar = showSnackbar
//        )
    })
}

@Composable
fun CancelAction(
    onCancelClicked: () -> Unit
) {
    IconButton(onClick = { onCancelClicked() }) {
        Icon(
            imageVector = Icons.Default.Close, contentDescription = "Cancel Button",
            tint = MaterialTheme.colors.topAppBarContentColor
        )
    }
}

@Composable
fun RefreshAction(
    onRefreshClicked: () -> Unit
) {
    IconButton(onClick = { onRefreshClicked() }) {
        Icon(
            imageVector = Icons.Default.Refresh, contentDescription = "Refresh Button",
            tint = MaterialTheme.colors.topAppBarContentColor
        )
    }
}