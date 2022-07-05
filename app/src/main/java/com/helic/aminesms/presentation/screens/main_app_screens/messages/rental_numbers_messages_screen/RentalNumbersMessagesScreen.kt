package com.helic.aminesms.presentation.screens.main_app_screens.messages.rental_numbers_messages_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Info
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
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.*
import com.helic.aminesms.utils.DisplayInfoDialog
import com.helic.aminesms.utils.ErrorLoadingResults
import com.helic.aminesms.utils.LoadingList
import com.helic.aminesms.utils.LoadingState

//TODO: check when the user cancel a number or delete it from the list that it has been properly updated or removed
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RentalNumbersMessages(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {

    val context = LocalContext.current

    val state by mainViewModel.gettingListOfRentalNumbersLoadingState.collectAsState()

    val listOfOrderedNumbers = mainViewModel.orderedRentalNumbers.collectAsState().value

    LaunchedEffect(key1 = listOfOrderedNumbers) {
        mainViewModel.getListOfRentalNumbersFromFirebase(context = context, snackbar = snackbar)
        mainViewModel.getLiveRentalNumbersList(snackbar = snackbar)
        mainViewModel.getPendingRentalNumbersList(snackbar = snackbar)
    }

    val listOfLiveRentalNumbers by mainViewModel.listOfLiveRentalNumbers

    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = snackbar)
    }

    mainViewModel.userBalance.collectAsState().value
    Scaffold(
        topBar = {
            MessagesTopAppBar(navController = navController)
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(MainAppScreens.RentalNumbers.route) {
                        launchSingleTop = true
                    }
                },
                backgroundColor = MaterialTheme.colors.ButtonColor,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Button",
                        tint = MaterialTheme.colors.ButtonTextColor
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.add_number),
                        color = MaterialTheme.colors.ButtonTextColor
                    )
                }
            )

        }) {
        when (state) {
            LoadingState.LOADING -> LoadingList()
            LoadingState.ERROR -> ErrorLoadingResults()
            else -> {
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    // Number of pending numbers
                    if (mainViewModel.listOfPendingRentalNumbers.value.size != 0) {
                        PendingRentalNumbersCount(mainViewModel.listOfPendingRentalNumbers.value)
                    }
                    RentalNumberMessageItem(
                        context = context,
                        navController = navController,
                        mainViewModel = mainViewModel,
                        listOfRentalNumbers = listOfLiveRentalNumbers,
                        showSnackbar = snackbar
                    )
                }
            }
        }
    }
}

@Composable
fun MessagesTopAppBar(navController: NavController) {
    TopAppBar(
        modifier = Modifier.height(TOP_APP_BAR_HEIGHT),
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.Home.route) {
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
        }, title = {
            Text(
                text = stringResource(R.string.rental_numbers),
                color = MaterialTheme.colors.topAppBarContentColor
            )
        },
        actions = {
            DisplayInfoAction()
        },
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun <T> PendingRentalNumbersCount(listOfNumbers: List<T>) {
    Column(Modifier.background(MaterialTheme.colors.backgroundColor)) {
        Box(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.primary,
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
                        text = "Currently pending numbers: ${listOfNumbers.size}",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.primary
                    )
                }

            }
        }
    }
}


@Composable
fun DisplayInfoAction() {
    var openDialog by remember { mutableStateOf(false) }

    DisplayInfoDialog(
        title = "Info",
        message = {
            Text(
                text = "You must activate your rental before it can be used, every time. " +
                        "This request takes a few seconds. The full activation process can take up to 5 minutes. After activation, " +
                        "we will deliver all of your messages, if you have any.",
                modifier = Modifier.padding(10.dp),
                fontSize = MaterialTheme.typography.subtitle2.fontSize
            )
        },
        openDialog = openDialog,
        closeDialog = { openDialog = false }) {
    }
    InfoButton {
        openDialog = true
    }
}


@Composable
fun InfoButton(onClick: () -> Unit) {
    IconButton(onClick = {
        onClick()
    }) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info Button",
            tint = MaterialTheme.colors.topAppBarContentColor
        )
    }
}
