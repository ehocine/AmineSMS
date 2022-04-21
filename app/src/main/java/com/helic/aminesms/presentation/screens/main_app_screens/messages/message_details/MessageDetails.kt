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
import com.helic.aminesms.data.models.order_number.OrderedNumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
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
        mainViewModel.getReusableNumbers(snackbar = showSnackbar)
    }

    val userBalance = mainViewModel.userBalance.collectAsState().value
    var remainingTime by remember {
        mutableStateOf(
            calculatingRemainingTime(
                context = context,
                orderedNumberData = temporaryNumber,
                snackbar = showSnackbar,
                userBalance = userBalance
            )
        )
    }

    LaunchedEffect(key1 = remainingTime) {
        delay(1000L)
        remainingTime = calculatingRemainingTime(
            context = context,
            orderedNumberData = temporaryNumber,
            snackbar = showSnackbar,
            userBalance = userBalance
        )

    }

    val numberState by remember { mutableStateOf(temporaryNumber.state) }

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
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text =
                    when {
                        remainingTime > 0 -> "Expires in $remainingTime second(s)"
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
                when (numberState) {
                    NumberState.Completed.toString() -> {
                        if (mainViewModel.reusableNumbersList.value.find { it.reusableId == temporaryNumber.temporaryNumberId } != null) {
                            ReuseNumber(
                                mainViewModel = mainViewModel,
                                temporaryNumber = temporaryNumber,
                                showSnackbar = showSnackbar
                            )
                        }
                    }
                    else -> Unit
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
                orderedNumber = orderedNumber,
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
    temporaryNumber: OrderedNumberData,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    ReuseButton(number = temporaryNumber,onClick = { openDialog = true })

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
                snackbar = showSnackbar
            )
        }
    )
}

@Composable
fun ReuseButton(number: OrderedNumberData, onClick: () -> Unit) {
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
    orderedNumber: OrderedNumberData?,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }

    DisplayAlertDialog(
        title = "Cancel ${orderedNumber?.number}",
        message = "Are you sure you want to cancel this number?",
        openDialog = openDialog,
        closeDialog = { openDialog = false },
        onYesClicked = {
            if (orderedNumber != null) {
                if (orderedNumber.state == NumberState.Pending.toString()) {
                    mainViewModel.cancelTempNumber(
                        context = context,
                        temporaryNumberId = orderedNumber.temporaryNumberId,
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
        if (orderedNumber != null) {
            mainViewModel.refreshMessageCheck(
                context = context,
                temporaryNumberId = orderedNumber.temporaryNumberId,
                snackbar = showSnackbar
            )
            mainViewModel.checkMessageReuseNumber(
                temporaryNumberId = orderedNumber.temporaryNumberId,
                snackbar = showSnackbar
            )
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