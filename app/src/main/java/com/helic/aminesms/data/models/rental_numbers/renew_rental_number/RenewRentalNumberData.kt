package com.helic.aminesms.data.models.rental_numbers.renew_rental_number


import com.google.gson.annotations.SerializedName

data class RenewRentalNumberData(
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("renewalId")
    val renewalId: String = "",
    @SerializedName("state")
    val state: String = ""
)