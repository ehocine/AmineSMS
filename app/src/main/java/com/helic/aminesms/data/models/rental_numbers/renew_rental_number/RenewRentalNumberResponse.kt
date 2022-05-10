package com.helic.aminesms.data.models.rental_numbers.renew_rental_number


import com.google.gson.annotations.SerializedName

data class RenewRentalNumberResponse(
    @SerializedName("data")
    val renewRentalNumberData: RenewRentalNumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)