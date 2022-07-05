package com.helic.aminesms.presentation.screens.main_app_screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
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
import com.helic.aminesms.presentation.ui.theme.*
import com.helic.aminesms.utils.Constants.TIME_BETWEEN_AUTO_REFRESH
import com.helic.aminesms.utils.WindowInfo
import com.helic.aminesms.utils.coloredShadow
import com.helic.aminesms.utils.hasInternetConnection
import com.helic.aminesms.utils.rememberWindowInfo
import kotlinx.coroutines.delay
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
        mainViewModel.getListOfTempNumbersFromFirebase(
            context = context,
            snackbar = snackbar
        ) // Size counter changes dynamically
        mainViewModel.getListOfRentalNumbersFromFirebase(
            context = context,
            snackbar = snackbar
        ) // Size counter changes dynamically
    }

//This is implemented to auto-refresh the lists every TIME_BETWEEN_AUTO_REFRESH
    if (hasInternetConnection(context = context)) {
        var counter by remember { mutableStateOf(0) }
        LaunchedEffect(key1 = counter) {
            delay(timeMillis = TIME_BETWEEN_AUTO_REFRESH)
            counter += 1
            mainViewModel.getLiveRentalNumbersList(snackbar = snackbar)
            mainViewModel.getPendingRentalNumbersList(snackbar = snackbar)
        }
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
        drawerContent = {
            Profile(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = snackbar
            )
        }
    ) {
        val windowInfo = rememberWindowInfo()
        if (windowInfo.screenWidthInfo is WindowInfo.WindowType.Compact) {
            ContentOnCompactScreen(
                navController = navController,
                mainViewModel = mainViewModel
            )
        } else {
            ContentOnLargeScreen(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
    }
}

@Composable
fun HomeTopAppBar(openDrawer: () -> Unit) {
    TopAppBar(
        modifier = Modifier.height(TOP_APP_BAR_HEIGHT),
        navigationIcon = {
            IconButton(onClick = { openDrawer() }) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Navigation Drawer Icon",
                    tint = MaterialTheme.colors.topAppBarContentColor
                )
            }
        }, title = {
            Text(
                text = stringResource(R.string.home),
                color = MaterialTheme.colors.topAppBarContentColor
            )
        },
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun ContentOnCompactScreen(navController: NavController, mainViewModel: MainViewModel) {
    var screenSize by remember { mutableStateOf(Size.Zero) }
    Column(
        modifier = Modifier
            .fillMaxSize()
//            .padding(paddingMedium)
            .onGloballyPositioned { coordinates ->
                screenSize = coordinates.size.toSize()
            }
            .background(MaterialTheme.colors.backgroundColor)
    ) {
        Column(
            Modifier.padding(paddingSmall),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            DashboardCardItem(
                modifier = Modifier
                    .height(with(LocalDensity.current) { screenSize.height.toDp() / 4 })
                    .fillMaxWidth(),
                color = MaterialTheme.colors.HomeCard.copy(alpha = 0.5f),
                onClick = {
                    navController.navigate(MainAppScreens.TempNumbersMessages.route) {
                        launchSingleTop = true
                    }
                },
                title = stringResource(R.string.temporary_numbers),
                description = "Purchased numbers:",
                count = "${mainViewModel.orderedTempNumbersList.collectAsState().value.size}",
            )
            Spacer(modifier = Modifier.padding(paddingMedium))
            DashboardCardItem(
                modifier = Modifier
                    .height(with(LocalDensity.current) { screenSize.height.toDp() / 4 })
                    .fillMaxWidth(),
                color = MaterialTheme.colors.HomeCard.copy(alpha = 0.5f),
                onClick = {
                    navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                        launchSingleTop = true
                    }
                },
                title = stringResource(R.string.rental_numbers),
                description = "Purchased numbers:",
                count = "${mainViewModel.orderedRentalNumbers.collectAsState().value.size}",
            )
        }
    }
}

//Basically used on large screens and landscape screen: Two section in row
@Composable
fun ContentOnLargeScreen(navController: NavController, mainViewModel: MainViewModel) {
    var screenSize by remember { mutableStateOf(Size.Zero) }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                screenSize = coordinates.size.toSize()
            }
            .background(MaterialTheme.colors.backgroundColor)
    ) {
        Row(
            Modifier
                .padding(paddingMedium)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DashboardCardItem(
                modifier = Modifier
                    .width(with(LocalDensity.current) { screenSize.width.toDp() / 2 })
                    .fillMaxHeight(),
                color = MaterialTheme.colors.HomeCard.copy(alpha = 0.5f),
                onClick = {
                    navController.navigate(MainAppScreens.TempNumbersMessages.route) {
                        launchSingleTop = true
                    }
                },
                title = stringResource(R.string.temporary_numbers),
                description = "Purchased numbers:",
                count = "${mainViewModel.orderedTempNumbersList.collectAsState().value.size}",
            )
            Spacer(modifier = Modifier.padding(paddingMedium))
            DashboardCardItem(
                modifier = Modifier
                    .width(with(LocalDensity.current) { screenSize.width.toDp() / 2 })
                    .fillMaxHeight(),
                color = MaterialTheme.colors.HomeCard.copy(alpha = 0.5f),
                onClick = {
                    navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                        launchSingleTop = true
                    }
                },
                title = stringResource(R.string.rental_numbers),
                description = "Purchased numbers:",
                count = "${mainViewModel.orderedRentalNumbers.collectAsState().value.size}",
            )
        }
    }
}

@Composable
fun DashboardCardItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    count: String,
    color: Color,
    onClick: () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(listOf(color.copy(.8F), color), startY = 10F)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingSmall, end = paddingSmall)
            .coloredShadow(
                color,
                alpha = 0.4F,
                borderRadius = paddingXXL,
                shadowRadius = paddingMedium,
                offsetX = paddingNone,
                offsetY = paddingSmall
            )
            .clip(RoundedCornerShape(shapeXL))
            .background(brush = gradientBrush)
            .clickable {
                onClick()
            }
    ) {
        Row(
            Modifier
                .padding(start = paddingXL, end = paddingXL)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(
                        top = paddingSmall,
                        bottom = paddingSmall
                    )
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = title, style = MaterialTheme.typography.h6, color = Color.White)
//                Spacer(modifier = modifier.padding(1.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.White
                )
            }
            Text(
                text = count,
                style = MaterialTheme.typography.h4,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}