package com.helic.aminesms.utils

import com.helic.aminesms.data.viewmodels.MainViewModel

fun dollarToCreditForPurchasingCurrency(dollars: Double, mainViewModel: MainViewModel): Double {
    return dollars * mainViewModel.purchasingCurrency.value
}

fun dollarToCreditForPurchasingNumbers(price: Double, mainViewModel: MainViewModel): Double {
    return price * mainViewModel.purchasingNumbersA.value + price * mainViewModel.purchasingNumbersB.value
}