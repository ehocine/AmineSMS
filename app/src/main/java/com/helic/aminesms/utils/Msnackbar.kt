package com.helic.aminesms.utils

import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Msnackbar(
    val scaffoldState: ScaffoldState,
    val snackbarScope: CoroutineScope
) {
    fun showSnackbar(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        snackbarScope.launch {
            scaffoldState.snackbarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }
}

@Composable
fun rememberSnackbarState(
    scaffoldState: ScaffoldState = rememberScaffoldState(
        snackbarHostState = remember {
            SnackbarHostState()
        }
    ),
    snackbarScope: CoroutineScope = rememberCoroutineScope()
) = remember(scaffoldState, snackbarScope) {
    Msnackbar(
        scaffoldState = scaffoldState,
        snackbarScope = snackbarScope
    )
}

