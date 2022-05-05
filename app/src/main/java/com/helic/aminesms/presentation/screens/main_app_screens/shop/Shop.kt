package com.helic.aminesms.presentation.screens.main_app_screens.shop

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.helic.aminesms.R
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.topAppBarBackgroundColor
import com.helic.aminesms.presentation.ui.theme.topAppBarContentColor
import com.helic.aminesms.utils.Constants.SHOP_LIST
import com.helic.aminesms.utils.LoadingState

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun Shop(
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        mainViewModel.getSuperUserBalance(snackbar = showSnackbar)
    }
    val state by mainViewModel.checkingSuperUserBalanceLoadingState.collectAsState()

    val superUserBalance = mainViewModel.superUserBalance.value

    Scaffold(topBar = {
        ShopTopAppBar(navController = navController)
    }) {
        MainShopScreen(
            context = context,
            mainViewModel = mainViewModel,
            showSnackbar = showSnackbar,
            superUserBalance = superUserBalance,
            state = state
        )
    }
}

@Composable
fun ShopTopAppBar(navController: NavController) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.TempNumberMessages.route) {
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
            Text(text = stringResource(R.string.buy_balance))
        }, backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
    )
}

@Composable
fun MainShopScreen(
    context: Context,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    superUserBalance: Double,
    state: LoadingState
) {
    var selected by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize()
            .verticalScroll(state = scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Buy Credits",
            fontSize = MaterialTheme.typography.h5.fontSize,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.padding(20.dp))
        SHOP_LIST.forEach { item ->
            ShopItem(selected = selected, title = item, onClick = {
                selected = item
            })
        }
        Spacer(modifier = Modifier.padding(10.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor),
            enabled = selected != 0 && state != LoadingState.ERROR && state != LoadingState.LOADING,
            onClick = {
                if (superUserBalance > 0) { // If the superUser has balance we can let the users buy their own.
                    mainViewModel.proceedToBuy(
                        chosenOption = selected,
                        showSnackbar = showSnackbar
                    )
                } else {
                    showSnackbar(context.getString(R.string.cant_purchase), SnackbarDuration.Short)
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


