package com.helic.aminesms.utils

import android.content.Context
import android.util.Log
import androidx.compose.material.SnackbarDuration
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.helic.aminesms.R
import com.helic.aminesms.data.models.User
import com.helic.aminesms.data.models.number_data.TempNumberData
import com.helic.aminesms.presentation.navigation.AuthenticationScreens
import com.helic.aminesms.utils.Constants.FIRESTORE_DATABASE
import com.helic.aminesms.utils.Constants.LIST_OF_TEMP_NUMBERS
import com.helic.aminesms.utils.Constants.TIMEOUT_IN_MILLIS
import com.helic.aminesms.utils.Constants.USER_BALANCE_DATABASE
import com.helic.aminesms.utils.Constants.auth
import com.helic.aminesms.utils.Constants.loadingState
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

//Register new user
fun registerNewUser(
    navController: NavController,
    snackbar: (String, SnackbarDuration) -> Unit,
    context: Context,
    userName: String,
    emailAddress: String,
    password: String
) {

    if (hasInternetConnection(context)) {
        if (emailAddress.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        loadingState.emit(LoadingState.LOADING)
                        auth.createUserWithEmailAndPassword(emailAddress, password).await()
                        loadingState.emit(LoadingState.LOADED)
                        withContext(Dispatchers.Main) {
                            val user = Firebase.auth.currentUser
                            val setUserName = userProfileChangeRequest {
                                displayName = userName
                            }
                            user!!.updateProfile(setUserName).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("Tag", user.displayName.toString())
                                }
                            }
                            user.sendEmailVerification().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    snackbar(
                                        context.getString(R.string.verification_email_sent),
                                        SnackbarDuration.Short
                                    )
                                }
                            }
                            createUserWithBalance(user)
                            navController.navigate(AuthenticationScreens.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        loadingState.emit(LoadingState.ERROR)
                        snackbar(context.getString(R.string.time_out), SnackbarDuration.Short)
                    }

                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        snackbar(e.message!!, SnackbarDuration.Short)
                    }
                }
            }
        } else {
            snackbar(context.getString(R.string.verify_inputs), SnackbarDuration.Short)
        }
    } else {
        snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
    }
}

//Sign in existing user
fun signInUser(
    navController: NavController,
    snackbar: (String, SnackbarDuration) -> Unit,
    context: Context,
    emailAddress: String,
    password: String
) {
    if (hasInternetConnection(context)) {
        if (emailAddress.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        loadingState.emit(LoadingState.LOADING)
                        auth.signInWithEmailAndPassword(emailAddress, password).await()
                        loadingState.emit(LoadingState.LOADED)
                        val user = Firebase.auth.currentUser
                        if (user!!.isEmailVerified) {
                            withContext(Dispatchers.Main) {
                                navController.navigate(Constants.MAIN_SCREEN_ROUTE) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                snackbar(
                                    context.getString(R.string.email_address_not_verified),
                                    SnackbarDuration.Short
                                )
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        loadingState.emit(LoadingState.ERROR)
                        snackbar(context.getString(R.string.time_out), SnackbarDuration.Short)
                    }

                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        snackbar(e.message!!, SnackbarDuration.Short)
                    }
                }
            }
        } else {
            snackbar(context.getString(R.string.verify_inputs), SnackbarDuration.Short)
        }
    } else {
        snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
    }
}


//Reset password function
fun resetUserPassword(
    context: Context,
    snackbar: (String, SnackbarDuration) -> Unit,
    emailAddress: String
) {
    if (hasInternetConnection(context)) {
        if (emailAddress.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        loadingState.emit(LoadingState.LOADING)
                        auth.sendPasswordResetEmail(emailAddress).await()
                        loadingState.emit(LoadingState.LOADED)
                        withContext(Dispatchers.Main) {
                            snackbar(context.getString(R.string.email_sent), SnackbarDuration.Short)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        loadingState.emit(LoadingState.ERROR)
                        snackbar(context.getString(R.string.time_out), SnackbarDuration.Short)
                    }
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        snackbar(e.message!!, SnackbarDuration.Short)
                    }
                }
            }
        } else {
            snackbar(context.getString(R.string.verify_inputs), SnackbarDuration.Short)
        }
    } else {
        snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
    }
}

// Function to create a new user with balance by getting the ID from the auth system
fun createUserWithBalance(user: FirebaseUser?) {
    val db = Firebase.firestore
    val newUser = user?.let {
        User(
            it.uid,
            it.displayName.toString(),
            it.email.toString(),
            userBalance = 0.0,
            listOfTempNumbers = listOf()
        )
    }
    if (newUser != null) {
        db.collection(FIRESTORE_DATABASE).document(user.uid)
            .set(newUser)
            .addOnCompleteListener { task ->
                Log.d("Tag", "success $task")
            }.addOnFailureListener { task ->
                Log.d("Tag", "Failure $task")
            }
    }
}

