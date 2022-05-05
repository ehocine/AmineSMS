package com.helic.aminesms.presentation.screens.main_app_screens.order_rental_numbers


import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.DROP_DOWN_HEIGHT
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.ErrorLoadingResults
import com.helic.aminesms.utils.LoadingList
import com.helic.aminesms.utils.LoadingState
import com.helic.aminesms.utils.dollarToCreditForPurchasingNumbers

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RentalNumberOptions(
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = showSnackbar)
    }
    val selectedServiceState = mainViewModel.selectedServiceState.value
    val balance = mainViewModel.userBalance.collectAsState().value

    mainViewModel.selectedAreaCode.value = ""

    val state by mainViewModel.loadingStateOfViewModel.collectAsState()
    val buyingState by mainViewModel.buyingLoadingStateOfViewModel.collectAsState()
    Scaffold(
        topBar = {
            RentNumberOptionsTopAppBar(
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
                    userBalance = balance
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
    userBalance: Double
) {

    Column(
        modifier = Modifier.padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Add options to your number",
            fontWeight = FontWeight.Bold,
            fontSize = MaterialTheme.typography.h5.fontSize
        )
        Spacer(modifier = Modifier.padding(20.dp))
        DropDownOptions(
            "Area code",
            listOf()
        ) {
            mainViewModel.selectedAreaCode.value = it
        }
        Spacer(modifier = Modifier.padding(20.dp))
        Button(
            onClick = {
                if (userBalance >= dollarToCreditForPurchasingNumbers(mainViewModel.selectedServiceState.value.price)) {
                    mainViewModel.orderNumber(
                        navController = navController,
                        serviceID = mainViewModel.selectedServiceState.value.serviceId,
                        areaCode = mainViewModel.selectedAreaCode.value,
                        snackbar = snackbar
                    )
                } else {
                    snackbar(context.getString(R.string.not_enough_balance), SnackbarDuration.Short)
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor)
        ) {
            if (buyingState == LoadingState.LOADING) {
                CircularProgressIndicator(color = MaterialTheme.colors.ButtonColor)
            } else {
                Text(
                    text = "Order number for ${dollarToCreditForPurchasingNumbers(mainViewModel.selectedServiceState.value.price)} credits",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RentNumberOptionsTopAppBar(navController: NavController, serviceStateName: String?) {
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

@Composable
fun DropDownOptions(
    label: String,
    optionsList: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(label) }
    val angle: Float by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f
    )
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .onGloballyPositioned {
                parentSize = it.size
            }
            .background(MaterialTheme.colors.background)
            .height(DROP_DOWN_HEIGHT)
            .clickable { expanded = true }
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(
                    alpha = ContentAlpha.disabled
                ),
                shape = MaterialTheme.shapes.small
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(weight = 8f)
                .padding(start = 10.dp),
        ) {
            Text(
                text = selectedOption,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Medium
            )
        }
        IconButton(
            modifier = Modifier
                .alpha(ContentAlpha.medium)
                .rotate(degrees = angle)
                .weight(weight = 1.5f),
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Drop Down Arrow"
            )
        }
        DropdownMenu(
            modifier = Modifier
                .width(with(LocalDensity.current) { parentSize.width.toDp() }),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            optionsList.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        selectedOption = option
                        onOptionSelected(option)
                    }
                ) {
                    Text(text = option)
                }
            }
        }
    }
}