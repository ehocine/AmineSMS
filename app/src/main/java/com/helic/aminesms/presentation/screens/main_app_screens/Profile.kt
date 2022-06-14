package com.helic.aminesms.presentation.screens.main_app_screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.helic.aminesms.R
import com.helic.aminesms.data.models.User
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.backgroundColor
import com.helic.aminesms.presentation.ui.theme.fancyFont
import com.helic.aminesms.utils.Constants.AUTHENTICATION_ROUTE
import com.helic.aminesms.utils.Constants.DARK_THEME
import com.helic.aminesms.utils.Constants.auth
import com.helic.aminesms.utils.CustomDivider
import com.helic.aminesms.utils.DisplayAlertDialog

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun Profile(
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit

) {
    val context = LocalContext.current
    val loggedInUser = Firebase.auth.currentUser

    LaunchedEffect(key1 = true) {
        mainViewModel.getBalance(context = context, snackbar = showSnackbar)
    }
    val balance = mainViewModel.userBalance.collectAsState().value

    val user = loggedInUser?.let {
        User(
            it.uid,
            it.displayName.toString(),
            it.email.toString(),
            balance
        )
    }

    Scaffold {
        if (user != null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.backgroundColor
            ) {
                ProfileDetails(
                    context = context,
                    mainViewModel = mainViewModel,
                    navController = navController,
                    showSnackbar = showSnackbar,
                    user = user
                )
            }

        }
    }
}

@Composable
fun ProfileDetails(
    context: Context,
    mainViewModel: MainViewModel,
    navController: NavController,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    user: User
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.welcome),
                    fontWeight = FontWeight.Bold,
                    fontFamily = fancyFont,
                    fontSize = 60.sp
                )
            }
            Text(
                text = user.userName,
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Bold,
            )
            var rowSize by remember { mutableStateOf(Size.Zero) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        //This value is used to assign to the DropDown the same width
                        rowSize = coordinates.size.toSize()
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.userEmail,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
//                    color = MediumGray
                )
                Column {
                    SigningOutFunction(
                        context = context,
                        navController = navController,
                        mainViewModel = mainViewModel,
                        showSnackbar = showSnackbar,
                        rowSize = rowSize
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(10.dp))
        CustomDivider()
        Spacer(modifier = Modifier.padding(10.dp))

        Column {
            Text(
                text = stringResource(R.string.balance),
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(10.dp))
                Card(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(5.dp)),
                    backgroundColor = MaterialTheme.colors.backgroundColor
                )
                {
                    Row(
                        modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${user.userBalance} credits",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.padding(10.dp))
            Card(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.primary,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        navController.navigate(MainAppScreens.Shop.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                backgroundColor = MaterialTheme.colors.backgroundColor
            ) {
                Row(
                    modifier = Modifier.padding(start = 5.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.padding(10.dp))
                    Text(
                        text = stringResource(R.string.add_balance),
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(10.dp))
        CustomDivider()
        Spacer(modifier = Modifier.padding(10.dp))

        Column {
            Text(
                text = stringResource(R.string.change_app_theme),
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Card(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.primary,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.dp))
                    .clickable {
                        mainViewModel.changeAppTheme()
                    },
                backgroundColor = MaterialTheme.colors.backgroundColor
            )
            {
                Row(
                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val themeText: String
                    val themeIcon: ImageVector
                    if (DARK_THEME.value) {
                        themeText = stringResource(R.string.switch_to_light_theme)
                        themeIcon = Icons.Default.LightMode
                    } else {
                        themeText = stringResource(R.string.switch_to_dark_theme)
                        themeIcon = Icons.Default.DarkMode
                    }
                    Icon(imageVector = themeIcon, contentDescription = "Theme Icon")
                    Spacer(modifier = Modifier.padding(end = 5.dp))
                    Text(
                        text = themeText,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}

@Composable
fun SignOutButton(rowSize: Size, onClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            imageVector = icon,
            contentDescription = "Sign out button",
//            tint = MediumGray
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        },
        modifier = Modifier
            .width(with(LocalDensity.current) { rowSize.width.toDp() })
    ) {
        DropdownMenuItem(onClick = {
            onClick()
        }) {
            Text(text = "Sign out")
        }
    }
}

@Composable
fun SigningOutFunction(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit,
    rowSize: Size
) {
    var openDialog by remember { mutableStateOf(false) }

    SignOutButton(rowSize = rowSize) {
        openDialog = true
    }
    DisplayAlertDialog(
        title = stringResource(R.string.sign_out),
        message = {
            Text(
                text = stringResource(R.string.sign_out_confirmation),
                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                fontWeight = FontWeight.Normal
            )
        },
        openDialog = openDialog,
        closeDialog = { openDialog = false },
        onYesClicked = {
            signOut(
                context = context,
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )

        }
    )
}

fun signOut(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    try {
        auth.signOut()

        //we cancel the request from the database upon signing out
        mainViewModel.registration?.remove()
        showSnackbar(context.getString(R.string.successfully_signed_out), SnackbarDuration.Short)
        navController.navigate(AUTHENTICATION_ROUTE) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            }

        }
    } catch (e: Exception) {
        showSnackbar(context.getString(R.string.something_went_wrong), SnackbarDuration.Short)
    }
}

