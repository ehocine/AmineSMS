package com.helic.aminesms.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Scaffold
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.nav_graph.RootNavGraph
import com.helic.aminesms.presentation.ui.theme.AmineSMSTheme
import com.helic.aminesms.utils.Constants.DARK_THEME
import com.helic.aminesms.utils.Msnackbar
import com.helic.aminesms.utils.rememberSnackbarState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    lateinit var navController: NavHostController
    private val mainViewModel: MainViewModel by viewModels()

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmineSMSTheme(darkTheme = DARK_THEME.value) {
                val defaultTheme = isSystemInDarkTheme()
                navController = rememberNavController()
                val appState: Msnackbar = rememberSnackbarState()
                lifecycleScope.launch {
                    mainViewModel.themeValue.collect {
                        DARK_THEME.value = it ?: defaultTheme
                    }
                    Log.d("Tag", "Dar from act ${DARK_THEME.value}")
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