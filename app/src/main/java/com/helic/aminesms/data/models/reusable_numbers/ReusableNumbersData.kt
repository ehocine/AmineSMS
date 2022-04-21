package com.helic.aminesms.data.models.reusable_numbers


import com.google.gson.annotations.SerializedName

data class ReusableNumbersData(
    @SerializedName("number")
    val number: String = "",
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("reusableId")
    val reusableId: String = "",
    @SerializedName("reuseableUntil")
    val reuseableUntil: Int = 0,
    @SerializedName("serviceName")
    val serviceName: String = ""
)