package com.helic.aminesms.utils

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow

object Constants {

    const val TIMEOUT_IN_MILLIS = 10000L
    var loadingState = MutableStateFlow(LoadingState.IDLE)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    const val ROOT_ROUTE = "root"
    const val AUTHENTICATION_ROUTE = "authentication_root"
    const val MAIN_SCREEN_ROUTE = "main_screen_root"

    const val FIRESTORE_DATABASE = "users"
    const val USER_BALANCE_DATABASE = "userBalance"
    const val LIST_OF_NUMBERS = "listOfNumbers"

    val SHOP_LIST = listOf(1, 5, 10, 25)
    val SKU_LIST = listOf<String>("one", "five", "ten", "twenty_five")

    const val PROJECT_KEY_QONVERSION = "2F3kQuqIZg7Fd3fWhcAYNigWcSWjXPfp"

    //    const val BASE_URL = "https://www.smsredux.com/"
//    const val API_KEY = "live_wZw5tBuQLtRPaspTt4HAOQ61FK81Og3bctkfDjmZAHsw"
    const val BASE_URL = "https://staging.whitelabeled.win/"
    const val API_KEY = "test_GMKGylrC10Xj0QnWSH2gIw04RtkdouZ7r8R8FaJHXqwA"

}