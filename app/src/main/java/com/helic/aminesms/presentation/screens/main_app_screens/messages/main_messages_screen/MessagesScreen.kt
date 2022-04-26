package com.helic.aminesms.presentation.screens.main_app_screens.messages.main_messages_screen

import android.annotation.SuppressLint
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.screens.main_app_screens.Profile
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.utils.ErrorLoadingResults
import com.helic.aminesms.utils.LoadingList
import com.helic.aminesms.utils.LoadingState
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun Messages(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {

    val context = LocalContext.current

    val state by mainViewModel.gettingListOfNumbersLoadingState.collectAsState()

    val listOfOrderedNumbers = mainViewModel.orderedNumbersList.collectAsState().value

    LaunchedEffect(key1 = listOfOrderedNumbers) {
        mainViewModel.getListOfNumbersFromFirebase(context = context, snackbar = snackbar)
//        mainViewModel.getReusableNumbers(snackbar = snackbar)
    }
    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = snackbar)
    }

    mainViewModel.userBalance.collectAsState().value

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MessagesTopAppBar {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }
        },
        floatingActionButton = {
            ExpandedMenuButtons(onItemClicked = {
                when (it) {
                    "Buy a temp number" -> {
                        navController.navigate(MainAppScreens.OrderNumbers.route) {
                            launchSingleTop = true
                        }
                    }
                    else -> {
                        navController.navigate(MainAppScreens.RentalNumbers.route) {
                            launchSingleTop = true
                        }
                    }
                }
            })
        },
        drawerContent = {
            Profile(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = snackbar
            )
        }) {
        when (state) {
            LoadingState.LOADING -> LoadingList()
            LoadingState.ERROR -> ErrorLoadingResults()
            else -> {
                PhoneMessageItem(
                    context = context,
                    navController = navController,
                    mainViewModel = mainViewModel,
                    listOfPhoneNumbers = listOfOrderedNumbers,
                    showSnackbar = snackbar
                )
            }
        }
    }
}

@Composable
fun MessagesTopAppBar(openDrawer: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { openDrawer() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Navigation Drawer Icon"
                )
            }
        }, title = {
            Text(text = stringResource(R.string.messages))
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun ExpandedMenuButtons(
    onItemClicked: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val list = listOf("Buy a temp number", "Rent a number")

    ExtendedFloatingActionButton(
        onClick = {
            expanded = true
        },
        backgroundColor = MaterialTheme.colors.ButtonColor,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Button",
                tint = Color.White
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                list.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            onItemClicked(item)
                        }
                    ) {
                        Text(text = item)
                    }
                }
            }
        },
        text = { Text(text = stringResource(R.string.add_number), color = Color.White) }
    )
}