package com.helic.aminesms.presentation.screens.main_app_screens.rental_numbers


import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.*

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun RentalNumbers(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(true) {
        mainViewModel.getSuperUserBalance(snackbar = snackbar)
        mainViewModel.getRentalServiceStateList(snackbar = snackbar)
        mainViewModel.getRentalNumberOptions(snackbar = snackbar)
    }
    val superUserCheckingBalanceState by mainViewModel.checkingSuperUserBalanceLoadingState.collectAsState()
    val superUserBalance = mainViewModel.superUserBalance.value
    mainViewModel.price.value = 0.0

    val listOfRentalServices by mainViewModel.rentalServiceStateList
    val optionsList by mainViewModel.availableRentalOptions

    val isRefreshing by mainViewModel.isRefreshing.collectAsState()

    val price by mainViewModel.price

    var selectedOption by remember { mutableStateOf(RentalNumberOption.WHOLE_LINE) }
    var serviceId by remember { mutableStateOf("") }

    Scaffold(topBar = {
        RentalNumberTopAppBar(navController = navController)
    }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { mainViewModel.refreshRentalServiceStateList(snackbar = snackbar) }
        ) {
            when (superUserCheckingBalanceState) {
                LoadingState.LOADING -> LoadingList()
                LoadingState.ERROR -> ErrorLoadingResults()
                else -> {
                    Content(
                        context = context,
                        serviceStateList = listOfRentalServices,
                        optionsList = optionsList,
                        mainViewModel = mainViewModel,
                        onTextChange = { newText ->
                            mainViewModel.searchTextState.value = newText
                        },
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
                        }, onRentalNumberOptionChosen = {
                            selectedOption = it
                        },
                        price = dollarToCreditForPurchasingNumbers(price),
                        superUserBalance = superUserBalance,
                        superUserCheckingBalanceState = superUserCheckingBalanceState,
                        snackbar = snackbar
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
        title = {
            Text(text = "Rent a number")
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

//TODO Loading services list takes so long, this must be fixed
@Composable
fun Content(
    context: Context,
    serviceStateList: List<RentalNumberServiceState>,
    optionsList: List<RentalOptionsData>,
    mainViewModel: MainViewModel,
    onTextChange: (String) -> Unit,
    onRentalOptionSelected: (RentalOptionsData) -> Unit,
    onRentalNumberOptionChosen: (RentalNumberOption) -> Unit,
    price: Double,
    superUserBalance: Double,
    superUserCheckingBalanceState: LoadingState,
    snackbar: (String, SnackbarDuration) -> Unit,
) {
    var selectedOption by remember { mutableStateOf(RentalNumberOption.WHOLE_LINE) }
    var userSelectedOption by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedOption == RentalNumberOption.WHOLE_LINE,
                onClick = {
                    selectedOption = RentalNumberOption.WHOLE_LINE
                    onRentalNumberOptionChosen(selectedOption)
                    mainViewModel.gotRentalPrice.value = false
                },
                colors = RadioButtonDefaults.colors(MaterialTheme.colors.primary)
            )
            Text(text = "Whole Line")
            Spacer(modifier = Modifier.padding(25.dp))
            RadioButton(
                selected = selectedOption == RentalNumberOption.SINGLE_SERVICE,
                onClick = {
                    selectedOption = RentalNumberOption.SINGLE_SERVICE
                    onRentalNumberOptionChosen(selectedOption)
                    mainViewModel.gotRentalPrice.value = false
                    userSelectedOption = false
                    mainViewModel.searchTextState.value = "" // To re-initialize the value on switching
                },
                colors = RadioButtonDefaults.colors(MaterialTheme.colors.primary)
            )
            Text(text = "Single Service")
        }
        if (selectedOption == RentalNumberOption.SINGLE_SERVICE) {

            mainViewModel.selectedRentalService.value = RentalNumberServiceState()
            RentalServicesDropDownMenu(
                label = "Search Service State",
                optionsList = serviceStateList,
                onTextChange = onTextChange,
                onOptionSelected = {
                    mainViewModel.selectedRentalService.value =
                        it // We did it here because we need to check if a service was selected
                    userSelectedOption = true
                },
                mainViewModel = mainViewModel
            )
            if (userSelectedOption) { // if the user didn't select a service we won't show the period options
                RentalOptionsDropDownMenu(
                    label = "Select a period",
                    optionsList = optionsList.filter { it.rentalType == "SingleService" },
                    onRentalOptionSelected = onRentalOptionSelected
                )
            }
        } else {
            RentalOptionsDropDownMenu(
                label = "Select a period",
                optionsList = optionsList.filter { it.rentalType != "SingleService" },
                onRentalOptionSelected = onRentalOptionSelected
            )
        }
        Text(
            text = "Cost",
            fontSize = MaterialTheme.typography.subtitle1.fontSize,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (selectedOption == RentalNumberOption.SINGLE_SERVICE && !userSelectedOption) {
                Text(
                    text = "Please select a service",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    color = Red
                )
            } else {
                Text(
                    text = "$price credits",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.h6.fontSize,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor),
                enabled = mainViewModel.gotRentalPrice.value && superUserCheckingBalanceState != LoadingState.ERROR && superUserCheckingBalanceState != LoadingState.LOADING,
                onClick = {
                    if (superUserBalance > price) { // If the superUser has balance we can let the users order.
                        //TODO Add a condition to check if the user has enough balance to buy
                        //TODO Add a function to proceed with buying

                    } else {
                        snackbar(context.getString(R.string.cant_purchase), SnackbarDuration.Short)
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.proceed),
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun RentalServicesDropDownMenu(
    label: String,
    optionsList: List<RentalNumberServiceState>,
    mainViewModel: MainViewModel,
    onTextChange: (String) -> Unit,
    onOptionSelected: (RentalNumberServiceState) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val searchText = mainViewModel.searchTextState.value

    val filteredList: List<RentalNumberServiceState> = if (searchText.isEmpty()) {
        optionsList
    } else {
        optionsList.filter { serviceState ->
            serviceState.name.contains(searchText)
        }
    }

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(20.dp)) {
        Box {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {
                    selectedText = it
                    onTextChange(it)
                    expanded = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        //This value is used to assign to the DropDown the same width
                        textFieldSize = coordinates.size.toSize()
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
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    .requiredSizeIn(maxHeight = 500.dp)
            ) {
                filteredList.forEach { item ->
                    DropdownMenuItem(onClick = {
                        selectedText = item.name
                        onOptionSelected(item)
                        expanded = false
                    }) {
                        Text(text = item.name)
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


    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(20.dp)) {
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
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    .requiredSizeIn(maxHeight = 500.dp)
            ) {
                optionsList.forEach { item ->
                    DropdownMenuItem(onClick = {
                        selectedText = item.duration
                        onRentalOptionSelected(item)
                        expanded = false
                    }) {
                        Text(text = item.duration)
                    }
                }
            }
        }
    }
}
//
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun RentalOptionsDropDownMenu(
//    label: String,
//    optionsList: List<RentalOptionsData>,
//    onRentalOptionSelected: (RentalOptionsData) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//    var selectedText by remember { mutableStateOf("") }
//    var textFieldSize by remember { mutableStateOf(Size.Zero) }
//
//    Column(Modifier.padding(20.dp)) {
//        Box {
//            ExposedDropdownMenuBox(
//                expanded = expanded,
//                onExpandedChange = {
//                    expanded = !expanded
//                }
//            ) {
//                TextField(
//                    readOnly = true,
//                    value = selectedText,
//                    onValueChange = {
//                        selectedText = it
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .onGloballyPositioned { coordinates ->
//                            //This value is used to assign to the DropDown the same width
//                            textFieldSize = coordinates.size.toSize()
//                        },
//                    label = { Text(label) },
//                    trailingIcon = {
//                        ExposedDropdownMenuDefaults.TrailingIcon(
//                            expanded = expanded
//                        )
//                    },
//                    colors = ExposedDropdownMenuDefaults.textFieldColors()
//                )
//                ExposedDropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = {
//                        expanded = false
//                    }
//                ) {
//                    optionsList.forEach { selectionOption ->
//                        DropdownMenuItem(
//                            onClick = {
//                                selectedText = selectionOption.duration
//                                onRentalOptionSelected(selectionOption)
//                                expanded = false
//                            }
//                        ) {
//                            Text(text = selectionOption.duration)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}