package com.helic.aminesms.presentation.screens.main_app_screens.order_rental_numbers


import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.helic.aminesms.R
import com.helic.aminesms.data.models.rental_numbers.RentalNumberServiceState
import com.helic.aminesms.data.models.rental_numbers.rental_options.RentalOptionsData
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.*
import com.helic.aminesms.utils.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun RentalNumbers(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        mainViewModel.getSuperUserBalance(snackbar = snackbar)
        mainViewModel.getBalance(context = context, snackbar = snackbar)
        mainViewModel.getRentalServiceStateList(snackbar = snackbar)
        mainViewModel.getRentalNumberOptions(snackbar = snackbar)
    }
    val superUserCheckingBalanceState by mainViewModel.checkingSuperUserBalanceLoadingState.collectAsState()
    val superUserBalance = mainViewModel.superUserBalance.value

    mainViewModel.rentalPrice.value = 0.0

    val listOfRentalServices by mainViewModel.rentalServiceStateList
    val optionsList by mainViewModel.availableRentalOptions

    val isRefreshing by mainViewModel.isRefreshing.collectAsState()

    val price by mainViewModel.rentalPrice

    var selectedOption by remember { mutableStateOf(RentalNumberOption.WHOLE_LINE) }
    var serviceId by remember { mutableStateOf("") }

    val balance = mainViewModel.userBalance.collectAsState().value

    val state by mainViewModel.rentalServiceLoadingStateOfViewModel.collectAsState()
    val orderRentalNumberState by mainViewModel.orderRentalNumberLoadingState.collectAsState()

    Scaffold(topBar = {
        RentalNumberTopAppBar(navController = navController)
    }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { mainViewModel.refreshRentalServiceStateList(snackbar = snackbar) }
        ) {
            when (state) {
                LoadingState.LOADING -> LoadingList()
                LoadingState.ERROR -> ErrorLoadingResults()
                else -> {
                    Content(
                        context = context,
                        serviceStateList = listOfRentalServices,
                        optionsList = optionsList,
                        mainViewModel = mainViewModel,
                        onRentalOptionSelected = {
                            when (it.duration) {
                                "30.00:00:00" -> mainViewModel.rentalPeriodOption.value = 30
                                "14.00:00:00" -> mainViewModel.rentalPeriodOption.value = 14
                                else -> mainViewModel.rentalPeriodOption.value = 7
                            }
                            serviceId = when (selectedOption) {
                                RentalNumberOption.SINGLE_SERVICE -> {
                                    mainViewModel.selectedRentalService.value.serviceId
                                }
                                else -> {
                                    ""
                                }
                            }
                            mainViewModel.getRentalServicePrice(
                                durationInHours = mainViewModel.rentalPeriodOption.value * 24,
                                serviceId = serviceId,
                                snackbar = snackbar
                            )
                        },
                        onRentalNumberOptionChosen = {
                            selectedOption = it
                        },
                        priceInCredits = dollarToCreditForPurchasingNumbers(
                            price,
                            mainViewModel
                        ),
                        superUserBalance = superUserBalance,
                        superUserCheckingBalanceState = superUserCheckingBalanceState,
                        userBalance = balance,
                        proceedBuying = {
                            mainViewModel.orderRentalNumber(
                                serviceId = serviceId,
                                durationInHours = mainViewModel.rentalPeriodOption.value * 24,
                                navController = navController,
                                snackbar = snackbar
                            )
                        },
                        snackbar = snackbar,
                        orderRentalNumberState = orderRentalNumberState
                    )
                }
            }
        }
    }
}

