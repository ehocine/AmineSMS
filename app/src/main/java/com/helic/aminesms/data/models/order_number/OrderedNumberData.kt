package com.helic.aminesms.data.models.order_number


import com.google.gson.annotations.SerializedName

data class OrderedNumberData(
    @SerializedName("expiresAt")
    val expiresAt: Int = 0,
    @SerializedName("number")
    val number: String = "",
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("reuseableUntil")
    val reuseableUntil: Int = 0,
    @SerializedName("state")
    var state: String = "",
    @SerializedName("temporaryNumberId")
    val temporaryNumberId: String = ""
)