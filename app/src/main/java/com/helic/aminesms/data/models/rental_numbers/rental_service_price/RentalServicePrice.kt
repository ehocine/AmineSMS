package com.helic.aminesms.data.models.rental_numbers.rental_service_price

import com.google.gson.annotations.SerializedName

data class RentalServicePrice(
    @SerializedName("data")
    val price: Double = 0.0,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)
