package com.helic.aminesms.presentation.navigation.nav_graph

import androidx.compose.material.SnackbarDuration
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.AuthenticationScreens
import com.helic.aminesms.presentation.screens.login_signup_screens.ForgetPassword
import com.helic.aminesms.presentation.screens.login_signup_screens.LoginPage
import com.helic.aminesms.presentation.screens.login_signup_screens.RegisterPage
import com.helic.aminesms.utils.Constants.AUTHENTICATION_ROUTE


fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {

    navigation(
        startDestination = AuthenticationScreens.Login.route,
        route = AUTHENTICATION_ROUTE
    ) {
        composable(route = AuthenticationScreens.Login.route) {
            LoginPage(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }
        composable(route = AuthenticationScreens.Register.route) {
            RegisterPage(
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }
        composable(route = AuthenticationScreens.ForgetPassword.route) {
            ForgetPassword(navController = navController, showSnackbar = showSnackbar)
        }
    }
}