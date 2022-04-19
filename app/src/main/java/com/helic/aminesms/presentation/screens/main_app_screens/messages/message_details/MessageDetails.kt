package com.helic.aminesms.presentation.screens.main_app_screens.messages.message_details

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MessageDetails(
    navController: NavController,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context, showSnackbar)
    }
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val sms = mainViewModel.message?.value
    val temporaryNumber = mainViewModel.selectedNumber.value
    val state by mainViewModel.checkingMessagesLoadingStateOfViewModel.collectAsState()

    val list = mutableListOf(sms)

    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = showSnackbar)
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
    var openDialog by remember { mutableStateOf(false) }
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
                temporaryNumberId = temporaryNumberId,
                showSnackbar = showSnackbar
            )
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun ExistingTaskAppBarActions(
    context: Context,
    navController: NavController,
    orderedNumber: OrderedNumberData?,
    mainViewModel: MainViewModel,
    temporaryNumberId: String,
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
                        temporaryNumberId = temporaryNumberId,
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
        mainViewModel.refreshMessageCheck(
            context = context,
            temporaryNumberId = temporaryNumberId,
            snackbar = showSnackbar
        )
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