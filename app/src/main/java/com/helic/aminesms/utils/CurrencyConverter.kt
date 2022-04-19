package com.helic.aminesms.utils

fun dollarToCreditForPurchasingCurrency(dollars: Double): Double {
    return dollars * 10
}

fun dollarToCreditForPurchasingNumbers(price: Double): Double {
    return price * 10 + 3 * price / 4
}