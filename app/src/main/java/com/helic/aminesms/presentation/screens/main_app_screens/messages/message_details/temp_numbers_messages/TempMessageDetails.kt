package com.helic.aminesms.presentation.screens.main_app_screens.messages.message_details.temp_numbers_messages

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.helic.aminesms.R
import com.helic.aminesms.data.models.number_data.TempNumberData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.*
import com.helic.aminesms.utils.*
import com.helic.aminesms.utils.Constants.REUSE_DISCOUNT_PERCENT
import com.helic.aminesms.utils.Constants.TIME_BETWEEN_AUTO_REFRESH
import kotlinx.coroutines.delay

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TempMessageDetails(
    navController: NavController,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current

    val isRefreshing by mainViewModel.isRefreshing.collectAsState()
    val sms = mainViewModel.message?.value
    val temporaryNumber = mainViewModel.selectedTempNumber.value
    val state by mainViewModel.checkingTempMessagesLoadingState.collectAsState()

    val reuseTempNumberState by mainViewModel.reuseTemNumberLoadingState.collectAsState()
    val list = mutableListOf(sms)

    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = showSnackbar)
    }

    LaunchedEffect(key1 = temporaryNumber.state) {
        // We relaunch the function after state of the number changes so we recheck the reusable numbers list
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
                tempNumberData = temporaryNumber,
                snackbar = showSnackbar,
                userBalance = userBalance
            )
        )
    }
    var remainingReuseTime by remember {
        mutableStateOf(
            calculatingRemainingReuseTime(
                tempNumberData = temporaryNumber
            )
        )
    }

    LaunchedEffect(key1 = remainingExpirationTime) {
        delay(1000L)
        remainingExpirationTime = calculatingRemainingExpirationTime(
            context = context,
            tempNumberData = temporaryNumber,
            snackbar = showSnackbar,
            userBalance = userBalance
        )
    }

    LaunchedEffect(key1 = remainingReuseTime) {
        delay(1000L)
        remainingReuseTime = calculatingRemainingReuseTime(
            tempNumberData = temporaryNumber
        )
    }

    //This is added to auto-check the incoming messages every TIME_BETWEEN_AUTO_REFRESH,
    // we added the variable counter to auto-update the LaunchedEffect
    // we added the condition to reduce resources usage when there is a message

    if (sms == null && hasInternetConnection(context = context)) {
        var counter by remember { mutableStateOf(0) }
        LaunchedEffect(key1 = counter) {
            delay(timeMillis = TIME_BETWEEN_AUTO_REFRESH)
            counter += 1
            mainViewModel.autoCheckTempMessage(
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
            temporaryTempNumber = temporaryNumber,
            showSnackbar = showSnackbar
        )
    }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                mainViewModel.refreshTempNumberMessageCheck(
                    context = context,
                    temporaryNumberId = temporaryNumber.temporaryNumberId,
                    snackbar = showSnackbar
                )
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.backgroundColor
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = when {
                                remainingExpirationTime > 0 -> "Expires in ${
                                    convertSeconds(
                                        remainingExpirationTime
                                    )
                                }"
                                else -> {
                                    temporaryNumber.state
                                }
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.TextColor
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
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
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
                                                    text = stringResource(R.string.number_can_be_reused_within),
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colors.TextColor
                                                )
                                                Text(
                                                    text = convertSeconds(remainingReuseTime),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colors.TextColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.padding(10.dp))
                                            ReuseNumber(
                                                mainViewModel = mainViewModel,
                                                temporaryTempNumber = temporaryNumber,
                                                showSnackbar = showSnackbar,
                                                navController = navController,
                                                loadingState = reuseTempNumberState
                                            )
                                        }
                                        else -> {
                                            CantBeReusedDisplay()
                                        }
                                    }
                                } else {
                                    CantBeReusedDisplay()
                                }
                            }
                            else -> Unit
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
    temporaryTempNumber: TempNumberData,
    showSnackbar: (String, SnackbarDuration) -> Unit,
) {
    TopAppBar(
        modifier = Modifier.height(TOP_APP_BAR_HEIGHT),
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.TempNumbersMessages.route) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }

            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIos,
                    contentDescription = "Back Arrow",
                    tint = MaterialTheme.colors.topAppBarContentColor
                )
            }
        },
        title = {
            Text(
                text = mainViewModel.selectedTempNumber.value.number,
                color = MaterialTheme.colors.topAppBarContentColor
            )
        },
        actions = {
            ExistingTaskAppBarActions(
                context = context,
                navController = navController,
                tempNumber = temporaryTempNumber,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        },
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun ReuseNumber(
    mainViewModel: MainViewModel,
    temporaryTempNumber: TempNumberData,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    navController: NavController,
    loadingState: LoadingState
) {
    var openDialog by remember { mutableStateOf(false) }
    ReuseButton(loadingState = loadingState) { openDialog = true }

    DisplayAlertDialog(
        title = "Reuse Number ${temporaryTempNumber.number}",
        message = {
            Text(
                text = "You can reuse this number for a $REUSE_DISCOUNT_PERCENT% off, Are you sure you want to continue?",
                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                fontWeight = FontWeight.Normal
            )
        },
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
fun ReuseButton(loadingState: LoadingState, onClick: () -> Unit) {
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
            },
        backgroundColor = MaterialTheme.colors.backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loadingState == LoadingState.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(30.dp),
                    color = MaterialTheme.colors.primary
                )
            } else {
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
            .clip(RoundedCornerShape(5.dp)),
        backgroundColor = MaterialTheme.colors.backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, top = 10.dp, bottom = 10.dp),
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
    tempNumber: TempNumberData?,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    DisplayAlertDialog(
        title = "Cancel ${tempNumber?.number}",
        message = {
            Text(
                text = "Are you sure you want to cancel this number?",
                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                fontWeight = FontWeight.Normal
            )
        },
        openDialog = openDialog,
        closeDialog = { openDialog = false },
        onYesClicked = {
            if (tempNumber != null) {
                if (tempNumber.state == NumberState.Pending.toString()) {
                    mainViewModel.cancelTempNumber(
                        context = context,
                        temporaryNumberId = tempNumber.temporaryNumberId,
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
        if (tempNumber != null) {
            mainViewModel.refreshTempNumberMessageCheck(
                context = context,
                temporaryNumberId = tempNumber.temporaryNumberId,
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