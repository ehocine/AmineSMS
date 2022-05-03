package com.helic.aminesms.data.models

import com.helic.aminesms.data.models.number_data.TempNumberData

data class User(
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userBalance: Double = 0.0,
    val listOfTempNumbers: List<TempNumberData> = listOf()
)
