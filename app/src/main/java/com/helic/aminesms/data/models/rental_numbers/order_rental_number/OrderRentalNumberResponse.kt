package com.helic.aminesms.data.models.rental_numbers.order_rental_number


import com.google.gson.annotations.SerializedName
import com.helic.aminesms.data.models.number_data.RentalNumberData

data class OrderRentalNumberResponse(
    @SerializedName("data")
    val rentalNumberData: RentalNumberData,
    @SerializedName("errors")
    val errors: Any,
    @SerializedName("msg")
    val msg: Any,
    @SerializedName("succeeded")
    val succeeded: Boolean
)