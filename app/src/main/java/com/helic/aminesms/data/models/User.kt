package com.helic.aminesms.data.models

import com.helic.aminesms.data.models.order_number.OrderedNumberData

data class User(
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userBalance: Double = 0.0,
    val listOfNumbers: List<OrderedNumberData> = listOf<OrderedNumberData>()
)