@Composable
fun RentalNumberTopAppBar(
    navController: NavController
) {
    TopAppBar(
        modifier = Modifier.height(TOP_APP_BAR_HEIGHT),
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
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
                text = stringResource(R.string.rent_a_number),
                color = MaterialTheme.colors.topAppBarContentColor
            )
        },
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun Content(
    context: Context,
    serviceStateList: List<RentalNumberServiceState>,
    optionsList: List<RentalOptionsData>,
    mainViewModel: MainViewModel,
    onRentalOptionSelected: (RentalOptionsData) -> Unit,
    onRentalNumberOptionChosen: (RentalNumberOption) -> Unit,
    priceInCredits: Double,
    superUserBalance: Double,
    superUserCheckingBalanceState: LoadingState,
    userBalance: Double,
    proceedBuying: () -> Unit,
    snackbar: (String, SnackbarDuration) -> Unit,
    orderRentalNumberState: LoadingState
) {
    var selectedOption by remember { mutableStateOf(RentalNumberOption.WHOLE_LINE) }
    var userSelectedOption by remember { mutableStateOf(false) }
    var serviceText by remember { mutableStateOf("") }
    var serviceDecisionText by remember { mutableStateOf("") }

    val loadingPrice by mainViewModel.rentalServicePriceLoadingState.collectAsState()
    Column(Modifier.background(MaterialTheme.colors.backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == RentalNumberOption.WHOLE_LINE,
                    onClick = {
                        selectedOption = RentalNumberOption.WHOLE_LINE
                        onRentalNumberOptionChosen(selectedOption)

                        // To re-initialize the values on switching
                        mainViewModel.gotRentalPrice.value = false
                        mainViewModel.rentalPrice.value = 0.0
                    },
                    colors = RadioButtonDefaults.colors(Purple500)
                )
                Text(text = "Whole Line", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.padding(25.dp))
                RadioButton(
                    selected = selectedOption == RentalNumberOption.SINGLE_SERVICE,
                    onClick = {
                        selectedOption = RentalNumberOption.SINGLE_SERVICE
                        onRentalNumberOptionChosen(selectedOption)

                        // To re-initialize the values on switching
                        mainViewModel.gotRentalPrice.value = false
                        userSelectedOption = false
                        mainViewModel.searchTextState.value = ""
                        mainViewModel.selectedRentalService.value = RentalNumberServiceState()
                        mainViewModel.rentalPrice.value = 0.0
                    },
                    colors = RadioButtonDefaults.colors(Purple500)
                )
                Text(text = "Single Service", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.padding(20.dp))

            if (selectedOption == RentalNumberOption.SINGLE_SERVICE) {
                RentalServicesDropDownMenu(
                    label = "Search Service State",
                    optionsList = serviceStateList,
                    onTextChange = {
                        serviceText = it
                        serviceDecisionText = it
                    },
                    onOptionSelected = {
                        // We did it here because we need to check if a service was selected
                        mainViewModel.selectedRentalService.value = it
                        userSelectedOption = true
                        serviceText = it.name
                        serviceDecisionText = it.name
                    }
                )
                if (userSelectedOption) { // if the user didn't select a service we won't show the period options
                    Spacer(modifier = Modifier.padding(10.dp))
                    RentalOptionsDropDownMenu(
                        label = stringResource(R.string.select_period),
                        optionsList = optionsList.filter { it.rentalType == "SingleService" },
                        onRentalOptionSelected = onRentalOptionSelected
                    )
                }
            } else {
                RentalOptionsDropDownMenu(
                    label = stringResource(R.string.select_period),
                    optionsList = optionsList.filter { it.rentalType != "SingleService" },
                    onRentalOptionSelected = onRentalOptionSelected
                )
            }
            Spacer(modifier = Modifier.padding(20.dp))
            Text(
                text = stringResource(R.string.cost),
                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                .padding(20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val borderColor: Color
                val priceText: String
                val textColor: Color

                when (selectedOption) {
                    RentalNumberOption.SINGLE_SERVICE -> {
                        if (userSelectedOption && serviceText.isNotEmpty()) {
                            borderColor = MaterialTheme.colors.primary
                            priceText = "$priceInCredits credits"
                            textColor = MaterialTheme.colors.TextColor
                        } else {
                            borderColor = Red
                            priceText = "Please select a service"
                            textColor = Red
                        }
                    }
                    else -> {
                        borderColor = MaterialTheme.colors.primary
                        priceText = "$priceInCredits credits"
                        textColor = MaterialTheme.colors.TextColor
                    }
                }

                Card(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp)),
                    backgroundColor = MaterialTheme.colors.backgroundColor
                )
                {
                    Row(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when (loadingPrice) {
                            LoadingState.LOADING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = MaterialTheme.colors.ButtonColor,
                                    strokeWidth = 3.dp
                                )
                            }
                            LoadingState.ERROR -> {
                                Text(
                                    text = stringResource(R.string.error_loading_price),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = MaterialTheme.typography.h6.fontSize,
                                    color = Red
                                )
                            }
                            else -> {
                                Text(
                                    text = priceText,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = MaterialTheme.typography.h6.fontSize,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.padding(20.dp))
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
                Text(
                    text = stringResource(R.string.rental_48_hours_notice),
                    color = MaterialTheme.colors.primary,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(5.dp)
                )
            }
            Spacer(modifier = Modifier.padding(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
//                .padding(20.dp),
                , horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor),
                    enabled = orderRentalNumberState != LoadingState.LOADING
                            && mainViewModel.gotRentalPrice.value
                            && superUserCheckingBalanceState != LoadingState.ERROR
                            && superUserCheckingBalanceState != LoadingState.LOADING
                            && serviceDecisionText.isNotEmpty(),
                    onClick = {
                        if (dollarToCreditForPurchasingNumbers(
                                superUserBalance,
                                mainViewModel
                            ) > priceInCredits
                        ) { // If the superUser has balance we can let the users order.
                            if (userBalance >= priceInCredits) { // If the user has enough balance to buy we proceed
                                proceedBuying() // function callback done in higher function because we need the serviceID and the duration
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
                    }
                ) {
                    if (orderRentalNumberState == LoadingState.LOADING) {
                        CircularProgressIndicator(color = MaterialTheme.colors.ProgressIndicatorColor)
                    } else {
                        Text(
                            text = stringResource(R.string.proceed),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RentalServicesDropDownMenu(
    label: String,
    optionsList: List<RentalNumberServiceState>,
    onTextChange: (String) -> Unit,
    onOptionSelected: (RentalNumberServiceState) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf("") }

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        OutlinedTextField(
            value = selectedOptionText,
            onValueChange = {
                selectedOptionText = it
                onTextChange(it)
                expanded = true
            },
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable {
                        expanded = !expanded
                    }
                )
            }
        )
        // filter options based on text field value
        val filteringOptions =
            optionsList.filter { it.name.contains(selectedOptionText, ignoreCase = true) }
        if (filteringOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                },
                modifier = Modifier.background(MaterialTheme.colors.CardColor)
//                modifier = Modifier
//                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
//                    .requiredSizeIn(maxHeight = 500.dp)
            ) {
                filteringOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            selectedOptionText = selectionOption.name
                            expanded = false
                            onOptionSelected(selectionOption)
                        }
                    ) {
                        Text(text = selectionOption.name)
                    }
                }
            }
        }
    }
}

@Composable
fun RentalOptionsDropDownMenu(
    label: String,
    optionsList: List<RentalOptionsData>,
    onRentalOptionSelected: (RentalOptionsData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val focusManager = LocalFocusManager.current

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Box {
        OutlinedTextField(
            readOnly = true,
            value = selectedText,
            onValueChange = {
                selectedText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textFieldSize = coordinates.size.toSize()
                }
                .onFocusChanged {
                    expanded = it.isFocused
                },
            label = { Text(label) },
            trailingIcon = {
                Icon(icon, "contentDescription",
                    Modifier.clickable {
                        expanded = !expanded
                    })
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                .requiredSizeIn(maxHeight = 500.dp)
                .background(MaterialTheme.colors.CardColor)
        ) {
            optionsList.forEach { item ->
                DropdownMenuItem(onClick = {
                    selectedText = item.duration
                    onRentalOptionSelected(item)
                    expanded = false
                    focusManager.clearFocus()
                }) {
                    Text(text = item.duration)
                }
            }
        }
    }
}