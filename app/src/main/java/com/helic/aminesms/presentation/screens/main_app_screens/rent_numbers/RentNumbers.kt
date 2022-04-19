package com.helic.aminesms.presentation.screens.main_app_screens.rent_numbers

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.ErrorLoadingResults
import com.helic.aminesms.utils.LoadingList
import com.helic.aminesms.utils.LoadingState
import com.helic.aminesms.utils.RentalNumberOption

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RentNumbers(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    val rentalServiceStateListResponse = mainViewModel.rentalServiceStateListResponse.value
    val listOfServicesNames = mutableListOf<String>()
    rentalServiceStateListResponse.forEach {
        listOfServicesNames.add(it.name)
    }

    val isRefreshing by mainViewModel.isRefreshing.collectAsState()

    LaunchedEffect(true) {
        mainViewModel.getRentalServiceStateList(snackbar = snackbar)
    }
    val state by mainViewModel.rentalServiceLoadingStateOfViewModel.collectAsState()

    Scaffold(topBar = {
        RentNumberTopAppBar(navController = navController)
    }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { mainViewModel.refreshServiceStateList(snackbar = snackbar) }
        ) {
            when (state) {
                LoadingState.LOADING -> LoadingList()
                LoadingState.ERROR -> ErrorLoadingResults()
                else -> {
                    Content(
                        serviceStateList = listOfServicesNames,
                        mainViewModel = mainViewModel,
                        onTextChange = { newText ->
                            mainViewModel.searchTextState.value = newText

                        },
                        onOptionSelected = {}
                    )
                }
            }
        }
    }
}

@Composable
fun RentNumberTopAppBar(
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
            Text(text = "Rent a Number")
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun Content(
    serviceStateList: List<String>,
    mainViewModel: MainViewModel,
    onTextChange: (String) -> Unit,
    onOptionSelected: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf(RentalNumberOption.WHOLE_LINE) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row() {
            RadioButton(
                selected = selectedOption == RentalNumberOption.WHOLE_LINE,
                onClick = { selectedOption = RentalNumberOption.WHOLE_LINE },
                colors = RadioButtonDefaults.colors(MaterialTheme.colors.primary)
            )
            Spacer(modifier = Modifier.padding(15.dp))
            Text(text = "Whole Line")
            Spacer(modifier = Modifier.padding(25.dp))
            RadioButton(
                selected = selectedOption == RentalNumberOption.SINGLE_SERVICE,
                onClick = { selectedOption = RentalNumberOption.SINGLE_SERVICE },
                colors = RadioButtonDefaults.colors(MaterialTheme.colors.primary)
            )
            Spacer(modifier = Modifier.padding(15.dp))
            Text(text = "Single Service")
        }
        if (selectedOption == RentalNumberOption.SINGLE_SERVICE) {
            DropDownMenu(
                label = "Search Service State",
                optionsList = serviceStateList,
                onTextChange = onTextChange,
                onOptionSelected = onOptionSelected,
                mainViewModel = mainViewModel
            )
        }
        Text(text = "Cost")
    }
}


@Composable
fun DropDownMenu(
    label: String,
    optionsList: List<String>,
    mainViewModel: MainViewModel,
    onTextChange: (String) -> Unit,
    onOptionSelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val searchText = mainViewModel.searchTextState.value

    val filteredList: List<String> = if (searchText.isEmpty()) {
        optionsList
    } else {
        optionsList.filter { serviceState ->
            serviceState.contains(searchText)
        }
    }

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(20.dp)) {
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
                    Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            filteredList.forEach { item ->
                DropdownMenuItem(onClick = {
                    selectedText = item
                    onOptionSelected(item)
                    expanded = false
                }) {
                    Text(text = item)
                }
            }
        }
    }
}


