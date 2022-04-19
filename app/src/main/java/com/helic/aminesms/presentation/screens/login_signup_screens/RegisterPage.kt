package com.helic.aminesms.presentation.screens.login_signup_screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.helic.aminesms.R
import com.helic.aminesms.presentation.navigation.AuthenticationScreens
import com.helic.aminesms.presentation.ui.theme.ButtonColor
import com.helic.aminesms.presentation.ui.theme.primaryColor
import com.helic.aminesms.utils.Constants.loadingState
import com.helic.aminesms.utils.LoadingState
import com.helic.aminesms.utils.registerNewUser
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun RegisterPage(navController: NavController, showSnackbar: (String, SnackbarDuration) -> Unit) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var nameValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }

    loadingState = MutableStateFlow(LoadingState.IDLE)
    val state by loadingState.collectAsState()

    var passwordVisibility by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.clickable { focusManager.clearFocus() }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.70f)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(state = scrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.sign_up),
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.h4.fontSize
                    )
                    Spacer(modifier = Modifier.padding(20.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedTextField(
                            value = nameValue,
                            onValueChange = { nameValue = it },
                            label = { Text(text = stringResource(R.string.name)) },
                            placeholder = { Text(text = stringResource(R.string.name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                        Spacer(modifier = Modifier.padding(5.dp))
                        OutlinedTextField(
                            value = emailValue,
                            onValueChange = { emailValue = it },
                            label = { Text(text = stringResource(R.string.email_address)) },
                            placeholder = { Text(text = stringResource(R.string.email_address)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                        Spacer(modifier = Modifier.padding(5.dp))
                        OutlinedTextField(
                            value = passwordValue,
                            onValueChange = { passwordValue = it },
                            label = { Text(text = stringResource(R.string.password)) },
                            placeholder = { Text(text = stringResource(R.string.password)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            trailingIcon = {
                                IconButton(onClick = {
                                    passwordVisibility = !passwordVisibility
                                }) {
                                    Icon(
                                        if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Password Eye",
                                        tint = primaryColor
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisibility) VisualTransformation.None
                            else PasswordVisualTransformation()
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(
                            onClick = {
                                registerNewUser(
                                    navController,
                                    snackbar = showSnackbar,
                                    context,
                                    nameValue,
                                    emailValue,
                                    passwordValue
                                )
                            },
                            enabled = state != LoadingState.LOADING,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colors.ButtonColor)
                        ) {
                            if (state == LoadingState.LOADING) {
                                CircularProgressIndicator(color = MaterialTheme.colors.ButtonColor)
                            } else {
                                Text(
                                    text = stringResource(R.string.sign_up),
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }


                        }
                        Spacer(modifier = Modifier.padding(15.dp))
                        Row {
                            Text(
                                text = stringResource(R.string.have_an_account_login),
                                fontSize = MaterialTheme.typography.subtitle1.fontSize
                            )
                            Spacer(modifier = Modifier.padding(end = 2.dp))
                            Text(
                                text = "Login",
                                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    navController.navigate(route = AuthenticationScreens.Login.route) {
                                        // popUpTo = navController.graph.startDestination
                                        launchSingleTop = true
                                    }
                                })
                        }
                        Spacer(modifier = Modifier.padding(20.dp))
                    }
                }
            }
        }
    }
}
