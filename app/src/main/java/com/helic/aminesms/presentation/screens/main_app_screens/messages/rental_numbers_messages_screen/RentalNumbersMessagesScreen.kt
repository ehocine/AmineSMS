package com.helic.aminesms.presentation.screens.main_app_screens.messages.rental_numbers_messages_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.ErrorLoadingResults
import com.helic.aminesms.utils.LoadingList
import com.helic.aminesms.utils.LoadingState

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
                        tint = Color.White
                    )
                },
                text = { Text(text = stringResource(R.string.add_number), color = Color.White) }
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
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.Home.route) {
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
        }, title = {
            Text(text = stringResource(R.string.rental_numbers))
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun <T> PendingRentalNumbersCount(listOfNumbers: List<T>) {
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
                .clip(RoundedCornerShape(5.dp))
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
