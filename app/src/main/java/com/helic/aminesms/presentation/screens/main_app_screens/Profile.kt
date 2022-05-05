package com.helic.aminesms.presentation.screens.main_app_screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.helic.aminesms.R
import com.helic.aminesms.data.models.User
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.presentation.ui.theme.Red
import com.helic.aminesms.utils.Constants.AUTHENTICATION_ROUTE
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
            .padding(10.dp)
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .padding(top = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.welcome),
                Modifier.padding(start = 8.dp, bottom = 15.dp),
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.h5.fontSize
            )
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = user.userName,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(2.dp)
                )
                Text(
                    text = user.userEmail,
                    fontSize = MaterialTheme.typography.subtitle1.fontSize,
                    modifier = Modifier.padding(2.dp)
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))
            CustomDivider()
            Spacer(modifier = Modifier.padding(10.dp))
            Column {
                Text(
                    text = stringResource(R.string.balance),
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(2.dp)
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
                            .clip(RoundedCornerShape(5.dp))
                    )
                    {
                        Row(
                            modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${user.userBalance} credits",
                                fontSize = MaterialTheme.typography.body1.fontSize,
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
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "",
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Text(
                            text = "Add Balance",
                            color = MaterialTheme.colors.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }
            }
        }



        Column(
            verticalArrangement = Arrangement.Bottom
        ) {
//            Spacer(modifier = Modifier.padding(10.dp))
//            CustomDivider()
//            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = "Sign out",
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(2.dp)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            SigningOutFunction(
                context = context,
                navController = navController,
                mainViewModel = mainViewModel,
                showSnackbar = showSnackbar
            )
        }
    }
}

@Composable
fun SigningOutFunction(
    context: Context,
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }

    SignOutButton(onClick = { openDialog = true })

    DisplayAlertDialog(
        title = "Sign out",
        message = "Are you sure you want to sign out?",
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

@Composable
fun SignOutButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Red,
                shape = RoundedCornerShape(5.dp)
            )
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(start = 5.dp, top = 15.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Logout, contentDescription = "", tint = Red)
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = stringResource(R.string.sign_out),
                color = Red,
                fontWeight = FontWeight.Bold
            )
        }

    }
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

