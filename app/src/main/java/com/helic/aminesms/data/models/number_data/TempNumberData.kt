package com.helic.aminesms.data.models.number_data


import com.google.gson.annotations.SerializedName

data class TempNumberData(
    @SerializedName("expiresAt")
    val expiresAt: Int = 0,
    @SerializedName("number")
    val number: String = "",
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("reuseableUntil")
    var reuseableUntil: Int = 0,
    @SerializedName("state")
    var state: String = "",
    @SerializedName("temporaryNumberId")
    val temporaryNumberId: String = ""
)