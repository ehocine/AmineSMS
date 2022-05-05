package com.helic.aminesms.data.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.helic.aminesms.R
import com.helic.aminesms.data.models.cancel_number.CancelNumberResponse
import com.helic.aminesms.data.models.messages.Sms
import com.helic.aminesms.data.models.number_data.RentalNumberData
import com.helic.aminesms.data.models.number_data.ReusableNumbersData
import com.helic.aminesms.data.models.number_data.TempNumberData
import com.helic.aminesms.data.models.rental_numbers.RentalNumberServiceState
import com.helic.aminesms.data.models.rental_numbers.rental_options.RentalOptionsData
import com.helic.aminesms.data.models.temp_number.ListOfOrderedRentalNumber
import com.helic.aminesms.data.models.temp_number.ListOfOrderedTempNumber
import com.helic.aminesms.data.models.temp_number.service_state.ServiceState
import com.helic.aminesms.data.repository.Repository
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.utils.*
import com.helic.aminesms.utils.Constants.FIRESTORE_DATABASE
import com.helic.aminesms.utils.Constants.TIMEOUT_IN_MILLIS
import com.helic.aminesms.utils.Constants.USER_BALANCE_DATABASE
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionOfferingsCallback
import com.qonversion.android.sdk.QonversionPermissionsCallback
import com.qonversion.android.sdk.dto.QPermission
import com.qonversion.android.sdk.dto.offerings.QOffering
import com.qonversion.android.sdk.dto.offerings.QOfferings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {

    var offerings by mutableStateOf<List<QOffering>>(emptyList())
        private set

    init {
        loadOfferings()
    }

    private fun loadOfferings() {
        Qonversion.offerings(object : QonversionOfferingsCallback {
            override fun onError(error: QonversionError) = Unit

            override fun onSuccess(offerings: QOfferings) {
                this@MainViewModel.offerings = offerings.availableOfferings
            }
        })
    }

    fun updatePermissions(context: Context, snackbar: (String, SnackbarDuration) -> Unit) {
        Qonversion.checkPermissions(object : QonversionPermissionsCallback {
            override fun onError(error: QonversionError) {
                snackbar(
                    "${context.getString(R.string.something_went_wrong)}: ${error.description}",
                    SnackbarDuration.Short
                )
            }

            override fun onSuccess(permissions: Map<String, QPermission>) {
                TODO("Not yet implemented")
            }
        })
    }


    private var _addBalanceAmount = MutableStateFlow(0.0)
    private var addBalanceAmount = _addBalanceAmount.asStateFlow()

    val searchAppBarState: MutableState<SearchAppBarState> =
        mutableStateOf(SearchAppBarState.CLOSED)
    val searchTextState: MutableState<String> = mutableStateOf("")

    private var _userBalance = MutableStateFlow(0.0)
    var userBalance = _userBalance.asStateFlow()

    //This variable is used to cancel request upon signing out or closing the app
    var registration: ListenerRegistration? = null

    val serviceStateListResponse: MutableState<List<ServiceState>> =
        mutableStateOf(listOf())

    val rentalServiceStateList: MutableState<List<RentalNumberServiceState>> =
        mutableStateOf(listOf())

    private val _isRefreshing = MutableStateFlow(false)

    val isRefreshing: StateFlow<Boolean>
        get() = _isRefreshing.asStateFlow()

    val selectedServiceState: MutableState<ServiceState> =
        mutableStateOf(ServiceState())

    val selectedAreaCode: MutableState<String> = mutableStateOf("")

    private val tempNumberData: MutableState<TempNumberData> =
        mutableStateOf(TempNumberData())

    @SuppressLint("MutableCollectionMutableState")
    private var _TempNumbersList: MutableStateFlow<MutableList<TempNumberData>> =
        MutableStateFlow(mutableListOf())
    var orderedTempNumbersList = _TempNumbersList.asStateFlow()

    private var _RentalNumbersList: MutableStateFlow<MutableList<RentalNumberData>> =
        MutableStateFlow(mutableListOf())
    var orderedRentalNumbers = _RentalNumbersList.asStateFlow()

    val selectedTempNumber: MutableState<TempNumberData> =
        mutableStateOf(TempNumberData())

    val message: MutableState<Sms>? = mutableStateOf(Sms())

    private val cancelTempNumber: MutableState<CancelNumberResponse> =
        mutableStateOf(CancelNumberResponse())

    private var _superUserBalance = MutableStateFlow(0.0)
    var superUserBalance = _superUserBalance.asStateFlow()

    var reusableNumbersList: MutableState<List<ReusableNumbersData>> = mutableStateOf(listOf())

    private var reuseTempNumberResponse: MutableState<TempNumberData> =
        mutableStateOf(TempNumberData())

    private var rentalServicePrice: MutableState<Double> = mutableStateOf(0.0)

    fun proceedToBuy(
        chosenOption: Int,
        showSnackbar: (String, SnackbarDuration) -> Unit
    ) {
        _addBalanceAmount.value = dollarToCreditForPurchasingCurrency(chosenOption.toDouble())
        Log.d("Tag", "You chose to buy $chosenOption option")
        addBalance(
            context = getApplication<Application>(),
            snackbar = showSnackbar,
            currentBalance = userBalance.value,
            amount = addBalanceAmount.value,
            AddingBalanceState.ADD
        )
    }

    fun getBalance(context: Context, snackbar: (String, SnackbarDuration) -> Unit) {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val data = currentUser?.let { db.collection(FIRESTORE_DATABASE).document(it.uid) }
        if (hasInternetConnection(getApplication<Application>())) {
            if (currentUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        loadingStateOfViewModel.emit(LoadingState.LOADING)
                        registration = data?.addSnapshotListener { value, error ->
                            if (error != null) {
                                snackbar("Error occurred: $error", SnackbarDuration.Short)
                                return@addSnapshotListener
                            }
                            if (value != null && value.exists()) {
                                _userBalance.value = value.getDouble(USER_BALANCE_DATABASE)!!
                                // If the balance goes under 0 we set it back to 0 and we update the database
                                if (_userBalance.value < 0) {
                                    _userBalance.value = 0.0
                                    data.update(USER_BALANCE_DATABASE, 0.0)
                                }
                            } else {
                                snackbar(
                                    context.getString(R.string.error_occurred),
                                    SnackbarDuration.Short
                                )
                            }
                        }
                        loadingStateOfViewModel.emit(LoadingState.LOADED)
                    } catch (e: Exception) {

                        //Cancel the database request if there is an error
                        registration?.remove()
                        loadingStateOfViewModel.emit(LoadingState.ERROR)
                        withContext(Dispatchers.Main) {
                            snackbar(e.message!!, SnackbarDuration.Short)
                        }
                    }
                }
            }
        } else {
            snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
        }
    }

    var gettingListOfTempNumbersLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun getListOfTempNumbersFromFirebase(
        context: Context,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val data = currentUser?.let { db.collection(FIRESTORE_DATABASE).document(it.uid) }
        if (hasInternetConnection(getApplication<Application>())) {
            if (currentUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        gettingListOfTempNumbersLoadingState.emit(LoadingState.LOADING)
                        registration = data?.addSnapshotListener { value, error ->
                            if (error != null) {
                                snackbar("Error occurred: $error", SnackbarDuration.Short)
                                return@addSnapshotListener
                            }
                            if (value != null && value.exists()) {
                                _TempNumbersList.value =
                                    value.toObject(ListOfOrderedTempNumber::class.java)?.listOfTempNumbers
                                        ?: mutableListOf()
                            } else {
                                snackbar(
                                    context.getString(R.string.error_occurred),
                                    SnackbarDuration.Short
                                )
                            }
                        }
                        gettingListOfTempNumbersLoadingState.emit(LoadingState.LOADED)
                    } catch (e: Exception) {

                        //Cancel the database request if there is an error
                        registration?.remove()
                        gettingListOfTempNumbersLoadingState.emit(LoadingState.ERROR)
                        withContext(Dispatchers.Main) {
                            snackbar(e.message!!, SnackbarDuration.Short)
                        }
                    }
                }
            }
        } else {
            snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
        }
    }

    var gettingListOfRentalNumbersLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun getListOfRentalNumbersFromFirebase(
        context: Context,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser
        val data = currentUser?.let { db.collection(FIRESTORE_DATABASE).document(it.uid) }
        if (hasInternetConnection(getApplication<Application>())) {
            if (currentUser != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        gettingListOfRentalNumbersLoadingState.emit(LoadingState.LOADING)
                        registration = data?.addSnapshotListener { value, error ->
                            if (error != null) {
                                snackbar("Error occurred: $error", SnackbarDuration.Short)
                                return@addSnapshotListener
                            }
                            if (value != null && value.exists()) {
                                _RentalNumbersList.value =
                                    value.toObject(ListOfOrderedRentalNumber::class.java)?.listOfRentalNumbers
                                        ?: mutableListOf()
                            } else {
                                snackbar(
                                    context.getString(R.string.error_occurred),
                                    SnackbarDuration.Short
                                )
                            }
                        }
                        gettingListOfRentalNumbersLoadingState.emit(LoadingState.LOADED)
                    } catch (e: Exception) {

                        //Cancel the database request if there is an error
                        registration?.remove()
                        gettingListOfRentalNumbersLoadingState.emit(LoadingState.ERROR)
                        withContext(Dispatchers.Main) {
                            snackbar(e.message!!, SnackbarDuration.Short)
                        }
                    }
                }
            }
        } else {
            snackbar(context.getString(R.string.device_not_connected), SnackbarDuration.Short)
        }
    }


    var loadingStateOfViewModel = MutableStateFlow(LoadingState.IDLE)

    fun getServiceStateList(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        loadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.getServiceStateList()
                        if (response.isSuccessful) {
                            serviceStateListResponse.value = response.body()!!.serviceStateList
                            loadingStateOfViewModel.emit(LoadingState.LOADED)
                        } else {
                            loadingStateOfViewModel.emit(LoadingState.ERROR)
                            snackbar(
                                getApplication<Application>().getString(R.string.an_error_occurred),
                                SnackbarDuration.Short
                            )
                        }

                    } ?: withContext(Dispatchers.Main) {
                        loadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    loadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }
            } else {
                loadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    fun refreshServiceStateList(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            getServiceStateList(snackbar = snackbar)
            _isRefreshing.emit(false)
        }
    }

    var buyingLoadingStateOfViewModel = MutableStateFlow(LoadingState.IDLE)

    fun orderNumber(
        serviceID: String,
        areaCode: String,
        snackbar: (String, SnackbarDuration) -> Unit,
        navController: NavController
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        buyingLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.orderNumber(
                            serviceID = serviceID,
                            areaCode = areaCode
                        )
                        if (response.isSuccessful) {
                            tempNumberData.value = response.body()!!.tempNumberData
                            buyingLoadingStateOfViewModel.emit(LoadingState.LOADED)

                            reduceBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = _userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(tempNumberData.value.price)
                            )
                            addOrRemoveTempNumberFromFirebase(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                AddOrRemoveNumberAction.ADD,
                                tempNumberData = tempNumberData.value
                            )
                            navController.navigate(MainAppScreens.TempNumberMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }

                    } ?: withContext(Dispatchers.Main) {
                        buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }

            } else {
                buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    var checkingMessagesLoadingStateOfViewModel = MutableStateFlow(LoadingState.IDLE)

    fun checkForMessages(
        context: Context,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        checkingMessagesLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.getTempNumberInfo(
                            temporaryNumberId = temporaryNumberId
                        )
                        if (response.isSuccessful) {
                            message?.value = response.body()!!.data.sms
                            if (message?.value != null) {
                                val orderedNumberInList =
                                    orderedTempNumbersList.value.find { it.temporaryNumberId == temporaryNumberId }
                                updateTempNumberState(
                                    context = context,
                                    snackbar = snackbar,
                                    tempNumberToBeUpdated = orderedNumberInList,
                                    newState = NumberState.Completed
                                )
                            }
                            checkingMessagesLoadingStateOfViewModel.emit(LoadingState.LOADED)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        checkingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    checkingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }

            } else {
                checkingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    // This function will be launched later with a LaunchedEffect in the MassageDetails screen
    private var autoCheckingMessagesLoadingStateOfViewModel = MutableStateFlow(LoadingState.IDLE)
    fun autoCheckMessage(
        context: Context,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        autoCheckingMessagesLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.getTempNumberInfo(
                            temporaryNumberId = temporaryNumberId
                        )
                        if (response.isSuccessful) {
                            message?.value = response.body()!!.data.sms
                            if (message?.value != null) {
                                val orderedNumberInList =
                                    orderedTempNumbersList.value.find { it.temporaryNumberId == temporaryNumberId }
                                updateTempNumberState(
                                    context = context,
                                    snackbar = snackbar,
                                    tempNumberToBeUpdated = orderedNumberInList,
                                    newState = NumberState.Completed
                                )
                            }
                            autoCheckingMessagesLoadingStateOfViewModel.emit(LoadingState.LOADED)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        autoCheckingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    autoCheckingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }

            } else {
                autoCheckingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    fun refreshMessageCheck(
        context: Context,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            checkForMessages(
                context = context,
                temporaryNumberId = temporaryNumberId,
                snackbar = snackbar
            )
            _isRefreshing.emit(false)
        }
    }

    fun cancelTempNumber(
        context: Context,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit,
        navController: NavController
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
//                        checkingMessagesLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.cancelTempNumber(
                            temporaryNumberId = temporaryNumberId
                        )
                        if (response.isSuccessful) {
                            cancelTempNumber.value = response.body()!!
                            val orderedNumberInList =
                                orderedTempNumbersList.value.find { it.temporaryNumberId == temporaryNumberId }

                            updateTempNumberState(
                                context = context,
                                snackbar = snackbar,
                                tempNumberToBeUpdated = orderedNumberInList,
                                NumberState.Canceled
                            )
//                            checkingMessagesLoadingStateOfViewModel.emit(LoadingState.LOADED)

                            if (orderedNumberInList != null) {
                                handleOrderedNumberState(
                                    getApplication<Application>(),
                                    snackbar,
                                    NumberState.Canceled,
                                    userBalance.value,
                                    dollarToCreditForPurchasingNumbers(orderedNumberInList.price)
                                )
                            }
                            navController.navigate(MainAppScreens.TempNumberMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
//                        checkingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
//                    checkingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }

            } else {
//                checkingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    var checkingSuperUserBalanceLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun getSuperUserBalance(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        checkingSuperUserBalanceLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.getSuperUserBalance()
                        if (response.isSuccessful) {
                            _superUserBalance.value = response.body()!!.data
                        }
                        checkingSuperUserBalanceLoadingState.emit(LoadingState.LOADED)
                    } ?: withContext(Dispatchers.Main) {
                        checkingSuperUserBalanceLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    checkingSuperUserBalanceLoadingState.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }
            } else {
                checkingSuperUserBalanceLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    //TODO it seems there is an issue with the reusableUntil value from the test API, it's always zero when getting the number ordered. Check with the production API
    fun getReusableNumbersList(
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        buyingLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.getReusableNumbers()
                        if (response.isSuccessful) {
                            reusableNumbersList.value = response.body()!!.reusableNumbersListData

                            reusableNumbersList.value.forEach {
                                Log.d("Tag", "Service: ${it.serviceName}, price: ${it.price} ")
                            }
                            buyingLoadingStateOfViewModel.emit(LoadingState.LOADED)
                        }

                    } ?: withContext(Dispatchers.Main) {
                        buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }

            } else {
                buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    fun reuseNumber(
        navController: NavController,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        buyingLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response =
                            repository.remote.reuseNumber(temporaryNumberId = temporaryNumberId)
                        if (response.isSuccessful) {
                            reuseTempNumberResponse.value = response.body()!!.reuseTempNumberData
                            buyingLoadingStateOfViewModel.emit(LoadingState.LOADED)

                            reduceBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = _userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(reuseTempNumberResponse.value.price)
                            )
                            addOrRemoveTempNumberFromFirebase(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                action = AddOrRemoveNumberAction.ADD,
                                tempNumberData = reuseTempNumberResponse.value
                            )
                            navController.navigate(MainAppScreens.TempNumberMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }
            } else {
                buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    var rentalServiceLoadingStateOfViewModel = MutableStateFlow(LoadingState.IDLE)

    fun getRentalServiceStateList(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        rentalServiceLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.getRentNumberServiceStateList()
                        if (response.isSuccessful) {
                            rentalServiceStateList.value =
                                response.body()!!.rentalServiceStateList
                            rentalServiceLoadingStateOfViewModel.emit(LoadingState.LOADED)
                        } else {
                            rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                            snackbar(
                                getApplication<Application>().getString(R.string.an_error_occurred),
                                SnackbarDuration.Short
                            )
                        }

                    } ?: withContext(Dispatchers.Main) {
                        rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }
            } else {
                rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    fun refreshRentalServiceStateList(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            getRentalServiceStateList(snackbar = snackbar)
            _isRefreshing.emit(false)
        }
    }

    val availableRentalOptions: MutableState<List<RentalOptionsData>> =
        mutableStateOf(listOf())

    //    val rentalPeriod
    var rentalPeriodOption: MutableState<Int> = mutableStateOf(0)
    var selectedRentalService: MutableState<RentalNumberServiceState> =
        mutableStateOf(RentalNumberServiceState())

    fun getRentalNumberOptions(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        rentalServiceLoadingStateOfViewModel.emit(LoadingState.LOADING)
                        val response = repository.remote.getRentalOptions()
                        if (response.isSuccessful) {
                            availableRentalOptions.value =
                                response.body()!!.rentalOptionsData
                            rentalServiceLoadingStateOfViewModel.emit(LoadingState.LOADED)
                            availableRentalOptions.value.forEach {
                                Log.d("Option", "${it.rentalType} + ${it.duration}")
                            }
                        } else {
                            rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                            snackbar(
                                getApplication<Application>().getString(R.string.an_error_occurred),
                                SnackbarDuration.Short
                            )
                        }

                    } ?: withContext(Dispatchers.Main) {
                        rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                    Log.d("Tag", e.message.toString())
                }
            } else {
                rentalServiceLoadingStateOfViewModel.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    var rentalServicePriceLoadingState = MutableStateFlow(LoadingState.IDLE)
    var rentalPrice: MutableState<Double> = mutableStateOf(0.0)

    var gotRentalPrice: MutableState<Boolean> = mutableStateOf(false)

    fun getRentalServicePrice(
        durationInHours: Int,
        serviceId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        rentalServicePriceLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.getRentalServicePrice(
                            durationInHours = durationInHours,
                            serviceId = serviceId
                        )
                        if (response.isSuccessful) {
                            rentalServicePrice.value =
                                response.body()!!.price
                            rentalServicePriceLoadingState.emit(LoadingState.LOADED)
                            rentalPrice.value = rentalServicePrice.value

                            gotRentalPrice.value = true

                        } else {
                            rentalServicePriceLoadingState.emit(LoadingState.ERROR)
                            snackbar(
                                getApplication<Application>().getString(R.string.an_error_occurred),
                                SnackbarDuration.Short
                            )
                        }
                    } ?: withContext(Dispatchers.Main) {
                        rentalServicePriceLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    rentalServicePriceLoadingState.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                }
            } else {
                rentalServicePriceLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    private val rentalNumberData: MutableState<RentalNumberData> =
        mutableStateOf(RentalNumberData())

    var orderRentalNumberLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun orderRentalNumber(
        serviceId: String,
        durationInHours: Int,
        snackbar: (String, SnackbarDuration) -> Unit,
        navController: NavController
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        orderRentalNumberLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.orderRentalNumber(
                            serviceId = serviceId,
                            durationInHours = durationInHours
                        )
                        if (response.isSuccessful) {
                            rentalNumberData.value = response.body()!!.rentalNumberData
                            orderRentalNumberLoadingState.emit(LoadingState.LOADED)
                            reduceBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = _userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(rentalNumberData.value.price.toDouble())
                            )
                            addOrRemoveRentalNumberFromFirebase(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                AddOrRemoveNumberAction.ADD,
                                rentalNumberData = rentalNumberData.value
                            )
                            navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }

                    } ?: withContext(Dispatchers.Main) {
                        orderRentalNumberLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    orderRentalNumberLoadingState.emit(LoadingState.ERROR)
                    snackbar(e.message!!, SnackbarDuration.Short)
                    Log.d("Tag", e.message.toString())
                }

            } else {
                orderRentalNumberLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

}