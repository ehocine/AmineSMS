package com.helic.aminesms.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow

object Constants {

    const val TIMEOUT_IN_MILLIS = 10000L
    var loadingState = MutableStateFlow(LoadingState.IDLE)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    const val ROOT_ROUTE = "root"
    const val AUTHENTICATION_ROUTE = "authentication_root"
    const val MAIN_SCREEN_ROUTE = "main_screen_root"

    val dataStoreKey = booleanPreferencesKey("Theme")
    var DARK_THEME: MutableState<Boolean> = mutableStateOf(false)

    const val FIRESTORE_DATABASE = "users"
    const val USER_BALANCE_DATABASE = "userBalance"
    const val LIST_OF_TEMP_NUMBERS = "listOfTempNumbers"
    const val LIST_OF_RENTAL_NUMBERS = "listOfRentalNumbers"
    const val EMAIL_VERIFIED = "emailVerified"

    val SHOP_LIST = listOf(1, 5, 10, 25)
    val SKU_LIST = listOf("one", "five", "ten", "twenty_five")
    const val REUSE_DISCOUNT_PERCENT = 50

    const val TIME_BETWEEN_AUTO_REFRESH = 1000L

    const val PROJECT_KEY_QONVERSION = "2F3kQuqIZg7Fd3fWhcAYNigWcSWjXPfp"

//    const val BASE_URL = "https://www.smsredux.com/"
//    const val API_KEY = "live_wZw5tBuQLtRPaspTt4HAOQ61FK81Og3bctkfDjmZAHsw"
    const val BASE_URL = "https://staging.whitelabeled.win/"
    const val API_KEY = "test_dmvkYgxH6RaBHVWs51GCxgPWApyaYvYEO2oOnJDNjimA"
}