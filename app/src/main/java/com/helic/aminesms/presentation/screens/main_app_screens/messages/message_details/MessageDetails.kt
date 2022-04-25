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
import androidx.compose.material.icons.filled.Redo
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
import com.helic.aminesms.data.models.messages.Sms
import com.helic.aminesms.data.models.number_data.NumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.presentation.ui.theme.phoneMessagesTextColor
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.*
import com.helic.aminesms.utils.Constants.REUSE_DISCOUNT_PERCENT
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
//        val reusableNumber =
//            mainViewModel.reusableNumbersList.value.find { it.reusableId == temporaryNumber.temporaryNumberId }
//        if (reusableNumber != null) {
//            temporaryNumber.apply {
//                reuseableUntil = reusableNumber.reuseableUntil
//            }
//            Log.d("Tag", "NumberID: ${temporaryNumber.temporaryNumberId} reusable ${temporaryNumber.reuseableUntil}")
//        }
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
            calculatingRemainingReuseTime(
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
        remainingReuseTime = calculatingRemainingReuseTime(
            numberData = temporaryNumber
        )
    }

    //This is added to autocheck the incoming messages every TIME_BETWEEN_AUTOREFRESH,
    // we added the variable counter to autoupdate the LaunchedEffect
    // we added the condition to reduce resources usage when there is a message

    if (sms == null) {
        var counter by remember { mutableStateOf(0) }
        LaunchedEffect(key1 = counter) {
            delay(1000L)
            counter += 1
            mainViewModel.autoCheckMessage(
                context = context,
                temporaryNumberId = temporaryNumber.temporaryNumberId,
                snackbar = showSnackbar
            )
        }
    }


    Scaffold(topBar = {
        MessageDetailsTopAppBar(
            context = context,
            navController = navController,
            mainViewModel = mainViewModel,
            temporaryNumber = temporaryNumber,
            sms = sms,
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
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                        Row {
                                            Text(
                                                text = "Number can be reused within ",
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colors.phoneMessagesTextColor
                                            )
                                            Text(
                                                text = convertSeconds(remainingReuseTime),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colors.phoneMessagesTextColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.padding(10.dp))
                                        ReuseNumber(
                                            mainViewModel = mainViewModel,
                                            temporaryNumber = temporaryNumber,
                                            showSnackbar = showSnackbar,
                                            navController = navController
                                        )
                                    }
                                    else -> {
                                        CantBeReusedDisplay()
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
    temporaryNumber: NumberData,
    sms: Sms?,
    showSnackbar: (String, SnackbarDuration) -> Unit,
) {
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
                number = temporaryNumber,
                sms = sms,
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
    ReuseButton { openDialog = true }

    DisplayAlertDialog(
        title = "Reuse Number ${temporaryNumber.number}",
        message = "You can reuse this number for a $REUSE_DISCOUNT_PERCENT% off, Are you sure you want to continue?",
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
fun ReuseButton(onClick: () -> Unit) {
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
                imageVector = Icons.Default.Redo,
                contentDescription = "",
                tint = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = "Reuse number for a $REUSE_DISCOUNT_PERCENT% off",
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CantBeReusedDisplay() {
    Card(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Red,
                shape = RoundedCornerShape(5.dp)
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Number can no longer be reused",
                fontWeight = FontWeight.Medium,
                color = Red
            )
        }

    }
}

@Composable
fun ExistingTaskAppBarActions(
    context: Context,
    navController: NavController,
    number: NumberData?,
    sms: Sms?,
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

    CancelAction(onCancelClicked = { openDialog = true })
    RefreshAction(onRefreshClicked = {
        if (number != null) {
            mainViewModel.refreshMessageCheck(
                context = context,
                temporaryNumberId = number.temporaryNumberId,
                snackbar = showSnackbar
            )
        }
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