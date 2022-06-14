package com.helic.aminesms.presentation.screens.main_app_screens.order_temp_numbers

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.TOP_APP_BAR_HEIGHT
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.ErrorLoadingResults
import com.helic.aminesms.utils.LoadingList
import com.helic.aminesms.utils.LoadingState
import com.helic.aminesms.utils.SearchAppBarState

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OrderTempNumbers(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    val serviceStateListResponse = mainViewModel.serviceStateListResponse.value

    val searchAppBarState: SearchAppBarState by mainViewModel.searchAppBarState
    val searchTextState: String by mainViewModel.searchTextState

    val isRefreshing by mainViewModel.isRefreshing.collectAsState()

    LaunchedEffect(true) {
        mainViewModel.getServiceStateList(snackbar = snackbar)
    }
    val state by mainViewModel.loadingStateOfViewModel.collectAsState()

    Scaffold(topBar = {
        when (searchAppBarState) {
            SearchAppBarState.CLOSED -> {
                OrderNumberTopAppBar(navController = navController,
                    onSearchClicked = {
                        mainViewModel.searchAppBarState.value = SearchAppBarState.OPENED
                    })
            }
            else -> {
                SearchAppBar(
                    text = searchTextState,
                    onTextChange = { newText ->
                        mainViewModel.searchTextState.value = newText
                    },
                    onCloseClicked = {
                        mainViewModel.searchAppBarState.value =
                            SearchAppBarState.CLOSED
                        mainViewModel.searchTextState.value = ""

                    }
                )
            }
        }
    }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { mainViewModel.refreshServiceStateList(snackbar = snackbar) }
        ) {
            when (state) {
                LoadingState.LOADING -> LoadingList()
                LoadingState.ERROR -> ErrorLoadingResults()
                else -> {
                    DisplayServiceStateList(
                        navController = navController,
                        listOfServiceState = serviceStateListResponse,
                        mainViewModel = mainViewModel,
                        snackbar = snackbar
                    )
                }
            }
        }
    }
}

@Composable
fun OrderNumberTopAppBar(
    navController: NavController,
    onSearchClicked: () -> Unit
) {
    TopAppBar(
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
            Text(text = "Order a temporary number")
        },
        actions = {
            IconButton(onClick = { onSearchClicked() }) {
                Icon(
                    imageVector = Icons.Default.Search, contentDescription = "Search Button",
                    tint = MaterialTheme.colors.topAppBarContentColor
                )
            }
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun SearchAppBar(
    text: String,
    onTextChange: (String) -> Unit,
    onCloseClicked: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(TOP_APP_BAR_HEIGHT),
        elevation = AppBarDefaults.TopAppBarElevation,
        color = MaterialTheme.colors.topAppBarBackgroundColor
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = text,
            onValueChange = {
                onTextChange(it)
            },
            placeholder = {
                Text(
                    modifier = Modifier
                        .alpha(ContentAlpha.medium),
                    text = stringResource(R.string.search),
                    color = Color.White
                )
            },
            textStyle = TextStyle(
                color = MaterialTheme.colors.topAppBarContentColor,
                fontSize = MaterialTheme.typography.subtitle1.fontSize
            ),
            singleLine = true,
            leadingIcon = {
                IconButton(
                    modifier = Modifier
                        .alpha(ContentAlpha.disabled),
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search Icon",
                        tint = MaterialTheme.colors.topAppBarContentColor
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (text.isNotEmpty()) {
                            onTextChange("")
                        } else {
                            onCloseClicked()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Icon",
                        tint = MaterialTheme.colors.topAppBarContentColor
                    )
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = MaterialTheme.colors.topAppBarContentColor,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                backgroundColor = Color.Transparent
            )
        )
    }
}


