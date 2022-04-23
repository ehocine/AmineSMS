package com.helic.aminesms.presentation.screens.main_app_screens.messages.message_details

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.helic.aminesms.R
import com.helic.aminesms.data.models.number_data.NumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.presentation.ui.theme.phoneMessagesTextColor
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.*
import com.helic.aminesms.utils.Constants.REUSE_DISCOUNT
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MessageDetails(
    navController: NavController,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current

    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val sms = mainViewModel.message?.value
    val temporaryNumber = mainViewModel.selectedNumber.value
    val state by mainViewModel.checkingMessagesLoadingStateOfViewModel.collectAsState()

    val list = mutableListOf(sms)

    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = showSnackbar)
    }

    LaunchedEffect(key1 = state) {
        mainViewModel.getReusableNumbersList(snackbar = showSnackbar)
    }

    val userBalance = mainViewModel.userBalance.collectAsState().value

    var remainingExpirationTime by remember {
        mutableStateOf(
            calculatingRemainingExpirationTime(
                context = context,
                numberData = temporaryNumber,
                snackbar = showSnackbar,
                userBalance = userBalance
            )
        )
    }
    var remainingReuseTime by remember {
        mutableStateOf(
            calculatingRemainingTime(
                numberData = temporaryNumber
            )
        )
    }

    LaunchedEffect(key1 = remainingExpirationTime) {
        delay(1000L)
        remainingExpirationTime = calculatingRemainingExpirationTime(
            context = context,
            numberData = temporaryNumber,
            snackbar = showSnackbar,
            userBalance = userBalance
        )
    }

    LaunchedEffect(key1 = remainingReuseTime) {
        delay(1000L)
        remainingReuseTime = calculatingRemainingTime(
            numberData = temporaryNumber
        )
    }

    Scaffold(topBar = {
        MessageDetailsTopAppBar(
            context = context,
            navController = navController,
            mainViewModel = mainViewModel,
            temporaryNumberId = temporaryNumber.temporaryNumberId,
            showSnackbar = showSnackbar
        )
    }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                mainViewModel.refreshMessageCheck(
                    context = context,
                    temporaryNumberId = temporaryNumber.temporaryNumberId,
                    snackbar = showSnackbar
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text =
                    when {
                        remainingExpirationTime > 0 -> "Expires in ${
                            convertSeconds(
                                remainingExpirationTime
                            )
                        }"
                        else -> temporaryNumber.state
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.phoneMessagesTextColor
                )
                Spacer(modifier = Modifier.padding(20.dp))
                when (state) {
                    LoadingState.LOADING -> LoadingList()
                    LoadingState.ERROR -> ErrorLoadingResults()
                    else -> {
                        if (sms != null) {
                            MessageDetailItem(listOfMessages = list)
                        } else {
                            NoResults()
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    when (temporaryNumber.state) {
                        NumberState.Completed.toString() -> {
                            if (mainViewModel.reusableNumbersList.value.find
                                { it.reusableId == temporaryNumber.temporaryNumberId } != null
                            ) {
                                when {
                                    remainingReuseTime > 0 -> {
                                        Text(
                                            text = "Number can be reused within ${
                                                convertSeconds(remainingReuseTime)
                                            }",
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colors.phoneMessagesTextColor
                                        )
                                        ReuseNumber(
                                            mainViewModel = mainViewModel,
                                            temporaryNumber = temporaryNumber,
                                            showSnackbar = showSnackbar,
                                            navController = navController
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "Time expired, number can no longer be reused",
                                            fontWeight = FontWeight.Medium,
                                            color = Red
                                        )
                                    }
                                }

                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun MessageDetailsTopAppBar(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    temporaryNumberId: String,
    showSnackbar: (String, SnackbarDuration) -> Unit,
) {
    val orderedNumber =
        mainViewModel.orderedNumbersList.value.find { it.temporaryNumberId == temporaryNumberId }
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.Messages.route) {
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
        title = { Text(text = mainViewModel.selectedNumber.value.number) },
        actions = {
            ExistingTaskAppBarActions(
                context = context,
                navController = navController,
                number = orderedNumber,
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
    temporaryNumber: NumberData,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    navController: NavController
) {
    var openDialog by remember { mutableStateOf(false) }
    ReuseButton(number = temporaryNumber, onClick = { openDialog = true })

    DisplayAlertDialog(
        title = "Reuse Number ${temporaryNumber.number}",
        message = "You can reuse this number for ${
            dollarToCreditForPurchasingNumbers(
                temporaryNumber.price
            ) * REUSE_DISCOUNT
        } credits, Are you sure you want to continue?",
        openDialog = openDialog,
        closeDialog = { openDialog = false },
        onYesClicked = {
            mainViewModel.reuseNumber(
                temporaryNumberId = temporaryNumber.temporaryNumberId,
                snackbar = showSnackbar,
                navController = navController
            )
        }
    )
}

@Composable
fun ReuseButton(number: NumberData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(5.dp)
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Recycling,
                contentDescription = "",
                tint = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = "Reuse Number for ${
                    dollarToCreditForPurchasingNumbers(
                        number.price
                    ) * REUSE_DISCOUNT
                }",
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }

    }

}

@Composable
fun ExistingTaskAppBarActions(
    context: Context,
    navController: NavController,
    number: NumberData?,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }

    DisplayAlertDialog(
        title = "Cancel ${number?.number}",
        message = "Are you sure you want to cancel this number?",
        openDialog = openDialog,
        closeDialog = { openDialog = false },
        onYesClicked = {
            if (number != null) {
                if (number.state == NumberState.Pending.toString()) {
                    mainViewModel.cancelTempNumber(
                        context = context,
                        temporaryNumberId = number.temporaryNumberId,
                        snackbar = showSnackbar,
                        navController = navController
                    )
                } else {
                    showSnackbar(
                        context.getString(R.string.cant_cancel_number),
                        SnackbarDuration.Short
                    )
                }
            }

        }
    )

    CancelAction(onDeleteClicked = { openDialog = true })
    RefreshAction(onRefreshClicked = {
        if (number != null) {
            mainViewModel.refreshMessageCheck(
                context = context,
                temporaryNumberId = number.temporaryNumberId,
                snackbar = showSnackbar
            )
//            mainViewModel.checkMessageReuseNumber(
//                temporaryNumberId = orderedNumber.temporaryNumberId,
//                snackbar = showSnackbar
//            )
        }
    })
}

@Composable
fun CancelAction(
    onDeleteClicked: () -> Unit
) {
    IconButton(onClick = { onDeleteClicked() }) {
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