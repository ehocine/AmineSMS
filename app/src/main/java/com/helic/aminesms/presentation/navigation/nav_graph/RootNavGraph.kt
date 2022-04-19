package com.helic.aminesms.presentation.navigation.nav_graph

import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.utils.Constants.AUTHENTICATION_ROUTE
import com.helic.aminesms.utils.Constants.MAIN_SCREEN_ROUTE
import com.helic.aminesms.utils.Constants.ROOT_ROUTE
import com.helic.aminesms.utils.userLoggedIn


@Composable
fun RootNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {

    NavHost(
        navController = navController,
        startDestination = if (!userLoggedIn()) AUTHENTICATION_ROUTE else MAIN_SCREEN_ROUTE,
        route = ROOT_ROUTE
    ) {
        authNavGraph(navController = navController, showSnackbar = showSnackbar)
        mainScreenNavGraph(navController = navController, mainViewModel, showSnackbar = showSnackbar)
    }
}