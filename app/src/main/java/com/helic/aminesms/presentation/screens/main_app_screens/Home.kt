package com.helic.aminesms.presentation.screens.main_app_screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun Home(
    navController: NavController,
    mainViewModel: MainViewModel,
    snackbar: (String, SnackbarDuration) -> Unit
) {
    val context = LocalContext.current

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        mainViewModel.getListOfTempNumbersFromFirebase(context = context, snackbar = snackbar)
        mainViewModel.getListOfRentalNumbersFromFirebase(context = context, snackbar = snackbar)
        mainViewModel.getLiveRentalNumbersList(snackbar = snackbar)
        mainViewModel.getPendingRentalNumbersList(snackbar = snackbar)
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeTopAppBar {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            }
        },

        drawerBackgroundColor = Red,
//        drawerScrimColor = Color.Transparent,
        drawerContent = {
            Profile(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = snackbar
            )
        }
    ) {
        Content(
            navController = navController,
            mainViewModel = mainViewModel
        )
    }
}

@Composable
fun HomeTopAppBar(openDrawer: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { openDrawer() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Navigation Drawer Icon"
                )
            }
        }, title = {
            Text(text = stringResource(R.string.home))
        },
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun Content(navController: NavController, mainViewModel: MainViewModel) {
    var screenSize by remember { mutableStateOf(Size.Zero) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .onGloballyPositioned { coordinates ->
                screenSize = coordinates.size.toSize()
            },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Card(modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(5.dp)
            )
            .height(with(LocalDensity.current) { screenSize.height.toDp() / 2 - 5.dp })
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                navController.navigate(MainAppScreens.TempNumbersMessages.route) {
                    launchSingleTop = true
                }
            }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Temporary numbers",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.h5.fontSize
                )
                Text(
                    text = "Purchased numbers: ${mainViewModel.orderedTempNumbersList.collectAsState().value.size}",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.h6.fontSize
                )
            }
        }
        Card(modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(5.dp)
            )
            .height(with(LocalDensity.current) { screenSize.height.toDp() / 2 - 5.dp })
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                    launchSingleTop = true
                }
            }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Rental numbers",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.h5.fontSize
                )
                Text(
                    text = "Purchased numbers: ${mainViewModel.orderedRentalNumbers.collectAsState().value.size}",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.h6.fontSize
                )
                Text(
                    text = "Live numbers: ${mainViewModel.listOfLiveRentalNumbers.value.size}",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize
                )
                Text(
                    text = "Pending numbers: ${mainViewModel.listOfPendingRentalNumbers.value.size}",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize
                )
            }
        }
    }
}