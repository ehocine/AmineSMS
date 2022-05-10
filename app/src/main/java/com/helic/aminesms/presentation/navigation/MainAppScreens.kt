package com.helic.aminesms.presentation.navigation

sealed class MainAppScreens(
    val route: String
) {
    object Home : MainAppScreens(
        route = "home"
    )

    object Profile : MainAppScreens(
        route = "profile"
    )

    object TempNumbersMessages : MainAppScreens(
        route = "temp_number_messages"
    )

    object RentalNumbersMessages : MainAppScreens(
        route = "rental_number_message"
    )

    object TempMessageDetails : MainAppScreens(
        route = "temp_message_details"
    )

    object RentalMessageDetails : MainAppScreens(
        route = "rental_message_details"
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

    object RentalNumbers : MainAppScreens(
        route = "rental_number"
    )

    object RentalNumbersOptions : MainAppScreens(
        route = "rental_number_options"
    )
}
