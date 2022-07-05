package com.helic.aminesms.presentation.screens.main_app_screens.messages.temp_numbers_messages_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TempNumbersMessages(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {

    val context = LocalContext.current

    val state by mainViewModel.gettingListOfTempNumbersLoadingState.collectAsState()

    val listOfOrderedNumbers = mainViewModel.orderedTempNumbersList.collectAsState().value

    LaunchedEffect(key1 = listOfOrderedNumbers) {
        mainViewModel.getListOfTempNumbersFromFirebase(context = context, snackbar = snackbar)
//        mainViewModel.getReusableNumbers(snackbar = snackbar)
    }
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
                    navController.navigate(MainAppScreens.OrderNumbers.route) {
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
                TempNumberMessageItem(
                    context = context,
                    navController = navController,
                    mainViewModel = mainViewModel,
                    listOfPhoneTempNumbers = listOfOrderedNumbers,
                    showSnackbar = snackbar
                )
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
                text = stringResource(R.string.temp_numbers),
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
fun DisplayInfoAction() {
    var openDialog by remember { mutableStateOf(false) }

    DisplayInfoDialog(
        title = "Info",
        message = {
            Text(
                buildAnnotatedString {
                    append("You can reuse temporary numbers for a ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("FEW MINUTES")
                    }
                    append(
                        " after receiving a code.\n" +
                                "This system cannot return a number if it is not shown here as reusable.\n" +
                                "If you need guaranteed reuse, buy a rental number."
                    )
                },
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
