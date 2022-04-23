package com.helic.aminesms.presentation.screens.main_app_screens.order_temp_numbers

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.*
import com.helic.aminesms.utils.ErrorLoadingResults
import com.helic.aminesms.utils.LoadingList
import com.helic.aminesms.utils.LoadingState
import com.helic.aminesms.utils.dollarToCreditForPurchasingNumbers

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun OrderNumberOptions(
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = showSnackbar)
        mainViewModel.getSuperUserBalance(snackbar = showSnackbar)
    }
    val selectedServiceState = mainViewModel.selectedServiceState.value
    val balance = mainViewModel.userBalance.collectAsState().value
    val superUserBalanceState by mainViewModel.checkingSuperUserBalanceLoadingState.collectAsState()
    val superUserBalance = mainViewModel.superUserBalance.value

    mainViewModel.selectedAreaCode.value = ""

    val state by mainViewModel.loadingStateOfViewModel.collectAsState()
    val buyingState by mainViewModel.buyingLoadingStateOfViewModel.collectAsState()
    Scaffold(
        topBar = {
            OrderNumberOptionsTopAppBar(
                navController = navController,
                serviceStateName = selectedServiceState.name
            )
        }
    ) {
        when (state) {
            LoadingState.LOADING -> LoadingList()
            LoadingState.ERROR -> ErrorLoadingResults()
            else -> {
                DisplayOptions(
                    context = context,
                    navController = navController,
                    mainViewModel = mainViewModel,
                    buyingState = buyingState,
                    snackbar = showSnackbar,
                    userBalance = balance,
                    superUserBalance = superUserBalance,
                    superUserState = superUserBalanceState
                )
            }
        }
    }
}

@Composable
fun DisplayOptions(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    buyingState: LoadingState,
    snackbar: (String, SnackbarDuration) -> Unit,
    userBalance: Double,
    superUserBalance: Double,
    superUserState: LoadingState
) {
    var areaCodeValue by remember { mutableStateOf("") }
    var invalidAreaCode by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(vertical = 50.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Add options to your number",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.h5.fontSize
            )
            Spacer(modifier = Modifier.padding(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedTextField(
                    value = areaCodeValue,
                    onValueChange = {
                        areaCodeValue = it
                        mainViewModel.selectedAreaCode.value = it
                    },
                    label = { Text(text = "Area code") },
                    placeholder = { Text(text = "Area code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                invalidAreaCode = when {
                    areaCodeValue.isEmpty() -> {
                        false
                    }
                    else -> {
                        areaCodeValue.length < 3
                    }
                }
                Spacer(modifier = Modifier.padding(10.dp))
                Text(
                    text = when (invalidAreaCode) {
                        false -> "Leave blank for no filter"
                        else -> "Invalid length"
                    },
                    color = when (invalidAreaCode) {
                        false -> MediumGray
                        else -> Red
                    }
                )
                Spacer(modifier = Modifier.padding(20.dp))
                Button(
                    onClick = {
                        if (superUserBalance > mainViewModel.selectedServiceState.value.price) {
                            if (userBalance >= dollarToCreditForPurchasingNumbers(
                                    mainViewModel.selectedServiceState.value.price
                                )
                            ) {
                                if (!invalidAreaCode) {
                                    mainViewModel.orderNumber(
                                        navController = navController,
                                        serviceID = mainViewModel.selectedServiceState.value.serviceId,
                                        areaCode = mainViewModel.selectedAreaCode.value,
                                        snackbar = snackbar
                                    )
                                }
                            } else {
                                snackbar(
                                    context.getString(R.string.not_enough_balance),
                                    SnackbarDuration.Short
                                )
                            }
                        } else {
                            snackbar(
                                context.getString(R.string.cant_purchase),
                                SnackbarDuration.Short
                            )
                        }
                    },
                    enabled = buyingState != LoadingState.LOADING && superUserState != LoadingState.ERROR && superUserState != LoadingState.LOADING,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor)
                ) {
                    if (buyingState == LoadingState.LOADING) {
                        CircularProgressIndicator(color = MaterialTheme.colors.ButtonColor)
                    } else {
                        Text(
                            text = "Order number for ${
                                dollarToCreditForPurchasingNumbers(
                                    mainViewModel.selectedServiceState.value.price
                                )
                            } credits",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun OrderNumberOptionsTopAppBar(navController: NavController, serviceStateName: String?) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.OrderNumbers.route) {
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
        title = {
            if (serviceStateName != null) {
                Text(text = serviceStateName)
            }
        }, backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}


