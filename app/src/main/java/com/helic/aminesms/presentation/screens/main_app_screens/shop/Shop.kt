package com.helic.aminesms.presentation.screens.main_app_screens.shop

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
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
import com.helic.aminesms.presentation.ui.theme.*
import com.helic.aminesms.utils.AddingBalanceState
import com.helic.aminesms.utils.Constants.SHOP_LIST
import com.helic.aminesms.utils.Constants.SKU_LIST
import com.helic.aminesms.utils.LoadingState
import com.helic.aminesms.utils.addBalance
import com.helic.aminesms.utils.dollarToCreditForPurchasingCurrency
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QPermission
import com.qonversion.android.sdk.dto.products.QProduct

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
        modifier = Modifier.height(TOP_APP_BAR_HEIGHT),
        navigationIcon = {
            IconButton(onClick = {
                navController.navigate(MainAppScreens.Home.route) {
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
            Text(text = stringResource(R.string.buy_balance), color = MaterialTheme.colors.topAppBarContentColor)
        },
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.topAppBarBackgroundColor
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
    var selectedOption by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
//            .padding(10.dp)
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .background(MaterialTheme.colors.backgroundColor),
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
                selectedOption = SKU_LIST.filter { it.value == selected }.keys.first()
                Log.d("Product", "Selected element : $selectedOption")
            }, mainViewModel = mainViewModel)
        }
        Spacer(modifier = Modifier.padding(10.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor),
            enabled = selected != 0 && state != LoadingState.ERROR && state != LoadingState.LOADING,
            onClick = {

                mainViewModel.products.forEach {
                    Log.d("Product", it.storeID.toString())
                }
                Log.d(
                    "Product",
                    mainViewModel.products.first { it.storeID == selectedOption }.toString()
                )
                if (superUserBalance > 0) { // If the superUser has balance we can let the users buy their own.
                    purchase(
                        activity = context as Activity,
                        product = mainViewModel.products.first { it.storeID == selectedOption },
                        mainViewModel = mainViewModel,
                        chosenOption = selected,
                        showSnackbar = showSnackbar
                    )

//                    proceedToBuy(
//                        context = context,
//                        chosenOption = selected,
//                        mainViewModel = mainViewModel,
//                        showSnackbar = showSnackbar
//                    )
                } else {
                    showSnackbar(context.getString(R.string.cant_purchase), SnackbarDuration.Short)
                }
            }
        ) {
            Text(
                text = stringResource(R.string.proceed),
                fontSize = 20.sp,
                color = MaterialTheme.colors.ButtonTextColor
            )

        }
    }
}

fun proceedToAddBalance(
    context: Context,
    chosenOption: Int,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    val addBalanceAmount =
        dollarToCreditForPurchasingCurrency(chosenOption.toDouble(), mainViewModel = mainViewModel)
    Log.d("Tag", "You chose to buy $chosenOption option")
    addBalance(
        context = context,
        snackbar = showSnackbar,
        currentBalance = mainViewModel.userBalance.value,
        amount = addBalanceAmount,
        addingBalanceState = AddingBalanceState.ADD
    )
}

private fun purchase(
    activity: Activity,
    product: QProduct,
    mainViewModel: MainViewModel,
    chosenOption: Int,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    Qonversion.purchase(activity, product, callback = object :
        QonversionPermissionsCallback {
        override fun onSuccess(permissions: Map<String, QPermission>) {
            Log.d("Tag", "Purchase succeeded")
            proceedToAddBalance(
                context = activity,
                chosenOption = chosenOption,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }

        override fun onError(error: QonversionError) {
            Log.d("Tag", error.description)
            showSnackbar(error.description, SnackbarDuration.Short)
        }
    })
}


