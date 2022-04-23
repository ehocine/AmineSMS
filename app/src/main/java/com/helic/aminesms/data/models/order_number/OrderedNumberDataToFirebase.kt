package com.helic.aminesms.data.models.order_number

import com.google.gson.annotations.SerializedName

data class OrderedNumberDataToFirebase(
    @SerializedName("expiresAt")
    var expiresAt: Int = 0,
    @SerializedName("number")
    var number: String = "",
    @SerializedName("price")
    var price: Double = 0.0,
    @SerializedName("reuseableUntil")
    var reuseableUntil: Int = 0,
    @SerializedName("state")
    var state: String = "",
    @SerializedName("temporaryNumberId")
    val temporaryNumberId: MutableList<String> = mutableListOf()
)
