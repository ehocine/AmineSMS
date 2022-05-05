package com.helic.aminesms.data.models.number_data


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.messages.Sms

data class RentalNumberData(
    @SerializedName("createdAt")
    val createdAt: Int = 0,
    @SerializedName("expiresAt")
    val expiresAt: Any = 0,
    @SerializedName("isRefundable")
    val isRefundable: Boolean = false,
    @SerializedName("isRenewable")
    val isRenewable: Boolean = false,
    @SerializedName("number")
    val number: Any = "",
    @SerializedName("price")
    val price: Int = 0,
    @SerializedName("rentalId")
    val rentalId: String = "",
    @SerializedName("rentalServiceName")
    val rentalServiceName: String = "",
    @SerializedName("sms")
    val sms: List<Any> = listOf(),
    @SerializedName("state")
    val state: String = ""
)