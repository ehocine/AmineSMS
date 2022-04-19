package com.helic.aminesms.presentation.navigation

sealed class MainAppScreens(
    val route: String
) {

    object Profile : MainAppScreens(
        route = "profile"
    )

    object Messages : MainAppScreens(
        route = "messages"
    )

    object MessageDetails : MainAppScreens(
        route = "message_details"
    )

    object Shop : MainAppScreens(
        route = "shop"
    )

    object OrderNumbers : MainAppScreens(
        route = "order_number"
    )

    object OrderNumberOptions : MainAppScreens(
        route = "order_number_options"
    )
}
