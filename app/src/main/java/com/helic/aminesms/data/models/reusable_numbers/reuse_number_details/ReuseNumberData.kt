package com.helic.aminesms.data.models.reusable_numbers.reuse_number_details


import com.google.gson.annotations.SerializedName

data class ReuseNumberData(
    @SerializedName("expiresAt")
    val expiresAt: Int = 0,
    @SerializedName("number")
    val number: String = "",
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("reuseableUntil")
    val reuseableUntil: Int = 0,
    @SerializedName("sms")
    val sms: Sms = Sms(),
    @SerializedName("state")
    val state: String = "",
    @SerializedName("temporaryNumberId")
    val temporaryNumberId: String = ""
)