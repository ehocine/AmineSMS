package com.helic.aminesms.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.nav_graph.RootNavGraph
import com.helic.aminesms.presentation.ui.theme.AmineSMSTheme
import com.helic.aminesms.presentation.ui.theme.darkBackgroundColor
import com.helic.aminesms.presentation.ui.theme.lightBackgroundColor
import com.helic.aminesms.utils.Constants.DARK_THEME
import com.helic.aminesms.utils.Msnackbar
import com.helic.aminesms.utils.rememberSnackbarState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    lateinit var navController: NavHostController
    private val mainViewModel: MainViewModel by viewModels()

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmineSMSTheme(darkTheme = DARK_THEME.value) {
                val systemUiController = rememberSystemUiController()

                val defaultTheme = isSystemInDarkTheme() //device's default theme
                navController = rememberNavController()
                val appState: Msnackbar = rememberSnackbarState()

                // we check the user choice of theme from data store otherwise we put the device's default theme
                LaunchedEffect(key1 = true) {
                    mainViewModel.themeValue.collect {
                        DARK_THEME.value = it ?: defaultTheme
                    }
                }
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = if (DARK_THEME.value) darkBackgroundColor else lightBackgroundColor
                    )
                }
                Scaffold(
                    scaffoldState = appState.scaffoldState
                ) {
                    RootNavGraph(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        showSnackbar = { message, duration ->
                            appState.showSnackbar(message = message, duration = duration)
                        })
                }
            }
        }
    }
}