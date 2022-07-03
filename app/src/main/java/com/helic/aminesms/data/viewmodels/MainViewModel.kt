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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
import com.helic.aminesms.data.models.rental_numbers.renew_rental_number.RenewRentalNumberData
import com.helic.aminesms.data.models.rental_numbers.renewal_rental_numbers.RenewalRequestStateData
import com.helic.aminesms.data.models.rental_numbers.rental_options.RentalOptionsData
import com.helic.aminesms.data.models.temp_number.ListOfOrderedRentalNumber
import com.helic.aminesms.data.models.temp_number.ListOfOrderedTempNumber
import com.helic.aminesms.data.models.temp_number.service_state.ServiceState
import com.helic.aminesms.data.repository.Repository
import com.helic.aminesms.presentation.navigation.MainAppScreens
import com.helic.aminesms.utils.*
import com.helic.aminesms.utils.Constants.DARK_THEME
import com.helic.aminesms.utils.Constants.FIRESTORE_DATABASE
import com.helic.aminesms.utils.Constants.TIMEOUT_IN_MILLIS
import com.helic.aminesms.utils.Constants.USER_BALANCE_DATABASE
import com.helic.aminesms.utils.Constants.dataStoreKey
import com.qonversion.android.sdk.Qonversion
import com.qonversion.android.sdk.QonversionError
import com.qonversion.android.sdk.QonversionProductsCallback
import com.qonversion.android.sdk.dto.products.QProduct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val dataStore: DataStore<Preferences>,
    application: Application
) : AndroidViewModel(application) {

    var products by mutableStateOf<Collection<QProduct>>(emptyList())
        private set

    init {
        loadProducts()
        getCurrencyParameters(context = getApplication<Application>())
    }

    private fun loadProducts() {
        Qonversion.products(callback = object : QonversionProductsCallback {
            override fun onSuccess(products: Map<String, QProduct>) {
                Log.d("Products", products.toString())
                this@MainViewModel.products = products.values
                this@MainViewModel.products.forEach {
                    Log.d("Product", it.qonversionID)
                }
            }

            override fun onError(error: QonversionError) {
                Log.d("Products Error", error.description)
            }
        })
    }

    private var _purchasingCurrency = MutableStateFlow(10.0)
    var purchasingCurrency = _purchasingCurrency.asStateFlow()

    private var _purchasingNumbersA = MutableStateFlow(10.0)
    var purchasingNumbersA = _purchasingNumbersA.asStateFlow()

    private var _purchasingNumbersB = MutableStateFlow(0.75)
    var purchasingNumbersB = _purchasingNumbersB.asStateFlow()

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
    private var _tempNumbersList: MutableStateFlow<MutableList<TempNumberData>> =
        MutableStateFlow(mutableListOf())
    var orderedTempNumbersList = _tempNumbersList.asStateFlow()

    private var _rentalNumbersList: MutableStateFlow<MutableList<RentalNumberData>> =
        MutableStateFlow(mutableListOf())
    var orderedRentalNumbers = _rentalNumbersList.asStateFlow()

    val selectedTempNumber: MutableState<TempNumberData> =
        mutableStateOf(TempNumberData())

    val selectedRentalNumber: MutableState<RentalNumberData> =
        mutableStateOf(RentalNumberData())

    val message: MutableState<Sms>? = mutableStateOf(Sms())

    private val cancelTempNumber: MutableState<CancelNumberResponse> =
        mutableStateOf(CancelNumberResponse())

    private var _superUserBalance = MutableStateFlow(0.0)
    var superUserBalance = _superUserBalance.asStateFlow()

    var reusableNumbersList: MutableState<List<ReusableNumbersData>> = mutableStateOf(listOf())

    private var reuseTempNumberResponse: MutableState<TempNumberData> =
        mutableStateOf(TempNumberData())

    private var rentalServicePrice: MutableState<Double> = mutableStateOf(0.0)

//    fun proceedToBuy(
//        chosenOption: Int,
//        showSnackbar: (String, SnackbarDuration) -> Unit
//    ) {
//        _addBalanceAmount.value = dollarToCreditForPurchasingCurrency(chosenOption.toDouble())
//        Log.d("Tag", "You chose to buy $chosenOption option")
//        addBalance(
//            context = getApplication<Application>(),
//            snackbar = showSnackbar,
//            currentBalance = userBalance.value,
//            amount = addBalanceAmount.value,
//            addingBalanceState = AddingBalanceState.ADD
//        )
//    }

    fun getCurrencyParameters(
        context: Context,
    ) {
        val db = Firebase.firestore
        val data = db.collection(Constants.FIRESTORE_PARAMETERS_DATABASE)
            .document(Constants.FIRESTORE_PARAMETERS_DOCUMENT)
        if (hasInternetConnection(context)) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    data.addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        if (value != null && value.exists()) {
                            _purchasingCurrency.value =
                                value.getDouble(Constants.PURCHASING_CURRENCY)!!
                            _purchasingNumbersA.value =
                                value.getDouble(Constants.PURCHASING_NUMBERS_A)!!
                            _purchasingNumbersB.value =
                                value.getDouble(Constants.PURCHASING_NUMBERS_B)!!
                        } else {
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                    }
                }
            }
        } else {
        }
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
                            snackbar(
                                getApplication<Application>().getString(R.string.an_error_occurred),
                                SnackbarDuration.Short
                            )
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
                                _tempNumbersList.value =
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
                            snackbar(
                                getApplication<Application>().getString(R.string.an_error_occurred),
                                SnackbarDuration.Short
                            )
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
                                _rentalNumbersList.value =
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
                            snackbar(
                                getApplication<Application>().getString(R.string.an_error_occurred),
                                SnackbarDuration.Short
                            )
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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
                        if (response.isSuccessful && response.body()?.succeeded == true) {
                            tempNumberData.value = response.body()!!.tempNumberData
                            buyingLoadingStateOfViewModel.emit(LoadingState.LOADED)

                            reduceBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = _userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(
                                    tempNumberData.value.price,
                                    mainViewModel = this@MainViewModel
                                )
                            )
                            addOrRemoveTempNumberFromFirebase(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                AddOrRemoveNumberAction.ADD,
                                tempNumberData = tempNumberData.value
                            )
                            navController.navigate(MainAppScreens.TempNumbersMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            buyingLoadingStateOfViewModel.emit(LoadingState.ERROR)
                            snackbar(
                                getApplication<Application>().getString(R.string.couldnt_process_request),
                                SnackbarDuration.Short
                            )
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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

    var checkingTempMessagesLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun checkForTempNumbersMessages(
        context: Context,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        checkingTempMessagesLoadingState.emit(LoadingState.LOADING)
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
                            checkingTempMessagesLoadingState.emit(LoadingState.LOADED)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        checkingTempMessagesLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    checkingTempMessagesLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                }

            } else {
                checkingTempMessagesLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    // This function will be launched later with a LaunchedEffect in the MassageDetails screen
    private var autoCheckingMessagesLoadingStateOfViewModel = MutableStateFlow(LoadingState.IDLE)
    fun autoCheckTempMessage(
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
                        Log.d("Tag", "Time out")
//                        snackbar(
//                            getApplication<Application>().getString(R.string.time_out),
//                            SnackbarDuration.Short
//                        )
                    }
                } catch (e: Exception) {
                    autoCheckingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                    Log.d("Tag", e.message.toString())
//                    snackbar(
//                        getApplication<Application>().getString(R.string.an_error_occurred),
//                        SnackbarDuration.Short
//                    )
                }

            } else {
                autoCheckingMessagesLoadingStateOfViewModel.emit(LoadingState.ERROR)
                Log.d("Tag", "Device not connected")
//                snackbar(
//                    getApplication<Application>().getString(R.string.device_not_connected),
//                    SnackbarDuration.Short
//                )
            }
        }
    }

    fun refreshTempNumberMessageCheck(
        context: Context,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            checkForTempNumbersMessages(
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
                                    dollarToCreditForPurchasingNumbers(
                                        orderedNumberInList.price,
                                        mainViewModel = this@MainViewModel
                                    )
                                )
                            }
                            navController.navigate(MainAppScreens.TempNumbersMessages.route) {
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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

    //TODO it seems there is an issue with the reusableUntil value from the test API, it's always zero when getting the number ordered. Must check with the production API
    var tempReusableListLoadingState = MutableStateFlow(LoadingState.IDLE)
    fun getReusableNumbersList(
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        tempReusableListLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.getReusableNumbers()
                        if (response.isSuccessful) {
                            reusableNumbersList.value = response.body()!!.reusableNumbersListData

                            reusableNumbersList.value.forEach {
                                Log.d("Tag", "Service: ${it.serviceName}, price: ${it.price} ")
                            }
                            tempReusableListLoadingState.emit(LoadingState.LOADED)
                        }

                    } ?: withContext(Dispatchers.Main) {
                        tempReusableListLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    tempReusableListLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                }

            } else {
                tempReusableListLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    var reuseTemNumberLoadingState = MutableStateFlow(LoadingState.IDLE)
    fun reuseNumber(
        navController: NavController,
        temporaryNumberId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        reuseTemNumberLoadingState.emit(LoadingState.LOADING)
                        val response =
                            repository.remote.reuseNumber(temporaryNumberId = temporaryNumberId)
                        if (response.isSuccessful && response.body()?.succeeded == true) {
                            reuseTempNumberResponse.value = response.body()!!.reuseTempNumberData
                            reuseTemNumberLoadingState.emit(LoadingState.LOADED)

                            reduceBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = _userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(
                                    reuseTempNumberResponse.value.price,
                                    mainViewModel = this@MainViewModel
                                )
                            )
                            addOrRemoveTempNumberFromFirebase(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                action = AddOrRemoveNumberAction.ADD,
                                tempNumberData = reuseTempNumberResponse.value
                            )
                            navController.navigate(MainAppScreens.TempNumbersMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            reuseTemNumberLoadingState.emit(LoadingState.ERROR)
                            snackbar(
                                getApplication<Application>().getString(R.string.couldnt_process_request),
                                SnackbarDuration.Short
                            )
                        }
                    } ?: withContext(Dispatchers.Main) {
                        reuseTemNumberLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    reuseTemNumberLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                }
            } else {
                reuseTemNumberLoadingState.emit(LoadingState.ERROR)
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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
                        if (response.isSuccessful && response.body()?.succeeded == true) {
                            rentalNumberData.value = response.body()!!.rentalNumberData
                            orderRentalNumberLoadingState.emit(LoadingState.LOADED)
                            reduceBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = _userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(
                                    rentalNumberData.value.price,
                                    mainViewModel = this@MainViewModel
                                )
                            )
                            addOrRemoveRentalNumberFromFirebase(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                AddOrRemoveNumberAction.ADD,
                                rentalNumberData = rentalNumberData.value
                            )
                            snackbar(
                                response.body()!!.msg.toString(),
                                SnackbarDuration.Long
                            )
                            navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            snackbar(
                                getApplication<Application>().getString(R.string.couldnt_process_request),
                                SnackbarDuration.Short
                            )
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
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
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

    @SuppressLint("MutableCollectionMutableState")
    var listOfLiveRentalNumbers: MutableState<MutableList<RentalNumberData>> =
        mutableStateOf(mutableListOf())

    fun getLiveRentalNumbersList(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        orderRentalNumberLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.getLiveRentalNumbersList()
                        if (response.isSuccessful) {
                            listOfLiveRentalNumbers.value = response.body()!!.rentalNumberData


                            //This is used to keep only the numbers this user purchased by comparing the list from firebase
                            // and the list from the server
                            listOfLiveRentalNumbers.value.retainAll { it -> it.rentalId in _rentalNumbersList.value.map { it.rentalId } }

                            orderRentalNumberLoadingState.emit(LoadingState.LOADED)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        orderRentalNumberLoadingState.emit(LoadingState.ERROR)
//                        snackbar(
//                            getApplication<Application>().getString(R.string.time_out),
//                            SnackbarDuration.Short
//                        )
                    }
                } catch (e: Exception) {
                    orderRentalNumberLoadingState.emit(LoadingState.ERROR)
//                    snackbar(
//                        getApplication<Application>().getString(R.string.an_error_occurred),
//                        SnackbarDuration.Short
//                    )
                    Log.d("Tag", e.message.toString())
                }

            } else {
                orderRentalNumberLoadingState.emit(LoadingState.ERROR)
//                snackbar(
//                    getApplication<Application>().getString(R.string.device_not_connected),
//                    SnackbarDuration.Short
//                )
            }
        }
    }

    @SuppressLint("MutableCollectionMutableState")
    var listOfPendingRentalNumbers: MutableState<MutableList<RentalNumberData>> =
        mutableStateOf(mutableListOf())

    fun getPendingRentalNumbersList(snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        orderRentalNumberLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.getPendingRentalNumbersList()
                        if (response.isSuccessful) {
                            listOfPendingRentalNumbers.value =
                                response.body()!!.rentalNumberDataList

                            //This is used to keep only the numbers this user purchased by comparing the list from firebase
                            // and the list from the server

                            listOfPendingRentalNumbers.value.retainAll { it -> it.rentalId in _rentalNumbersList.value.map { it.rentalId } }

                            orderRentalNumberLoadingState.emit(LoadingState.LOADED)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        orderRentalNumberLoadingState.emit(LoadingState.ERROR)
//                        snackbar(
//                            getApplication<Application>().getString(R.string.time_out),
//                            SnackbarDuration.Short
//                        )
                    }
                } catch (e: Exception) {
                    orderRentalNumberLoadingState.emit(LoadingState.ERROR)
//                    snackbar(
//                        getApplication<Application>().getString(R.string.an_error_occurred),
//                        SnackbarDuration.Short
//                    )
                    Log.d("Tag", e.message.toString())
                }

            } else {
                orderRentalNumberLoadingState.emit(LoadingState.ERROR)
//                snackbar(
//                    getApplication<Application>().getString(R.string.device_not_connected),
//                    SnackbarDuration.Short
//                )
            }
        }
    }

    var rentalNumbersMessagesList: MutableState<List<Sms>> = mutableStateOf(listOf())

    var checkingRentalMessagesLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun getRentalNumberMessages(rentalId: String, snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        checkingRentalMessagesLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.getRentalNumberMessages(
                            rentalId = rentalId
                        )
                        if (response.isSuccessful && response.body()?.succeeded == true) {
                            rentalNumbersMessagesList.value = response.body()!!.data
                            checkingRentalMessagesLoadingState.emit(LoadingState.LOADED)
                        }

                    } ?: withContext(Dispatchers.Main) {
                        checkingRentalMessagesLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    checkingRentalMessagesLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                    Log.d("Tag", e.message.toString())
                }

            } else {
                checkingRentalMessagesLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }


    var activatingRentalNumberLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun activateRentalNumber(rentalId: String, snackbar: (String, SnackbarDuration) -> Unit) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        activatingRentalNumberLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.activateRentalNumber(
                            rentalId = rentalId
                        )
                        if (response.isSuccessful && response.body()?.succeeded == true) {
                            activatingRentalNumberLoadingState.emit(LoadingState.LOADED)
                            snackbar(
                                response.body()!!.msg.toString(),
                                SnackbarDuration.Long
                            )
                        } else {
                            snackbar(
                                getApplication<Application>().getString(R.string.couldnt_process_request),
                                SnackbarDuration.Short
                            )
                        }
                    } ?: withContext(Dispatchers.Main) {
                        activatingRentalNumberLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    activatingRentalNumberLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                    Log.d("Tag", e.message.toString())
                }

            } else {
                activatingRentalNumberLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    // This function will be launched later with a LaunchedEffect in the MassageDetails screen
    private var autoCheckingRentalMessagesLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun autoCheckRentalMessage(
        rentalId: String
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        autoCheckingRentalMessagesLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.getRentalNumberMessages(
                            rentalId = rentalId
                        )
                        if (response.isSuccessful) {
                            rentalNumbersMessagesList.value = response.body()!!.data
                            autoCheckingRentalMessagesLoadingState.emit(LoadingState.LOADED)
                        }

                    } ?: withContext(Dispatchers.Main) {
                        autoCheckingRentalMessagesLoadingState.emit(LoadingState.ERROR)
                        Log.d("Tag", "Time out")
//                        snackbar(
//                            getApplication<Application>().getString(R.string.time_out),
//                            SnackbarDuration.Short
//                        )
                    }
                } catch (e: Exception) {
                    autoCheckingRentalMessagesLoadingState.emit(LoadingState.ERROR)
//                    snackbar(
//                        getApplication<Application>().getString(R.string.an_error_occurred),
//                        SnackbarDuration.Short
//                    )
                    Log.d("Tag", e.message.toString())
                }

            } else {
                autoCheckingRentalMessagesLoadingState.emit(LoadingState.ERROR)
                Log.d("Tag", "Device not connected")
//                snackbar(
//                    getApplication<Application>().getString(R.string.device_not_connected),
//                    SnackbarDuration.Short
//                )
            }
        }
    }

    fun refreshRentalNumberMessagesCheck(
        rentalId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            _isRefreshing.emit(true)
            getRentalNumberMessages(
                rentalId = rentalId,
                snackbar = snackbar
            )
            _isRefreshing.emit(false)
        }
    }

    private var refundRentalNumberLoadingState = MutableStateFlow(LoadingState.IDLE)

    fun requestRefundRentalNumber(
        rentalNumberData: RentalNumberData,
        snackbar: (String, SnackbarDuration) -> Unit,
        navController: NavController
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        refundRentalNumberLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.requestRefundRentalNumber(
                            rentalId = rentalNumberData.rentalId
                        )
                        if (response.isSuccessful && response.body()?.succeeded == true) {
//                            rentalNumbersMessagesList.value = response.body()!!.data
                            refundRentalNumberLoadingState.emit(LoadingState.LOADED)
                            addBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(
                                    rentalNumberData.price,
                                    mainViewModel = this@MainViewModel
                                ),
                                addingBalanceState = AddingBalanceState.REFUND
                            )
                            // this variable is used to get reference from firebase of the same id since it's not the same object in the server
                            val selectedRentalNumber =
                                _rentalNumbersList.value.find { it.rentalId == rentalNumberData.rentalId }

                            addOrRemoveRentalNumberFromFirebase(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                action = AddOrRemoveNumberAction.REMOVE,
                                rentalNumberData = selectedRentalNumber
                            )

                            navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } else {
                            snackbar(
                                getApplication<Application>().getString(R.string.couldnt_process_request),
                                SnackbarDuration.Short
                            )
                        }
                    } ?: withContext(Dispatchers.Main) {
                        refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                    Log.d("Tag", e.message.toString())
                }

            } else {
                refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    //TODO: when renewing a number, we get it in created state then success or failure. This must be checked and tell the user about it.

    private var renewRentalNumber: MutableState<RenewRentalNumberData> = mutableStateOf(
        RenewRentalNumberData()
    )

    fun renewRentalNumber(
        rentalNumberData: RentalNumberData,
        snackbar: (String, SnackbarDuration) -> Unit,
        navController: NavController
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
                        refundRentalNumberLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.renewRentalNumber(
                            rentalId = rentalNumberData.rentalId
                        )
                        if (response.isSuccessful && response.body()?.succeeded == true) {
                            refundRentalNumberLoadingState.emit(LoadingState.LOADED)
                            renewRentalNumber.value = response.body()!!.renewRentalNumberData
                            reduceBalance(
                                context = getApplication<Application>(),
                                snackbar = snackbar,
                                currentBalance = _userBalance.value,
                                amount = dollarToCreditForPurchasingNumbers(
                                    rentalNumberData.price,
                                    mainViewModel = this@MainViewModel
                                )
                            )
                            navController.navigate(MainAppScreens.RentalNumbersMessages.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                            checkingRenewalState(
                                renewalId = renewRentalNumber.value.renewalId,
                                snackbar = snackbar
                            )
                        } else {
                            snackbar(
                                getApplication<Application>().getString(R.string.couldnt_process_request),
                                SnackbarDuration.Short
                            )
                        }
                    } ?: withContext(Dispatchers.Main) {
                        refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                    Log.d("Tag", e.message.toString())
                }
            } else {
                refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }


    private var renewalRequestState: MutableState<RenewalRequestStateData> =
        mutableStateOf(RenewalRequestStateData())

    private fun checkingRenewalState(
        renewalId: String,
        snackbar: (String, SnackbarDuration) -> Unit
    ) {
        viewModelScope.launch {
            if (hasInternetConnection(getApplication<Application>())) {
                try {
                    withTimeoutOrNull(TIMEOUT_IN_MILLIS) {
//                        refundRentalNumberLoadingState.emit(LoadingState.LOADING)
                        val response = repository.remote.checkingRenewalState(
                            renewalId = renewalId
                        )
                        if (response.isSuccessful && response.body()?.succeeded == true) {
//                            refundRentalNumberLoadingState.emit(LoadingState.LOADED)
                            renewalRequestState.value = response.body()!!.renewalRequestStateData

                            snackbar(
                                "Your renewal request was a ${renewalRequestState.value.state}",
                                SnackbarDuration.Short
                            )
                        } else {
                            snackbar(
                                getApplication<Application>().getString(R.string.couldnt_process_request),
                                SnackbarDuration.Short
                            )
                        }
                    } ?: withContext(Dispatchers.Main) {
                        refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                        snackbar(
                            getApplication<Application>().getString(R.string.time_out),
                            SnackbarDuration.Short
                        )
                    }
                } catch (e: Exception) {
                    refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                    snackbar(
                        getApplication<Application>().getString(R.string.an_error_occurred),
                        SnackbarDuration.Short
                    )
                    Log.d("Tag", e.message.toString())
                }
            } else {
                refundRentalNumberLoadingState.emit(LoadingState.ERROR)
                snackbar(
                    getApplication<Application>().getString(R.string.device_not_connected),
                    SnackbarDuration.Short
                )
            }
        }
    }

    // Write data to dataStore: storing the user's theme
    fun changeAppTheme() {
        DARK_THEME.value = !DARK_THEME.value
        runBlocking {
            dataStore.edit {
                it[dataStoreKey] = DARK_THEME.value
            }
        }
    }

    //Read data from dataStore
    val themeValue: Flow<Boolean?> = dataStore.data
        .map { preferences ->
            // No type safety.
            preferences[dataStoreKey]
        }
}