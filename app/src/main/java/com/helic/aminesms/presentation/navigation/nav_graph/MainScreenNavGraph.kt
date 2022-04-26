package com.helic.aminesms.presentation.navigation.nav_graph

import androidx.compose.material.SnackbarDuration
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.screens.main_app_screens.Profile
import com.helic.aminesms.presentation.screens.main_app_screens.messages.main_messages_screen.Messages
import com.helic.aminesms.presentation.screens.main_app_screens.messages.message_details.MessageDetails
import com.helic.aminesms.presentation.screens.main_app_screens.order_temp_numbers.OrderNumberOptions
import com.helic.aminesms.presentation.screens.main_app_screens.order_temp_numbers.OrderNumbers
import com.helic.aminesms.presentation.screens.main_app_screens.rental_numbers.RentalNumberOptions
import com.helic.aminesms.presentation.screens.main_app_screens.rental_numbers.RentalNumbers
import com.helic.aminesms.presentation.screens.main_app_screens.shop.Shop
import com.helic.aminesms.utils.Constants.MAIN_SCREEN_ROUTE


fun NavGraphBuilder.mainScreenNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit,

    ) {
    navigation(
        startDestination = MainAppScreens.Messages.route,
        route = MAIN_SCREEN_ROUTE
    ) {

        composable(route = MainAppScreens.Messages.route) {
            Messages(
                navController = navController,
                mainViewModel = mainViewModel,
                snackbar = showSnackbar
            )
        }
        composable(route = MainAppScreens.Profile.route) {
            Profile(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }
        composable(
            route = MainAppScreens.MessageDetails.route
        ) {
            MessageDetails(
                navController = navController,
                showSnackbar = showSnackbar,
                mainViewModel = mainViewModel
            )
        }
        composable(route = MainAppScreens.Shop.route) {
            Shop(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }
        composable(route = MainAppScreens.OrderNumbers.route) {
            OrderNumbers(
                navController = navController,
                mainViewModel = mainViewModel,
                snackbar = showSnackbar
            )
        }
        composable(route = MainAppScreens.OrderNumberOptions.route) {
            OrderNumberOptions(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }

        composable(route = MainAppScreens.RentalNumbers.route) {
            RentalNumbers(
                navController = navController,
                mainViewModel = mainViewModel,
                snackbar = showSnackbar
            )
        }
        composable(route = MainAppScreens.RentalNumbersOptions.route) {
            RentalNumberOptions(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }
    }
}