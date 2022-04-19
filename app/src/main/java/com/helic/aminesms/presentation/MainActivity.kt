package com.helic.aminesms.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Scaffold
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.nav_graph.RootNavGraph
import com.helic.aminesms.presentation.ui.theme.AmineSMSTheme
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
            AmineSMSTheme() {
                navController = rememberNavController()
                val appState: Msnackbar = rememberSnackbarState()
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