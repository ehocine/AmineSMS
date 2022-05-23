package com.helic.aminesms.presentation.screens.main_app_screens

import android.annotation.SuppressLint
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.NoticeColor
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.utils.WindowInfo
import com.helic.aminesms.utils.rememberWindowInfo
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
fun ContentOnCompactScreen(navController: NavController, mainViewModel: MainViewModel) {
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
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(15.dp)
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
                Card(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.NoticeColor,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    Text(
                        buildAnnotatedString {
                            append("You can reuse temporary numbers for a ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("FEW MINUTES")
                            }
                            append(
                                " after receiving a code.\n" +
                                        "This system cannot return a number if it is not shown here as reusable.\n" +
                                        "If you need guaranteed reuse, buy a rental number."
                            )
                        },
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colors.NoticeColor,
                        fontSize = MaterialTheme.typography.subtitle1.fontSize
                    )
                }
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
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(15.dp)
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
                Card(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.NoticeColor,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    Text(
                        text = "You must activate your rental before it can be used, every time. " +
                                "This request takes a few seconds. The full activation process can take up to 5 minutes. After activation, " +
                                "we will deliver all of your messages, if you have any.",
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colors.NoticeColor,
                        fontSize = MaterialTheme.typography.subtitle1.fontSize
                    )
                }
            }
        }
    }
}

@Composable
fun ContentOnLargeScreen(navController: NavController, mainViewModel: MainViewModel) {
    var screenSize by remember { mutableStateOf(Size.Zero) }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .onGloballyPositioned { coordinates ->
                screenSize = coordinates.size.toSize()
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Card(modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(5.dp)
            )
            .width(with(LocalDensity.current) { screenSize.width.toDp() / 2 - 5.dp })
            .fillMaxHeight()
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                navController.navigate(MainAppScreens.TempNumbersMessages.route) {
                    launchSingleTop = true
                }
            }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(15.dp)
            ) {
                Text(
                    text = stringResource(R.string.temporary_numbers),
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.h5.fontSize
                )
                Text(
                    text = "Purchased numbers: ${mainViewModel.orderedTempNumbersList.collectAsState().value.size}",
                    fontWeight = FontWeight.Medium,
                    fontSize = MaterialTheme.typography.h6.fontSize
                )
                Card(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.NoticeColor,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    Text(
                        buildAnnotatedString {
                            append("You can reuse temporary numbers for a ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("FEW MINUTES")
                            }
                            append(
                                " after receiving a code.\n" +
                                        "This system cannot return a number if it is not shown here as reusable.\n" +
                                        "If you need guaranteed reuse, buy a rental number."
                            )
                        },
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colors.NoticeColor,
                        fontSize = MaterialTheme.typography.subtitle1.fontSize
                    )
                }
            }
        }
        Card(modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(5.dp)
            )
            .width(with(LocalDensity.current) { screenSize.width.toDp() / 2 - 5.dp })
            .fillMaxHeight()
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                    launchSingleTop = true
                }
            }) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(15.dp)
            ) {
                Text(
                    text = stringResource(R.string.rental_numbers),
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
                Card(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.NoticeColor,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    Text(
                        text = "You must activate your rental before it can be used, every time. " +
                                "This request takes a few seconds. The full activation process can take up to 5 minutes. After activation, " +
                                "we will deliver all of your messages, if you have any.",
                        modifier = Modifier.padding(10.dp),
                        color = MaterialTheme.colors.NoticeColor,
                        fontSize = MaterialTheme.typography.subtitle1.fontSize
                    )
                }
            }
        }
    }
}