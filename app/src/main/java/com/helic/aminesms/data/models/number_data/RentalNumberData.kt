package com.helic.aminesms.data.models.number_data


import com.google.gson.annotations.SerializedName

data class RentalNumberData(
    @SerializedName("createdAt")
    val createdAt: Int = 0,
    @SerializedName("expiresAt")
    val expiresAt: Int? = null,
    @SerializedName("isRefundable")
    val isRefundable: Boolean = false,
    @SerializedName("isRenewable")
    val isRenewable: Boolean = false,
    @SerializedName("number")
    val number: String? = null,
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("rentalId")
    val rentalId: String = "",
    @SerializedName("rentalServiceName")
    val rentalServiceName: String = "",
    @SerializedName("state")
    var state: String = ""
)