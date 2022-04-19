package com.helic.aminesms.data.models.messages


import com.google.gson.annotations.SerializedName

data class Data(
    @SerializedName("sms")
    val sms: Sms,
    @SerializedName("expiresAt")
    val expiresAt: Int,
    @SerializedName("number")
    val number: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("reuseableUntil")
    val reuseableUntil: Int,
    @SerializedName("state")
    val state: String,
    @SerializedName("temporaryNumberId")
    val temporaryNumberId: String
)