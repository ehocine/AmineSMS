package com.helic.aminesms.presentation.navigation

sealed class AuthenticationScreens(
    val route: String
) {
    object Login : AuthenticationScreens(route = "login_screen")
    object Register : AuthenticationScreens(route = "register_screen")
    object ForgetPassword : AuthenticationScreens(route = "forget_password")
}
