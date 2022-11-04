package com.helic.aminesms.presentation.screens.login_signup_screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.helic.aminesms.data.viewmodels.MainViewModel
import com.helic.aminesms.presentation.navigation.AuthenticationScreens
import com.helic.aminesms.presentation.ui.theme.*
import com.helic.aminesms.utils.*
import com.helic.aminesms.utils.Constants.loadingState
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun LoginPage(
    navController: NavController,
    mainViewModel: MainViewModel,
    showSnackbar: (String, SnackbarDuration) -> Unit
) {

    val user = Constants.auth.currentUser

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    var emailValue by remember { mutableStateOf("") }
    var passwordValue by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    loadingState = MutableStateFlow(LoadingState.IDLE)
    val state by loadingState.collectAsState()

    Surface(modifier = Modifier
        .clickable { focusManager.clearFocus() }
        .background(MaterialTheme.colors.backgroundColor)) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .padding(10.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(state = scrollState)
                ) {
                    Text(
                        text = stringResource(R.string.sign_in),
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.h4.fontSize
                    )
                    Spacer(modifier = Modifier.padding(10.dp))

                    MyLottieAnimation(modifier = Modifier.size(200.dp), lottie = R.raw.login)

                    Spacer(modifier = Modifier.padding(10.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        OutlinedTextField(
                            value = emailValue,
                            onValueChange = { emailValue = it },
                            label = { Text(text = stringResource(R.string.email_address)) },
                            placeholder = { Text(text = stringResource(R.string.email_address)) },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth(0.8f),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.PasswordEyeColor,
                                focusedLabelColor = MaterialTheme.colors.PasswordEyeColor
                            )
                        )
                        Spacer(modifier = Modifier.padding(5.dp))
                        OutlinedTextField(
                            value = passwordValue,
                            onValueChange = { passwordValue = it },
                            trailingIcon = {
                                IconButton(onClick = {
                                    passwordVisibility = !passwordVisibility
                                }) {
                                    Icon(
                                        if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = stringResource(R.string.password_eye),
                                        tint = MaterialTheme.colors.PasswordEyeColor
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisibility) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            label = { Text(text = stringResource(R.string.password)) },
                            placeholder = { Text(text = stringResource(R.string.password)) },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .focusRequester(focusRequester = focusRequester),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colors.PasswordEyeColor,
                                focusedLabelColor = MaterialTheme.colors.PasswordEyeColor
                            )
                            )
                        Spacer(modifier = Modifier.padding(5.dp))
                        Text(text = stringResource(R.string.forget_password),
                            fontWeight = FontWeight.Bold,
                            fontSize = MaterialTheme.typography.subtitle2.fontSize,
                            modifier = Modifier
                                .align(Alignment.End)
                                .clickable {
                                    navController.navigate(route = AuthenticationScreens.ForgetPassword.route) {
                                        // popUpTo = navController.graph.startDestination
                                        launchSingleTop = true
                                    }
                                })

                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(
                            onClick = {
                                signInUser(
                                    navController,
                                    snackbar = showSnackbar,
                                    context,
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
                                CircularProgressIndicator(color = MaterialTheme.colors.ProgressIndicatorColor)
                            } else {
                                Text(
                                    text = stringResource(R.string.sign_in),
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colors.ButtonTextColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.padding(15.dp))
                        Row {
                            Text(
                                text = stringResource(R.string.dont_have_an_account),
                                fontSize = MaterialTheme.typography.subtitle1.fontSize
                            )
                            Spacer(modifier = Modifier.padding(end = 2.dp))
                            Text(
                                text = stringResource(R.string.register_an_account),
                                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    navController.navigate(route = AuthenticationScreens.Register.route) {
                                        // popUpTo = navController.graph.startDestination
                                        launchSingleTop = true
                                    }
                                })
                        }
                        Spacer(modifier = Modifier.padding(15.dp))

                        if (user != null && !user.isEmailVerified) {
                            Row {
                                Text(
                                    text = "Didn't get the verification email?",
                                    fontSize = MaterialTheme.typography.subtitle2.fontSize
                                )
                                Spacer(modifier = Modifier.padding(end = 2.dp))
                                Text(
                                    text = "Request one",
                                    fontSize = MaterialTheme.typography.subtitle2.fontSize,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        resendVerificationEmail(
                                            snackbar = showSnackbar,
                                            context = context
                                        )
                                    })
                            }
                            Text(
                                text = "(${user.email})",
                                fontSize = MaterialTheme.typography.subtitle2.fontSize
                            )
                        }
                        Spacer(modifier = Modifier.padding(20.dp))
                    }
                }
            }
        }
    }
}