//Function to add or refund balance to users database
fun addBalance(
    context: Context,
    snackbar: (String, SnackbarDuration) -> Unit,
    currentBalance: Double,
    amount: Double,
    addingBalanceState: AddingBalanceState
) {
    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    val data = currentUser?.let { db.collection(FIRESTORE_DATABASE).document(it.uid) }
    if (hasInternetConnection(context)) {
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loadingState.emit(LoadingState.LOADING)
                    data?.update(USER_BALANCE_DATABASE, currentBalance + amount)
                        ?.addOnSuccessListener {
                            when (addingBalanceState) {
                                AddingBalanceState.ADD -> snackbar(
                                    context.getString(R.string.balance_added_successfully),
                                    SnackbarDuration.Short
                                )
                                else -> snackbar(
                                    context.getString(R.string.balance_refunded),
                                    SnackbarDuration.Short
                                )
                            }

                        }?.addOnFailureListener {
                            snackbar("Something went wrong: $it", SnackbarDuration.Short)
                        }
                    loadingState.emit(LoadingState.LOADED)
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        withContext(Dispatchers.Main) {
                            snackbar(e.message!!, SnackbarDuration.Short)
                        }
                    }
                }
            }
        }
    } else {
        snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
    }
}

fun reduceBalance(
    context: Context,
    snackbar: (String, SnackbarDuration) -> Unit,
    currentBalance: Double,
    amount: Double
) {
    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    val data = currentUser?.let { db.collection(FIRESTORE_DATABASE).document(it.uid) }
    if (hasInternetConnection(context)) {
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loadingState.emit(LoadingState.LOADING)
                    data?.update(USER_BALANCE_DATABASE, currentBalance - amount)
                        ?.addOnSuccessListener {

                        }?.addOnFailureListener {
                            snackbar("Something went wrong: $it", SnackbarDuration.Short)
                        }
                    loadingState.emit(LoadingState.LOADED)
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        withContext(Dispatchers.Main) {
                            snackbar(e.message!!, SnackbarDuration.Short)
                        }
                    }
                }
            }
        }
    } else {
        snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
    }
}

fun handleOrderedNumberState(
    context: Context,
    snackbar: (String, SnackbarDuration) -> Unit,
    orderedNumberState: NumberState,
    currentBalance: Double,
    amount: Double
) {
    when (orderedNumberState) {
        NumberState.Expired, NumberState.Canceled -> { // Refund after non-use or cancel
            addBalance(
                context = context,
                snackbar = snackbar,
                currentBalance = currentBalance,
                amount = amount,
                AddingBalanceState.REFUND
            )
        }
        else -> Unit
    }

}


fun addOrRemoveTempNumberFromFirebase(
    context: Context,
    snackbar: (String, SnackbarDuration) -> Unit,
    action: AddOrRemoveNumberAction,
    tempNumberData: TempNumberData?
) {

    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    val data = currentUser?.let { db.collection(FIRESTORE_DATABASE).document(it.uid) }
    if (hasInternetConnection(context)) {
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loadingState.emit(LoadingState.LOADING)
                    when (action) {
                        AddOrRemoveNumberAction.ADD -> {
                            data?.update(
                                LIST_OF_TEMP_NUMBERS,
                                FieldValue.arrayUnion(tempNumberData)
                            )
                                ?.addOnSuccessListener {

                                }?.addOnFailureListener {
                                    snackbar("Something went wrong: $it", SnackbarDuration.Short)
                                }
                            loadingState.emit(LoadingState.LOADED)
                        }
                        AddOrRemoveNumberAction.REMOVE -> {
                            data?.update(
                                LIST_OF_TEMP_NUMBERS,
                                FieldValue.arrayRemove(tempNumberData)
                            )
                                ?.addOnSuccessListener {
                                }?.addOnFailureListener {
                                    snackbar("Something went wrong: $it", SnackbarDuration.Short)
                                }
                            loadingState.emit(LoadingState.LOADED)
                        }
                    }
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        withContext(Dispatchers.Main) {
                            snackbar(e.message!!, SnackbarDuration.Short)
                        }
                    }
                }
            }
        }
    } else {
        snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
    }
}

fun updateNumberState(
    context: Context,
    snackbar: (String, SnackbarDuration) -> Unit,
    tempNumberToBeUpdated: TempNumberData?,
    newState: NumberState
) {

    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    val data = currentUser?.let { db.collection(FIRESTORE_DATABASE).document(it.uid) }
    if (hasInternetConnection(context)) {
        if (currentUser != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loadingState.emit(LoadingState.LOADING)
                    data?.update(
                        LIST_OF_TEMP_NUMBERS,
                        FieldValue.arrayRemove(tempNumberToBeUpdated)
                    )
                        ?.addOnSuccessListener {
                            if (tempNumberToBeUpdated != null) {
                                tempNumberToBeUpdated.state = newState.toString()
                            }
                            data.update(
                                LIST_OF_TEMP_NUMBERS,
                                FieldValue.arrayUnion(tempNumberToBeUpdated)
                            )
                        }?.addOnFailureListener {
                            snackbar("Something went wrong: $it", SnackbarDuration.Short)
                        }
                    loadingState.emit(LoadingState.LOADED)
                } catch (e: Exception) {
                    loadingState.emit(LoadingState.ERROR)
                    withContext(Dispatchers.Main) {
                        withContext(Dispatchers.Main) {
                            snackbar(e.message!!, SnackbarDuration.Short)
                        }
                    }
                }
            }
        }
    } else {
        snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
    }
}

// Function to check is the user is not null and has email verified
fun userLoggedIn(): Boolean {
    val user = Firebase.auth.currentUser
    return user != null && user.isEmailVerified
}

